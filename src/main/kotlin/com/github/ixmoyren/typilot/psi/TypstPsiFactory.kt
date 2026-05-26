package com.github.ixmoyren.typilot.psi

import com.github.ixmoyren.typilot.language.TypstFileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.descendantsOfType

class TypstPsiFactory(private val project: Project) {
    fun createRawBlock(lang: String, content: String): TypstRawBlockPsiElement {
        val code = "```$lang\n$content\n```"
        return createFile(code)
            .descendantsOfType<TypstRawBlockPsiElement>()
            .first()
    }

    fun createFile(text: String): TypstPsiFile {
        return PsiFileFactory.getInstance(project)
            .createFileFromText("temporary.typ", TypstFileType, text) as TypstPsiFile
    }
}
