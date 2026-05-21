package com.github.ixmoyren.typilot.psi

import com.github.ixmoyren.typilot.TypstSyntaxKind
import com.github.ixmoyren.typilot.language.TypstLanguage
import com.intellij.psi.tree.IElementType

class TypstElementType(val kind: TypstSyntaxKind?, name: String = kind!!.name) : IElementType(name, TypstLanguage.INSTANCE) {
    override fun toString(): String {
        return "TypstElementType." + super.toString()
    }
}

val elementTypeMap = TypstSyntaxKind.entries.associateWith { TypstElementType(it) }

val TypstSyntaxKind.elementType
    get() = elementTypeMap[this]!!
