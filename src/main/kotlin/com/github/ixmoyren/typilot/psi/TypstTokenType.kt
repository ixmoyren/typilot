package com.github.ixmoyren.typilot.psi

import com.github.ixmoyren.typilot.Token
import com.github.ixmoyren.typilot.TypstSyntaxKind
import com.github.ixmoyren.typilot.language.TypstLanguage
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet

class TypstTokenType(val kind: TypstSyntaxKind?, name: String = kind!!.name) : IElementType(name, TypstLanguage.INSTANCE) {
    override fun toString(): String {
        return "TypstTokenType." + super.toString()
    }

    companion object {
        val WHITESPACE = TypstTokenType(null, "Whitespace")
        val COMMENT_TOKEN_SET = TokenSet.create(TypstSyntaxKind.LINE_COMMENT.tokenType, TypstSyntaxKind.BLOCK_COMMENT.tokenType, TypstSyntaxKind.SHEBANG.tokenType)

        val WHITESPACE_TOKEN_SET = TokenSet.create()

        val TYPST_FILE = IFileElementType("Typst", TypstLanguage.INSTANCE)
    }
}

private val tokenTypeMap = TypstSyntaxKind.entries.associateWith { TypstTokenType(it) }

val TypstSyntaxKind.tokenType
    get() = tokenTypeMap[this]!!

val Token.type
    get() = this.kind.tokenType
