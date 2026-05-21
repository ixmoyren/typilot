package com.github.ixmoyren.typilot.language

import com.github.ixmoyren.typilot.TypilotBundle
import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon
import org.jetbrains.annotations.NonNls

object TypstFileType : LanguageFileType(TypstLanguage.INSTANCE) {
    override fun getName(): @NonNls String {
        return "Typst"
    }

    override fun getDescription(): @NonNls String {
        return TypilotBundle["fileType.typst.description"]
    }

    override fun getDefaultExtension(): @NonNls String {
        return "typ"
    }

    override fun getIcon(): Icon {
        return TypstFileIcon.FILE
    }
}
