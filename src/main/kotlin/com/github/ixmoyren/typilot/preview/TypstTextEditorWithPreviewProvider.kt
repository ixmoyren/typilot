package com.github.ixmoyren.typilot.preview

import com.github.ixmoyren.typilot.language.TypstFileType
import com.intellij.openapi.fileEditor.TextEditorWithPreviewProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class TypstTextEditorWithPreviewProvider : TextEditorWithPreviewProvider(TypstPreviewFileEditorProvider()), DumbAware {
    override fun accept(project: Project, file: VirtualFile): Boolean =
        file.fileType == TypstFileType || file.extension.equals(TypstFileType.defaultExtension, ignoreCase = true)
}
