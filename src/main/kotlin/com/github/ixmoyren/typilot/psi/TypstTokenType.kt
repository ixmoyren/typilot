package com.github.ixmoyren.typilot.psi

import com.github.ixmoyren.typilot.TypstSyntaxKind
import com.github.ixmoyren.typilot.language.TypstLanguage
import com.intellij.psi.tree.IElementType

class TypstTokenType(val kind: TypstSyntaxKind?, name: String = kind!!.name) : IElementType(name, TypstLanguage.INSTANCE) {
    override fun toString(): String {
        return "TypstTokenType." + super.toString()
    }
}

val tokenTypeMap = TypstSyntaxKind.entries.associateWith { TypstTokenType(it) }

val TypstSyntaxKind.tokenType
    get() = tokenTypeMap[this]!!
