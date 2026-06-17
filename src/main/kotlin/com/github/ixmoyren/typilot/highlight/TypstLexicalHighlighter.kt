package com.github.ixmoyren.typilot.highlight

import com.github.ixmoyren.typalize.TypstSyntaxKind
import com.github.ixmoyren.typilot.psi.TypstLexer
import com.github.ixmoyren.typilot.psi.TypstSyntaxKindUtils
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
            is TypstSyntaxKind.LineComment -> arrayOf(TypstHighlightingColors.LINE_COMMENT.key)
            is TypstSyntaxKind.BlockComment -> arrayOf(TypstHighlightingColors.BLOCK_COMMENT.key)
            is TypstSyntaxKind.Label -> arrayOf(TypstHighlightingColors.LABELS.key)
            is TypstSyntaxKind.Str -> arrayOf(TypstHighlightingColors.STRINGS.key)
            in TypstSyntaxKindUtils.keywordSet -> arrayOf(TypstHighlightingColors.KEYWORD.key)
            in TypstSyntaxKindUtils.operatorSet -> arrayOf(TypstHighlightingColors.OPERATOR.key)
            else -> arrayOf()
        }
            as Array<out TextAttributesKey?>
    }
}

class TypstSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): SyntaxHighlighter = TypstLexicalHighlighter()
}
