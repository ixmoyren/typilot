package com.github.ixmoyren.typilot.misc

import com.github.ixmoyren.typilot.psi.TypstCodePsiElement
import com.github.ixmoyren.typilot.psi.TypstEmbeddedCodePsiElement
import com.github.ixmoyren.typilot.psi.TypstMathPsiElement
import com.github.ixmoyren.typilot.psi.TypstPsiFile
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.SelectionModel
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

class TypstTypedHandler : TypedHandlerDelegate() {
    override fun beforeSelectionRemoved(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
        if (file !is TypstPsiFile) return Result.CONTINUE

        with(editor) {
            return selectionModel.takeIf { it.hasSelection() }?.let { surround(it, c) } ?: pair(c, file, caretModel.offset)
        }
    }
}

private fun Editor.surround(selection: SelectionModel, c: Char): TypedHandlerDelegate.Result {
    val text = selection.selectedText ?: return TypedHandlerDelegate.Result.CONTINUE
    val wrapped = c.wrap(text) ?: return TypedHandlerDelegate.Result.CONTINUE

    document.replaceString(selection.selectionStart, selection.selectionEnd, wrapped)
    caretModel.moveToOffset(selection.selectionStart + wrapped.length)
    return TypedHandlerDelegate.Result.STOP
}

private fun Editor.pair(c: Char, file: TypstPsiFile, offset: Int): TypedHandlerDelegate.Result {
    val action = Action.forChar(c, file, offset, document) ?: return TypedHandlerDelegate.Result.CONTINUE

    action.insert?.let { document.insertString(offset, it) }
    caretModel.moveToOffset(offset + action.caretDelta)
    return TypedHandlerDelegate.Result.STOP
}

private fun Char.wrap(text: String): String? =
    when (this) {
        '_',
        '*',
        '$',
        '`' -> "$this$text$this"
        else -> null
    }

private fun Char.mate(): Char =
    when (this) {
        '(' -> ')'
        '[' -> ']'
        '{' -> '}'
        '<' -> '>'
        else -> this
    }

private open class Action(val insert: String?, val caretDelta: Int) {
    companion object {
        fun forChar(c: Char, file: TypstPsiFile, offset: Int, document: Document): Action? {
            val chars = document.charsSequence

            return when (c) {
                '(',
                '[',
                '{' -> PairAction(c, c.mate())

                '<' -> file.markupContext(offset)?.let { PairAction(c, c.mate()) }
                ')',
                ']',
                '}',
                '>' -> chars.getOrElse(offset) { Char.MAX_VALUE }.takeIf { it == c }?.let { SkipAction() }

                '_',
                '*',
                '$' ->
                    when {
                        chars.getOrElse(offset) { Char.MAX_VALUE } == c -> SkipAction()
                        file.markupContext(offset) != null -> PairAction(c, c)
                        else -> null
                    }
                '`' ->
                    when {
                        chars.getOrElse(offset) { Char.MAX_VALUE } == c && chars.getOrElse(offset - 1) { Char.MAX_VALUE } != c -> SkipAction()
                        chars.getOrElse(offset) { Char.MAX_VALUE } == c && chars.getOrElse(offset - 1) { Char.MAX_VALUE } == c -> PairAction(c, c)
                        file.markupContext(offset) != null -> PairAction(c, c)
                        else -> null
                    }

                else -> null
            }
        }
    }
}

private class PairAction(open: Char, close: Char) : Action("$open$close", 1)

private class SkipAction : Action(null, 1)

private fun TypstPsiFile.markupContext(offset: Int): TypstPsiFile? {
    val element = findElementAt(offset) ?: return this
    var current: PsiElement? = element
    while (current != null) {
        if (current is TypstMathPsiElement || current is TypstCodePsiElement || current is TypstEmbeddedCodePsiElement) return null
        current = current.parent
    }
    return this
}
