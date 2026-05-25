package com.github.ixmoyren.typilot.psi

import com.github.ixmoyren.typilot.TypstSyntaxKind
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.PsiTreeUtil

interface TypstPsiElement : PsiElement

interface TypstNamedElement : TypstPsiElement, PsiNamedElement

open class TypstLeafElement(type: TypstElementType, text: CharSequence) : LeafPsiElement(type, text), TypstPsiElement

class TypstIdentElement(type: TypstElementType, text: CharSequence) : TypstLeafElement(type, text) {
    override fun getName(): String = this.text
}

class TypstKeywordElement(type: TypstElementType, text: CharSequence) : TypstLeafElement(type, text)

class TypstCommentElement(type: TypstElementType, text: CharSequence) : TypstLeafElement(type, text), PsiComment {
    override fun getTokenType() = elementType
}

open class TypstCompositeElement(node: ASTNode) : ASTWrapperPsiElement(node), TypstPsiElement

class TypstLetBindingElement(node: ASTNode) : TypstCompositeElement(node), TypstNamedElement {
    override fun getName(): String? = findChildByType<TypstIdentElement>(TypstSyntaxKind.IDENT.elementType)?.text

    override fun setName(name: String): PsiElement = apply {
        findChildByType<TypstIdentElement>(TypstSyntaxKind.IDENT.elementType)?.replace(TypstPsiFactory(project).createIdent(name))
    }
}

class TypstFuncCallElement(node: ASTNode) : TypstCompositeElement(node) {
    val callee: PsiElement?
        get() = firstChild
}

class TypstFieldAccessElement(node: ASTNode) : TypstCompositeElement(node)

class TypstClosureElement(node: ASTNode) : TypstCompositeElement(node)

class TypstModuleImportElement(node: ASTNode) : TypstCompositeElement(node)

class TypstModuleIncludeElement(node: ASTNode) : TypstCompositeElement(node)

class TypstRefElement(node: ASTNode) : TypstCompositeElement(node), PsiReference {
    override fun getReference() = this

    override fun getElement(): PsiElement = this

    override fun getRangeInElement(): TextRange {
        val refMarker = findChildByType<PsiElement>(TypstSyntaxKind.REF_MARKER.elementType) ?: return TextRange(0, textLength)
        val markerStart = refMarker.startOffsetInParent
        return TextRange(markerStart + 1, markerStart + refMarker.textLength)
    }

    override fun getCanonicalText(): String = findChildByType<PsiElement>(TypstSyntaxKind.REF_MARKER.elementType)?.text?.removePrefix("@") ?: ""

    override fun resolve(): PsiElement? {
        val targetName = getCanonicalText()
        if (targetName.isEmpty()) return null
        val file = element.containingFile ?: return null
        return PsiTreeUtil.collectElementsOfType(file, TypstLabelElement::class.java).firstOrNull { it.getName() == targetName }
    }

    override fun isReferenceTo(element: PsiElement): Boolean {
        if (element !is TypstLabelElement) return false
        if (element.getName() != getCanonicalText()) return false
        return manager.areElementsEquivalent(resolve(), element)
    }

    override fun handleElementRename(newElementName: String): PsiElement {
        val newRef = TypstPsiFactory(project).createRef(newElementName)
        return replace(newRef)
    }

    override fun bindToElement(element: PsiElement): PsiElement {
        if (element is TypstLabelElement) {
            val newName = element.getName()
            return handleElementRename(newName)
        }
        return this
    }

    override fun isSoft() = true

    override fun clone(): Any = super.clone()
}

class TypstLabelElement(node: ASTNode) : TypstCompositeElement(node), TypstNamedElement {
    override fun getName() = text.removeSurrounding("<", ">")

    override fun setName(name: String): PsiElement = this
}

class TypstHeadingElement(node: ASTNode) : TypstCompositeElement(node)

class TypstSetRuleElement(node: ASTNode) : TypstCompositeElement(node)

class TypstShowRuleElement(node: ASTNode) : TypstCompositeElement(node)
