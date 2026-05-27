package com.github.ixmoyren.typilot.highlight

import com.github.ixmoyren.typilot.TypstHighlightTag
import com.github.ixmoyren.typilot.TypstSyntaxKind
import com.github.ixmoyren.typilot.psi.COMMENT_SET
import com.github.ixmoyren.typilot.psi.IDENT_SET
import com.github.ixmoyren.typilot.psi.KEYWORD_SET
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

class TypstLexicalHighlighter : SyntaxHighlighterBase() {
    override fun getHighlightingLexer(): Lexer = TypstLexer()

    override fun getTokenHighlights(tokenType: IElementType?): Array<out TextAttributesKey?> {
        val typstToken = tokenType as? TypstTokenType ?: return arrayOf()
        return when (typstToken.kind) {
            TypstSyntaxKind.HEADING -> arrayOf(TypstHighlightTag.HEADING.Color)
            TypstSyntaxKind.ERROR -> arrayOf(TypstHighlightTag.ERROR.Color)
            TypstSyntaxKind.RAW -> arrayOf(TypstHighlightTag.RAW.Color)
            TypstSyntaxKind.STR -> arrayOf(TypstHighlightTag.STRING.Color)
            in TypstSyntaxKind.COMMENT_SET -> arrayOf(TypstHighlightTag.COMMENT.Color)
            in TypstSyntaxKind.KEYWORD_SET -> arrayOf(TypstHighlightTag.KEYWORD.Color)
            in TypstSyntaxKind.IDENT_SET -> arrayOf(TypstHighlightTag.INTERPOLATED.Color)
            else -> arrayOf()
        }
    }
}

class TypstSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): SyntaxHighlighter = TypstLexicalHighlighter()
}
