package com.github.ixmoyren.typilot.psi

import com.github.ixmoyren.typilot.language.TypstFileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.descendantsOfType

class TypstPsiFactory(private val project: Project) {
    fun createIdent(name: String): TypstIdentElement {
        val code = "#let $name = none"
        val file = createFile(code)
        return file.descendantsOfType<TypstIdentElement>().first()
    }

    fun createRef(labelName: String): TypstRefElement = createFile("@$labelName").descendantsOfType<TypstRefElement>().first()

    fun createFile(text: String): TypstPsiFile {
        return PsiFileFactory.getInstance(project).createFileFromText("temporary.typ", TypstFileType, text) as TypstPsiFile
    }
}
