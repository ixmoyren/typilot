package com.github.ixmoyren.typilot.highlight

import com.github.ixmoyren.typilot.psi.TypstCompositeElement
import com.github.ixmoyren.typilot.psi.TypstLeafElement
import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement

class TypstHighlightAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (!element.isValid) return
        val tag =
            when (element) {
                is TypstCompositeElement -> element.tag
                is TypstLeafElement -> element.tag
                else -> null
            } ?: return
        val attrKey = tag.Color
        holder
            .newSilentAnnotation(HighlightInfoType.SYMBOL_TYPE_SEVERITY)
            .range(element.textRange)
            .textAttributes(attrKey)
            .create()
    }
}
