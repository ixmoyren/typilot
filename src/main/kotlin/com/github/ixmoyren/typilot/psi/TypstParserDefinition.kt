package com.github.ixmoyren.typilot.psi

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

    override fun createFile(viewProvider: FileViewProvider): PsiFile =
        TypstPsiFile(viewProvider)

    override fun createElement(node: ASTNode): PsiElement {
        return TypstPsiElement(node)
    }
}