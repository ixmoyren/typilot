package com.github.ixmoyren.typilot.psi

import com.github.ixmoyren.typalize.ASTNode
import com.github.ixmoyren.typalize.TypstSyntaxKind
import com.github.ixmoyren.typilot.language.TypstLanguage
import com.intellij.psi.tree.IElementType

class TypstElementType(val kind: TypstSyntaxKind?, name: String = kind!!.name) : IElementType(name, TypstLanguage.INSTANCE) {
    override fun toString(): String {
        return "TypstElementType." + super.toString()
    }

    companion object {
        private val elementTypeMap: Map<TypstSyntaxKind, TypstElementType> by lazy { TypstSyntaxKindUtils.entries.associateWith { TypstElementType(it) } }

        fun getElementType(kind: TypstSyntaxKind): TypstElementType = elementTypeMap.getValue(kind)
    }
}

val TypstSyntaxKind.elementType: TypstElementType
    get() = TypstElementType.getElementType(this)

val ASTNode.type: TypstElementType
    get() = this.kind.elementType

val ASTNode.isLeaf: Boolean
    get() = this.is_leaf

val ASTNode.isError: Boolean
    get() = this.is_error

val ASTNode.errorMessage: String?
    get() = this.error_message.map { it }.orElse("")

val ASTNode.childrenCount: Int
    get() = this.children_count

val ASTNode.isSpace: Boolean
    get() = this.kind in TypstSyntaxKindUtils.spaceSet

val ASTNode.isComment: Boolean
    get() = this.kind in TypstSyntaxKindUtils.commentSet
