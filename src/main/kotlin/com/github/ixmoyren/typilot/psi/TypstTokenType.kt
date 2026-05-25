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
        private val tokenTypeMap: Map<TypstSyntaxKind, TypstTokenType> by lazy {
            TypstSyntaxKind.entries.associateWith { TypstTokenType(it) }
        }

        fun getTokenType(kind: TypstSyntaxKind): TypstTokenType = tokenTypeMap.getValue(kind)

        val COMMENT_TOKEN_SET = TokenSet.create(TypstSyntaxKind.LINE_COMMENT.tokenType, TypstSyntaxKind.BLOCK_COMMENT.tokenType, TypstSyntaxKind.SHEBANG.tokenType)

        val WHITESPACE_TOKEN_SET = TokenSet.create(TypstSyntaxKind.SPACE.tokenType)

        val TYPST_FILE = IFileElementType("Typst", TypstLanguage.INSTANCE)
    }
}

val TypstSyntaxKind.tokenType
    get() = TypstTokenType.getTokenType(this)

val Token.type
    get() = this.kind.tokenType
