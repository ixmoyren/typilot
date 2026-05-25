package com.github.ixmoyren.typilot.psi

import com.github.ixmoyren.typilot.AstNode
import com.github.ixmoyren.typilot.TypstSyntaxKind
import com.github.ixmoyren.typilot.language.TypstLanguage
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

class TypstElementType(val kind: TypstSyntaxKind?, name: String = kind!!.name) : IElementType(name, TypstLanguage.INSTANCE) {
    override fun toString(): String {
        return "TypstElementType." + super.toString()
    }

    companion object {
        private val elementTypeMap: Map<TypstSyntaxKind, TypstElementType> by lazy { TypstSyntaxKind.entries.associateWith { TypstElementType(it) } }

        fun getElementType(kind: TypstSyntaxKind): TypstElementType = elementTypeMap.getValue(kind)
    }
}

val TypstSyntaxKind.elementType
    get() = TypstElementType.getElementType(this)

val AstNode.type
    get() = this.kind.elementType
