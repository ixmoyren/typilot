package com.github.ixmoyren.typilot.lsp

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.redhat.devtools.lsp4ij.LanguageServerItem
import com.redhat.devtools.lsp4ij.commands.CommandExecutor
import com.redhat.devtools.lsp4ij.commands.LSPCommandContext
import org.eclipse.lsp4j.Command

object TinymistExportPdfHandler {
    fun perform(
        project: Project,
        currentFile: VirtualFile,
        languageServer: LanguageServerItem?,
        languageServerId: String?
    ) {
        val exportCommand = Command(
            "Export PDF",
            "tinymist.exportPdf",
            listOf(currentFile.path, emptyMap<String, Any>(), mapOf("open" to false))
        )
        val context = LSPCommandContext(exportCommand, project).setPreferredLanguageServer(languageServer)
            .setPreferredLanguageServerId(languageServerId)
        CommandExecutor.executeCommand(context)
    }
}
