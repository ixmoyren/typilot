package com.github.ixmoyren.typilot.lsp

import com.github.ixmoyren.typilot.language.TypstFileType
import com.github.ixmoyren.typilot.lsp.config.TinymistServerConfiguration
import com.github.ixmoyren.typilot.lsp.services.TinymistLocateService
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspClient
import com.intellij.platform.lsp.api.LspServerListener
import com.intellij.platform.lsp.api.ProjectWideLspClientDescriptor
import com.intellij.platform.lsp.api.customization.LspCodeLensCustomizer
import com.intellij.platform.lsp.api.customization.LspCodeLensSupport
import com.intellij.platform.lsp.api.customization.LspCustomization
import com.intellij.platform.lsp.api.customization.LspSemanticTokensCustomizer
import com.intellij.platform.lsp.api.customization.LspSemanticTokensSupport
import com.intellij.psi.PsiFile
import com.intellij.util.execution.ParametersListUtil
import java.awt.event.MouseEvent
import org.eclipse.lsp4j.Command
import org.eclipse.lsp4j.ConfigurationItem
import org.eclipse.lsp4j.InitializeResult

/**
 * Describes how the built-in LSP client starts and talks to the `tinymist` language server.
 *
 * The server is project-wide: a single `tinymist` process serves every Typst file of the project.
 */
class TypstLspClientDescriptor(project: Project) : ProjectWideLspClientDescriptor(project, "Typst Language Server") {

    override fun isSupportedFile(file: VirtualFile): Boolean = file.fileType == TypstFileType

    override fun getLanguageId(file: VirtualFile): String = "typst"

    override fun createCommandLine(): GeneralCommandLine {
        val command = TinymistLocateService.getInstance().firstValidLocator?.locate()
        if (command.isNullOrBlank()) {
            throw ExecutionException(
                "Could not locate the tinymist language server. Configure its path in Settings | Tools | Typst, install it on PATH, or download it from the settings page.")
        }
        return GeneralCommandLine(ParametersListUtil.parse(command)).withCharset(Charsets.UTF_8)
    }

    /** `tinymist` accepts its configuration as the LSP `initializationOptions` object, which is confirmed by the server log line `config update_by_map { ... }`. */
    override fun createInitializationOptions(): Any = TinymistServerConfiguration.parseForLsp()

    /** `tinymist` does not use pull-based configuration, but answering `workspace/configuration` keeps the implementation correct for servers that do. */
    override fun getWorkspaceConfiguration(item: ConfigurationItem): Any = TinymistServerConfiguration.parseForLsp()

    override val lspCustomization: LspCustomization =
        object : LspCustomization() {
            override val codeLensCustomizer: LspCodeLensCustomizer = TinymistCodeLensCustomizer()

            override val semanticTokensCustomizer: LspSemanticTokensCustomizer =
                object : LspSemanticTokensSupport() {
                    override fun shouldAskServerForSemanticTokens(psiFile: PsiFile): Boolean = psiFile.fileType == TypstFileType
                }
        }

    override val lspServerListener: LspServerListener =
        object : LspServerListener {
            override fun serverInitialized(params: InitializeResult) {
                TinymistLspService.getInstance(project).serverStarted()
            }
        }
}

/**
 * `tinymist` returns its `Export PDF` code lens with the document URI as the only argument, which the server itself cannot resolve. Rebuild the command with the document path and
 * the options the server expects.
 */
private class TinymistCodeLensCustomizer : LspCodeLensSupport() {
    override fun codeLensClicked(lspClient: LspClient, contextFile: VirtualFile, command: Command, mouseEvent: MouseEvent?) {
        if (command.command == TinymistCommands.EXPORT_PDF_COMMAND) {
            TinymistCommands.exportPdf(lspClient.project, contextFile)
        } else {
            super.codeLensClicked(lspClient, contextFile, command, mouseEvent)
        }
    }
}
