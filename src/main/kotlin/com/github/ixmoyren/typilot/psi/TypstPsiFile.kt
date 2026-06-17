package com.github.ixmoyren.typilot.psi

import com.github.ixmoyren.typilot.language.TypstFileType
import com.github.ixmoyren.typilot.language.TypstLanguage
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

class TypstPsiFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, TypstLanguage.INSTANCE) {

    override fun getFileType(): FileType = TypstFileType

    override fun toString(): String = "TypstPsiFile: ${virtualFile?.name ?: "<unknown>"}"
}
