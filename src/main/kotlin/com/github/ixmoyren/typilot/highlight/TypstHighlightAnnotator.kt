package com.github.ixmoyren.typilot.highlight

import com.github.ixmoyren.typilot.psi.TypstPsiElement
import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement

class TypstHighlightAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is TypstPsiElement) return
        if (element.children.isNotEmpty()) return
        val annotationBuilder = holder.newSilentAnnotation(HighlightInfoType.SYMBOL_TYPE_SEVERITY).range(element.textRange)
        annotationBuilder.create()
    }
}
