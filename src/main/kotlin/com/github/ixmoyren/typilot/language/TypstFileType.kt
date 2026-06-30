package com.github.ixmoyren.typilot.language

import com.github.ixmoyren.typilot.TypilotBundle
import com.intellij.openapi.fileTypes.LanguageFileType
import org.jetbrains.annotations.NonNls
import javax.swing.Icon

object TypstFileType : LanguageFileType(TypstLanguage.INSTANCE) {
    override fun getName(): @NonNls String = "Typst"

    override fun getDescription(): @NonNls String = TypilotBundle["fileType.typst.description"]

    override fun getDefaultExtension(): @NonNls String = "typ"

    override fun getIcon(): Icon = TypstFileIcon.FILE
}
