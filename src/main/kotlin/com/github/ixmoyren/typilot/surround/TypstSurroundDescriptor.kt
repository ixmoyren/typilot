package com.github.ixmoyren.typilot.surround

import com.github.ixmoyren.typilot.TypilotBundle
import com.github.ixmoyren.typilot.psi.TypstPsiFile
import com.intellij.lang.surroundWith.SurroundDescriptor
import com.intellij.lang.surroundWith.Surrounder
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil

class TypstSurroundDescriptor : SurroundDescriptor {
    override fun getSurrounders(): Array<Surrounder> =
        arrayOf(
            TypstStrongSurrounder,
            TypstEmphasisSurrounder,
            TypstMathSurrounder,
            TypstCodeSurrounder,
        )

    override fun isExclusive(): Boolean = false

    override fun getElementsToSurround(file: PsiFile, startOffset: Int, endOffset: Int): Array<PsiElement> {
        val startElement = file.findElementAt(startOffset) ?: return PsiElement.EMPTY_ARRAY
        val elements = mutableListOf(startElement)
        if (endOffset > startOffset) {
            var next = PsiTreeUtil.nextLeaf(startElement)
            while (next != null && next.textRange.startOffset < endOffset) {
                elements.add(next)
                next = PsiTreeUtil.nextLeaf(next)
            }
        }
        return elements.toTypedArray()
    }
}

private abstract class TypstSurrounder(
    private val delimiter: String,
    private val descriptionKey: String,
) : Surrounder {
    override fun getTemplateDescription(): String = TypilotBundle[descriptionKey]

    override fun isApplicable(elements: Array<out PsiElement>): Boolean {
        if (elements.isEmpty()) return false
        val file = elements.first().containingFile ?: return false
        return file is TypstPsiFile
    }

    override fun surroundElements(project: Project, editor: Editor, elements: Array<out PsiElement>): TextRange? {
        val document = editor.document
        val selectionModel = editor.selectionModel
        val startOffset = selectionModel.selectionStart
        val endOffset = selectionModel.selectionEnd
        val selectedText = selectionModel.selectedText ?: return null

        WriteCommandAction.runWriteCommandAction(project) {
            document.replaceString(startOffset, endOffset, "$delimiter$selectedText$delimiter")
        }

        return TextRange(startOffset, startOffset + delimiter.length + selectedText.length + delimiter.length)
    }
}

private data object TypstStrongSurrounder : TypstSurrounder("*", "surround.strong")

private data object TypstEmphasisSurrounder : TypstSurrounder("_", "surround.emph")

private data object TypstMathSurrounder : TypstSurrounder("$", "surround.math")

private data object TypstCodeSurrounder : TypstSurrounder("`", "surround.code")
