package com.github.ixmoyren.typilot.navigation

import com.github.ixmoyren.typalize.TypstSyntaxKind
import com.github.ixmoyren.typilot.psi.TypstClosurePsiElement
import com.github.ixmoyren.typilot.psi.TypstDestructuringPsiElement
import com.github.ixmoyren.typilot.psi.TypstForLoopPsiElement
import com.github.ixmoyren.typilot.psi.TypstFuncCallPsiElement
import com.github.ixmoyren.typilot.psi.TypstImportItemPathPsiElement
import com.github.ixmoyren.typilot.psi.TypstLetBindingPsiElement
import com.github.ixmoyren.typilot.psi.TypstParamsPsiElement
import com.github.ixmoyren.typilot.psi.TypstRenamedImportItemPsiElement
import com.github.ixmoyren.typilot.psi.TypstSetRulePsiElement
import com.github.ixmoyren.typilot.psi.TypstShowRulePsiElement
import com.github.ixmoyren.typilot.psi.TypstSpreadPsiElement
import com.github.ixmoyren.typilot.psi.TypstTokenType
import com.intellij.navigation.DirectNavigationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementWalkingVisitor

/**
 * Provides "Go To Declaration" for the built-in Typst functions that `tinymist` cannot resolve.
 *
 * `tinymist` returns no definition for standard library functions (`text`, `page`, `emph`, ...) because they are not declared in any source file, so navigating to their
 * documentation page is the closest equivalent of jumping to a definition. The provider only takes over when the identifier is a function call of a documented built-in and that
 * name is not declared in the current file, so user-defined and imported functions keep their regular LSP navigation.
 */
class TypstBuiltinNavigationProvider : DirectNavigationProvider {

    override fun getNavigationElement(element: PsiElement): PsiElement? {
        if (!isBuiltinFunctionIdentifier(element)) return null
        val url = TypstBuiltinDocumentation.urlFor(element.text) ?: return null
        if (isDeclaredInFile(element.containingFile, element.text)) return null
        return TypstBuiltinDocumentationPsiElement(element, url)
    }

    /** True when [element] names a function in a call such as `text(...)`, or a set/show rule such as `set text(...)` and `show text: ...`. */
    private fun isBuiltinFunctionIdentifier(element: PsiElement): Boolean {
        val kind = (element.node?.elementType as? TypstTokenType)?.kind
        if (kind != TypstSyntaxKind.Ident()) return false
        return when (val parent = element.parent) {
            is TypstFuncCallPsiElement -> parent.getIndentPsiElement()?.textRange == element.textRange

            is TypstSetRulePsiElement,
            is TypstShowRulePsiElement -> firstIdentChild(parent)?.textRange == element.textRange

            else -> false
        }
    }

    private fun firstIdentChild(element: PsiElement): PsiElement? =
        element.node?.getChildren(null)?.firstOrNull { (it.elementType as? TypstTokenType)?.kind == TypstSyntaxKind.Ident() }?.psi

    /**
     * Checks whether [name] is bound anywhere in [file] by a `let`, parameter, loop variable, destructuring pattern or import, so that a local or imported function is not shadowed
     * by the built-in documentation.
     */
    private fun isDeclaredInFile(file: PsiFile?, name: String): Boolean {
        if (file == null) return false
        var declared = false
        file.accept(
            object : PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: PsiElement) {
                    if (declared) return
                    if (declaresName(element, name)) {
                        declared = true
                        return
                    }
                    super.visitElement(element)
                }
            })
        return declared
    }

    private fun declaresName(element: PsiElement, name: String): Boolean =
        when (element) {
            is TypstLetBindingPsiElement,
            is TypstClosurePsiElement,
            is TypstParamsPsiElement,
            is TypstDestructuringPsiElement,
            is TypstForLoopPsiElement,
            is TypstSpreadPsiElement -> directIdentNames(element).contains(name)

            is TypstImportItemPathPsiElement -> element.parent !is TypstRenamedImportItemPsiElement && directIdentNames(element).firstOrNull() == name

            is TypstRenamedImportItemPsiElement -> directIdentNames(element).lastOrNull() == name

            else -> false
        }

    /** The names of the identifier tokens that are direct children of [element]. `PsiElement.getChildren()` skips leaf tokens, so the AST node is walked directly. */
    private fun directIdentNames(element: PsiElement): List<String> =
        element.node?.getChildren(null)?.mapNotNull { child -> if ((child.elementType as? TypstTokenType)?.kind == TypstSyntaxKind.Ident()) child.text else null } ?: emptyList()
}
