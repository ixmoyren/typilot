package com.github.ixmoyren.typilot.preview

import com.github.ixmoyren.typilot.language.TypstFileType
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class TypstPreviewFileEditorProvider : FileEditorProvider, DumbAware {
    override fun accept(project: Project, file: VirtualFile): Boolean = file.fileType == TypstFileType

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        return TypstPreviewFileEditor(project, file)
    }

    override fun getEditorTypeId(): String = "tinymist-preview-editor"

    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR
}
