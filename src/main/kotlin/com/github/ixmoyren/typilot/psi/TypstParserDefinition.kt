package com.github.ixmoyren.typilot.psi

import com.github.ixmoyren.typilot.TypstSyntaxKind
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lang.tree.util.children
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet

class TypstParserDefinition : ParserDefinition {
    override fun createLexer(project: Project?): Lexer = TypstLexer()

    override fun createParser(project: Project?): PsiParser = TypstParser()

    override fun getFileNodeType(): IFileElementType = TypstTokenType.TYPST_FILE

    override fun getWhitespaceTokens(): TokenSet = TypstTokenType.WHITESPACE_TOKEN_SET

    override fun getCommentTokens(): TokenSet = TypstTokenType.COMMENT_TOKEN_SET

    override fun getStringLiteralElements(): TokenSet = TokenSet.EMPTY

    override fun createFile(viewProvider: FileViewProvider): PsiFile = TypstPsiFile(viewProvider)

    override fun createElement(node: ASTNode): PsiElement {
        val kind = when(val type = node.elementType) {
            is TypstElementType -> type.kind!!
            is TypstTokenType -> type.kind!!
            else -> return ASTWrapperPsiElement(node)
        }
        if (kind == TypstSyntaxKind.RAW && isRawBlock(node)) {
            return TypstRawBlockPsiElement(node)
        }
        return TypstSyntaxKindToPsiElementMap[kind]?.let { it(node) } ?: TypstErrorPsiElement(node)
    }

    private fun isRawBlock(node: ASTNode): Boolean {
        val firstChild = node.firstChildNode ?: return false

        val children = node.children().toList()
        if (children.size < 2) return false

        if (firstChild.elementType != node.lastChildNode.elementType) return false

        val firstType = when(val type = firstChild.elementType) {
            is TypstElementType -> type.kind!!
            is TypstTokenType -> type.kind!!
            else -> return false
        }
        if (firstType != TypstSyntaxKind.RAW_DELIM && firstChild.textLength >= 3) return false

        val secondChild = children[1]
        val secondType =  when(val type = secondChild.elementType) {
            is TypstElementType -> type.kind!!
            is TypstTokenType -> type.kind!!
            else -> return false
        }
        return secondType == TypstSyntaxKind.RAW_LANG && secondChild.textLength >= 1
    }
}
