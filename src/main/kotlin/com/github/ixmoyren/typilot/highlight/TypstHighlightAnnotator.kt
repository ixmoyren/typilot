package com.github.ixmoyren.typilot.highlight

import com.github.ixmoyren.typilot.psi.TypstLinkFuncPsiElement
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement

class TypstHighlightAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is TypstLinkFuncPsiElement) return
        val urlPsi = element.urlPsiElement() ?: return
        if (urlPsi.text.isNullOrBlank()) return
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(urlPsi.textRange).textAttributes(TypstHighlightingColors.LINKS.key).create()
    }
}
