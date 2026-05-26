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
        val type = node.elementType as TypstElementType
        return when (type.kind) {
            TypstSyntaxKind.LET_BINDING -> TypstLetBindingElement(node)
            TypstSyntaxKind.FUNC_CALL-> TypstFuncCallElement(node)
            TypstSyntaxKind.FIELD_ACCESS -> TypstFieldAccessElement(node)
            TypstSyntaxKind.MATH_FIELD_ACCESS -> TypstFieldAccessElement(node)
            TypstSyntaxKind.CLOSURE -> TypstClosureElement(node)
            TypstSyntaxKind.MODULE_IMPORT -> TypstModuleImportElement(node)
            TypstSyntaxKind.MODULE_INCLUDE -> TypstModuleIncludeElement(node)
            TypstSyntaxKind.REF -> TypstRefElement(node)
            TypstSyntaxKind.LABEL -> TypstLabelElement(node)
            TypstSyntaxKind.HEADING-> TypstHeadingElement(node)
            TypstSyntaxKind.SET_RULE -> TypstSetRuleElement(node)
            TypstSyntaxKind.SHOW_RULE-> TypstShowRuleElement(node)
            TypstSyntaxKind.RAW -> TypstRawBlockElement(node)
            in TypstSyntaxKind.IDENT_SET -> TypstIdentElement(node)
            in TypstSyntaxKind.KEYWORD_SET -> TypstKeywordElement(node)
            in TypstSyntaxKind.COMMENT_SET -> TypstCommentElement(node)
            in TypstSyntaxKind.OPERATOR_SET,
            in TypstSyntaxKind.LITERAL_SET -> TypstLeafElement(node)
            else -> TypstCompositeElement(node)
        }
    }
}
