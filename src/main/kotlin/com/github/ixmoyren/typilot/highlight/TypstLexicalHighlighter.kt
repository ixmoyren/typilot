package com.github.ixmoyren.typilot.highlight

import com.github.ixmoyren.typilot.psi.TypstLexer
import com.github.ixmoyren.typilot.psi.TypstTokenType
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.tree.IElementType

class TypstLexicalHighlighter: SyntaxHighlighterBase() {
    override fun getHighlightingLexer(): Lexer = TypstLexer()

    override fun getTokenHighlights(tokenType: IElementType?): Array<out TextAttributesKey?> {
        val color = (tokenType as? TypstTokenType)?.tag?.Color ?: DefaultColor
        return arrayOf(color)
    }
}

class TypstSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): SyntaxHighlighter =
        TypstLexicalHighlighter()
}