package com.github.ixmoyren.typilot.navigation

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.FakePsiElement

/**
 * A synthetic navigation target for a Typst built-in: navigating to it opens the built-in's page in the official documentation.
 *
 * It wraps the identifier the user invoked "Go To Declaration" on, so that text range, parent file and project are those of the real element.
 */
class TypstBuiltinDocumentationPsiElement(
    private val context: PsiElement,
    private val url: String,
) : FakePsiElement() {

    override fun getParent(): PsiElement = context

    override fun getTextRange(): TextRange = context.textRange ?: TextRange.EMPTY_RANGE

    override fun getPresentableText(): String = url

    override fun getName(): String = url

    override fun canNavigate(): Boolean = context.isValid

    /** Navigation opens a browser, not an editor, so the platform must treat this as a non-source navigation. */
    override fun canNavigateToSource(): Boolean = false

    override fun navigate(requestFocus: Boolean) {
        BrowserUtil.browse(url)
    }

    override fun isValid(): Boolean = context.isValid
}
