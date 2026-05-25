package com.github.ixmoyren.typilot.psi

import com.github.ixmoyren.typilot.TypstSyntaxKind
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
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
        return when (val type = node.elementType as TypstElementType) {
            TypstSyntaxKind.LET_BINDING.elementType -> TypstLetBindingElement(node)
            TypstSyntaxKind.FUNC_CALL.elementType -> TypstFuncCallElement(node)
            TypstSyntaxKind.FIELD_ACCESS.elementType -> TypstFieldAccessElement(node)
            TypstSyntaxKind.MATH_FIELD_ACCESS.elementType -> TypstFieldAccessElement(node)
            TypstSyntaxKind.CLOSURE.elementType -> TypstClosureElement(node)
            TypstSyntaxKind.MODULE_IMPORT.elementType -> TypstModuleImportElement(node)
            TypstSyntaxKind.MODULE_INCLUDE.elementType -> TypstModuleIncludeElement(node)
            TypstSyntaxKind.REF.elementType -> TypstRefElement(node)
            TypstSyntaxKind.LABEL.elementType -> TypstLabelElement(node)
            TypstSyntaxKind.HEADING.elementType -> TypstHeadingElement(node)
            TypstSyntaxKind.SET_RULE.elementType -> TypstSetRuleElement(node)
            TypstSyntaxKind.SHOW_RULE.elementType -> TypstShowRuleElement(node)

            in TypstElementType.IDENT_ELEMENTS ->
                TypstIdentElement(type, node.chars)

            in TypstElementType.KEYWORD_ELEMENTS ->
                TypstKeywordElement(type, node.chars)

            in TypstElementType.COMMENT_ELEMENTS ->
                TypstCommentElement(type, node.chars)

            in TypstElementType.LITERAL_ELEMENTS,
            in TypstElementType.OPERATOR_ELEMENTS ->
                TypstLeafElement(type, node.chars)

            else -> TypstCompositeElement(node)
        }
    }
}
