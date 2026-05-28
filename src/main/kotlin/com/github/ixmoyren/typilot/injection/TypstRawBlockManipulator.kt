package com.github.ixmoyren.typilot.injection

import com.github.ixmoyren.typilot.psi.TypstRawBlockPsiElement
import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil

class TypstRawBlockManipulator : AbstractElementManipulator<TypstRawBlockPsiElement>() {
    override fun handleContentChange(element: TypstRawBlockPsiElement, range: TextRange, newContent: String): TypstRawBlockPsiElement {
        val newText = range.replace(element.text, newContent)
        val type = element.containingFile.fileType
        val newFile = PsiFileFactory.getInstance(element.project).createFileFromText("new." + type.defaultExtension, type, newText)
        val newElement = PsiTreeUtil.findChildOfType(newFile, TypstRawBlockPsiElement::class.java)!!
        return element.replace(newElement) as TypstRawBlockPsiElement
    }

    override fun getRangeInElement(element: TypstRawBlockPsiElement): TextRange {
        return element.getContentRange()
    }
}
