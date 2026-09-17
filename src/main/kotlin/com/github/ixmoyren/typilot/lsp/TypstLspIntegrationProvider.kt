package com.github.ixmoyren.typilot.lsp

import com.github.ixmoyren.typilot.language.TypstFileIcon
import com.github.ixmoyren.typilot.language.TypstFileType
import com.github.ixmoyren.typilot.settings.TinymistConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspClient
import com.intellij.platform.lsp.api.LspIntegrationProvider
import com.intellij.platform.lsp.api.lsWidget.LspClientWidgetItem

/** Starts the `tinymist` language server through the IntelliJ Platform's built-in LSP client whenever a Typst file is opened in the editor. */
class TypstLspIntegrationProvider : LspIntegrationProvider {

    override fun fileOpened(project: Project, file: VirtualFile, clientStarter: LspIntegrationProvider.LspClientStarter) {
        if (isTypstFile(file)) {
            clientStarter.ensureClientStarted(TypstLspClientDescriptor(project))
        }
    }

    override fun createWidgetItem(lspClient: LspClient, currentFile: VirtualFile?): LspClientWidgetItem =
        LspClientWidgetItem(lspClient, currentFile, TypstFileIcon.FILE, TinymistConfigurable::class.java)
}

fun isTypstFile(file: VirtualFile): Boolean = file.fileType == TypstFileType
