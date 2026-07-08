package com.github.ixmoyren.typilot.highlight

import com.github.ixmoyren.typilot.psi.TypstFuncCallPsiElement
import com.github.ixmoyren.typilot.psi.TypstLinkFuncPsiElement
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement

class TypstHighlightAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element) {
            is TypstLinkFuncPsiElement -> annotateLinkFunc(element, holder)
            is TypstFuncCallPsiElement -> annotateFuncCall(element, holder)
        }
    }

    private fun annotateLinkFunc(element: TypstLinkFuncPsiElement, holder: AnnotationHolder) {
        annotateFuncCall(element, holder)
        val urlPsi = element.urlPsiElement() ?: return
        if (urlPsi.text.isNullOrBlank()) return
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(urlPsi.textRange).textAttributes(TypstHighlightingColors.LINKS.key).create()
    }

    private fun annotateFuncCall(element: TypstFuncCallPsiElement, holder: AnnotationHolder) {
        val identPsi = element.getIndentPsiElement() ?: return
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(identPsi.textRange).textAttributes(TypstHighlightingColors.FUNCTION.key).create()
    }
}
