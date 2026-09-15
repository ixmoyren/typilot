package com.github.ixmoyren.typilot

import com.github.ixmoyren.typilot.language.TypstFileType
import com.github.ixmoyren.typilot.navigation.TypstBuiltinDocumentation
import com.github.ixmoyren.typilot.navigation.TypstBuiltinDocumentationPsiElement
import com.github.ixmoyren.typilot.navigation.TypstBuiltinNavigationProvider
import com.github.ixmoyren.typilot.psi.TypstNamedPairPsiElement
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.platform.backend.navigation.NavigationRequest
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class TypstBuiltinNavigationTest : BasePlatformTestCase() {

    private val provider = TypstBuiltinNavigationProvider()

    fun testDocumentationMapContainsTypstBuiltins() {
        assertEquals("https://typst.app/docs/reference/text/text/", TypstBuiltinDocumentation.urlFor("text"))
        assertEquals("https://typst.app/docs/reference/layout/page/", TypstBuiltinDocumentation.urlFor("page"))
        assertEquals("https://typst.app/docs/reference/model/emph/", TypstBuiltinDocumentation.urlFor("emph"))
        assertNull(TypstBuiltinDocumentation.urlFor("myfunc"))
    }

    fun testNavigatesBuiltinFunctionToDocumentation() {
        val file = configure("#text(size: 10pt)[hello]\n#page(width: 5cm)")

        val target = navigationFor(file, "text")
        assertNotNull("a built-in function must navigate to its documentation", target)
        assertEquals("https://typst.app/docs/reference/text/text/", target!!.presentableText)
        assertTrue(target.canNavigate())
    }

    fun testNavigatesBuiltinInSetRule() {
        val file = configure("#set text(size: 10pt)")

        assertNotNull("a set rule must navigate to the built-in documentation", navigationFor(file, "text"))
    }

    fun testNavigatesBuiltinInShowRule() {
        val file = configure("#show emph: it => it")

        assertNotNull("a show rule must navigate to the built-in documentation", navigationFor(file, "emph"))
    }

    fun testDocumentationTargetProvidesNavigationRequest() {
        val file = configure("#text(size: 10pt)[hello]")
        val target = navigationFor(file, "text")
        assertNotNull(target)
        assertTrue(target!!.canNavigate())
        assertFalse("opening documentation is not a source navigation", target.canNavigateToSource())

        val request =
            ApplicationManager.getApplication()
                .executeOnPooledThread<NavigationRequest?> { ReadAction.nonBlocking<NavigationRequest?> { target.navigationRequest() }.executeSynchronously() }
                .get()
        assertNotNull("the documentation target must provide a navigation request", request)
    }

    fun testDoesNotNavigateUserDefinedFunction() {
        val file = configure("#let myfunc(x) = x + 1\n#myfunc(3)")

        assertNull(navigationFor(file, "myfunc"))
    }

    fun testDoesNotShadowLocallyDeclaredBuiltin() {
        val file = configure("#let text = 1\n#text(size: 10pt)[hello]")

        assertNull("a locally declared name must keep its LSP definition", navigationFor(file, "text"))
    }

    fun testDoesNotShadowFunctionParameter() {
        val file = configure("#let f(text) = text(1)\n#f(2)")

        assertNull("a parameter must keep its LSP definition", navigationFor(file, "text"))
    }

    fun testDoesNotShadowImportedBuiltin() {
        val file = configure("#import \"other.typ\": page\n#page(width: 5cm)")

        assertNull("an imported name must keep its LSP definition", navigationFor(file, "page"))
    }

    fun testDoesNotNavigateNamedArgument() {
        val file = configure("#text(size: 10pt)[hello]")

        val namedPair = PsiTreeUtil.findChildOfType(file, TypstNamedPairPsiElement::class.java)
        assertNotNull(namedPair)
        assertNull(provider.getNavigationElement(namedPair!!.firstChild))
    }

    private fun configure(text: String): PsiFile = myFixture.configureByText(TypstFileType, text)

    /** Runs the provider on every element named [functionName] and returns the first documentation target it finds. */
    private fun navigationFor(file: PsiFile, functionName: String): TypstBuiltinDocumentationPsiElement? {
        var found: TypstBuiltinDocumentationPsiElement? = null
        file.accept(
            object : PsiRecursiveElementVisitor() {
                override fun visitElement(element: PsiElement) {
                    if (found == null && element.text == functionName) {
                        found = provider.getNavigationElement(element) as? TypstBuiltinDocumentationPsiElement
                    }
                    if (found == null) super.visitElement(element)
                }
            })
        return found
    }
}
