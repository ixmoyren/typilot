package com.github.ixmoyren.typilot.highlight

import com.github.ixmoyren.typalize.TypstSyntaxKind
import com.github.ixmoyren.typilot.psi.TypstFuncCallPsiElement
import com.github.ixmoyren.typilot.psi.getTypstSyntaxKind
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.psi.PsiElement
import com.intellij.util.io.URLUtil

class TypstHighlightAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element.firstChild != null) return
        val kind = element.getTypstSyntaxKind() ?: return
        when (kind) {
            is TypstSyntaxKind.Ident -> annotateIdent(element, holder)
            is TypstSyntaxKind.Str if (isUrlStr(element)) -> annotateUrlStr(element, holder)
            else -> return
        }
    }

    private fun isUrlStr(element: PsiElement): Boolean {
        val text = element.text ?: return false
        return URLUtil.URL_PATTERN.matcher(text).find()
    }

    private fun annotateIdent(element: PsiElement, holder: AnnotationHolder) {
        val parent = element.parent ?: return
        when (parent) {
            is TypstFuncCallPsiElement ->
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(element.textRange).textAttributes(TypstHighlightingColors.FUNCTION.key).create()
        }
    }

    private fun annotateUrlStr(element: PsiElement, holder: AnnotationHolder) {
        val attrs = EditorColorsManager.getInstance().globalScheme.getAttributes(TypstHighlightingColors.LINKS.key)
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(element.textRange).enforcedTextAttributes(attrs).create()
    }
}
