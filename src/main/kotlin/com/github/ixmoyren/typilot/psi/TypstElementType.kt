package com.github.ixmoyren.typilot.psi

import com.github.ixmoyren.typilot.Event
import com.github.ixmoyren.typilot.Token
import com.github.ixmoyren.typilot.TypstSyntaxKind
import com.github.ixmoyren.typilot.language.TypstLanguage
import com.intellij.psi.tree.IElementType

class TypstElementType(val kind: TypstSyntaxKind?, name: String = kind!!.name) : IElementType(name, TypstLanguage.INSTANCE) {
    override fun toString(): String {
        return "TypstElementType." + super.toString()
    }
}

private val elementTypeMap = TypstSyntaxKind.entries.associateWith { TypstElementType(it) }

val TypstSyntaxKind.elementType
    get() = elementTypeMap[this]!!

val Event.type
    get() = when (this) {
        is Event.Enter -> kind.elementType
        is Event.Exit -> kind.elementType
        is Event.Leaf -> kind.elementType
    }