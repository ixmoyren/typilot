package com.github.ixmoyren.typilot.psi

import com.github.ixmoyren.typilot.TypstSyntaxKind
import com.github.ixmoyren.typilot.language.TypstLanguage
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

val TYPST_FILE = IFileElementType("Typst", TypstLanguage.INSTANCE)

class TypstParserDefinition : ParserDefinition {
    override fun createLexer(project: Project?): Lexer = TypstLexer()

    override fun createParser(project: Project?): PsiParser = TypstParser()

    override fun getFileNodeType(): IFileElementType = TYPST_FILE

    override fun getWhitespaceTokens(): TokenSet = TypstTokenType.WHITESPACE_TOKEN_SET

    override fun getCommentTokens(): TokenSet = TypstTokenType.COMMENT_TOKEN_SET

    override fun getStringLiteralElements(): TokenSet = TokenSet.EMPTY

    override fun createFile(viewProvider: FileViewProvider): PsiFile = TypstPsiFile(viewProvider)

    override fun createElement(node: ASTNode): PsiElement {
        val type = node.elementType
        val kind =
            when (type) {
                is TypstElementType -> type.kind!!
                is TypstTokenType -> type.kind!!
                else -> return ASTWrapperPsiElement(node)
            }
        if (kind == TypstSyntaxKind.IDENT && type is TypstTokenType) {
            return dealWithIdentNode(node)
        }
        if (kind == TypstSyntaxKind.RAW && type is TypstElementType && isRawBlock(node)) {
            return TypstRawBlockPsiElement(node)
        }
        return TypstSyntaxKindToPsiElementMap[kind]?.let { it(node) } ?: TypstErrorPsiElement(node)
    }

    private fun dealWithIdentNode(node: ASTNode): PsiElement {
        var current: ASTNode? = node
        var parent: ASTNode? = node.treeParent
        while (parent != null) {
            when (parent.elementType) {
                TypstSyntaxKind.PARAMS.tokenType, TypstSyntaxKind.LET_BINDING.tokenType -> {
                    return TypstIdentDeclPsiElement(node)
                }
                TypstSyntaxKind.SPREAD.tokenType, TypstSyntaxKind.DESTRUCTURING.tokenType -> Unit
                TypstSyntaxKind.NAMED.tokenType -> {
                    val beforeColon = generateSequence(current) { it.treeNext }
                        .any { it.elementType == TypstSyntaxKind.COLON.tokenType }
                    if (beforeColon) Unit else return TypstIdentRefPsiElement(node)
                }
                TypstSyntaxKind.CLOSURE.tokenType -> {
                    when {
                        parent.treeParent.elementType == TypstSyntaxKind.LET_BINDING.tokenType -> {
                            val beforeEq = generateSequence(current) { it.treeNext }
                                .any { it.elementType == TypstSyntaxKind.EQ.tokenType }
                            return if (beforeEq) TypstIdentDeclPsiElement(node) else TypstIdentRefPsiElement(node)
                        }
                        parent.treeParent.elementType == TypstSyntaxKind.CONTEXTUAL.tokenType -> {
                            return TypstIdentRefPsiElement(node)
                        }
                        else -> {
                            val beforeArrow = generateSequence(current) { it.treeNext }
                                .any { it.elementType == TypstSyntaxKind.ARROW.tokenType }
                            return if (beforeArrow) TypstIdentDeclPsiElement(node) else TypstIdentRefPsiElement(node)
                        }
                    }
                }

                else -> {
                    return TypstIdentRefPsiElement(node)
                }
            }
            current = parent
            parent = parent.treeParent
        }
        return TypstIdentRefPsiElement(node)
    }

    private fun isRawBlock(node: ASTNode): Boolean {
        val firstChild = node.firstChildNode ?: return false

        val children = node.children().toList()
        if (children.size < 2) return false

        if (firstChild.elementType != node.lastChildNode.elementType) return false

        val firstType =
            when (val type = firstChild.elementType) {
                is TypstTokenType -> type.kind!!
                else -> return false
            }
        if (firstType != TypstSyntaxKind.RAW_DELIM || firstChild.textLength < 3) return false

        val secondChild = children[1]
        val secondType =
            when (val type = secondChild.elementType) {
                is TypstTokenType -> type.kind!!
                else -> return false
            }
        return secondType == TypstSyntaxKind.RAW_LANG && secondChild.textLength >= 1
    }
}
