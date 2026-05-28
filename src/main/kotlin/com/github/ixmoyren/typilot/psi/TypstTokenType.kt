package com.github.ixmoyren.typilot.psi

import com.github.ixmoyren.typilot.Token
import com.github.ixmoyren.typilot.TypstSyntaxKind
import com.github.ixmoyren.typilot.language.TypstLanguage
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

class TypstTokenType(val kind: TypstSyntaxKind?, val name: String = kind!!.name) : IElementType(name, TypstLanguage.INSTANCE) {
    override fun toString(): String {
        return "TypstTokenType." + super.toString()
    }

    companion object {
        private val tokenTypeMap: Map<TypstSyntaxKind, TypstTokenType> by lazy { TypstSyntaxKind.entries.associateWith { TypstTokenType(it) } }

        fun getTokenType(kind: TypstSyntaxKind): TypstTokenType = tokenTypeMap.getValue(kind)

        val COMMENT_TOKEN_SET by lazy { TokenSet.create(*TypstSyntaxKind.COMMENT_SET.map { it.tokenType }.toTypedArray()) }

        val WHITESPACE_TOKEN_SET by lazy { TokenSet.create(*TypstSyntaxKind.SPACE_SET.map { it.tokenType }.toTypedArray()) }
    }
}

val TypstSyntaxKind.tokenType: TypstTokenType
    get() = TypstTokenType.getTokenType(this)

val Token.type: TypstTokenType
    get() = this.kind.tokenType
