package com.github.ixmoyren.typilot.highlight

import com.github.ixmoyren.typilot.TypstSyntaxKind
import com.github.ixmoyren.typilot.psi.KEYWORD_SET
import com.github.ixmoyren.typilot.psi.OPERATOR_SET
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
            TypstSyntaxKind.LINE_COMMENT -> arrayOf(TypstHighlightingColors.LINE_COMMENT.key)
            TypstSyntaxKind.BLOCK_COMMENT -> arrayOf(TypstHighlightingColors.BLOCK_COMMENT.key)
            TypstSyntaxKind.LABEL -> arrayOf(TypstHighlightingColors.LABELS.key)
            TypstSyntaxKind.STR -> arrayOf(TypstHighlightingColors.STRINGS.key)
            in TypstSyntaxKind.KEYWORD_SET -> arrayOf(TypstHighlightingColors.KEYWORD.key)
            in TypstSyntaxKind.OPERATOR_SET -> arrayOf(TypstHighlightingColors.OPERATOR.key)
            else -> arrayOf()
        }
            as Array<out TextAttributesKey?>
    }
}

class TypstSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): SyntaxHighlighter = TypstLexicalHighlighter()
}
