package com.github.ixmoyren.typilot.lsp

import com.intellij.codeInsight.codeVision.CodeVisionEntry
import com.intellij.codeInsight.codeVision.ui.model.ClickableTextCodeVisionEntry
import com.redhat.devtools.lsp4ij.client.features.LSPCodeLensFeature
import org.eclipse.lsp4j.CodeLens
import java.util.*

@Suppress("UnstableApiUsage")
class TinymistCodeLensFeature : LSPCodeLensFeature() {
    override fun createCodeVisionEntry(
        codeLens: CodeLens,
        providerId: String,
        codeLensContext: LSPCodeLensContext
    ): CodeVisionEntry? {
        val command = codeLens.command
        if (command != null && command.command == EXPORT_PDF_COMMAND) {
            val text = getText(codeLens) ?: return null
            val languageServer = codeLensContext.languageServer
            val currentFile = codeLensContext.psiFile.virtualFile ?: return null

            return ClickableTextCodeVisionEntry(text, providerId, { _, editor ->
                val project = editor.project ?: return@ClickableTextCodeVisionEntry
                TinymistExportPdfHandler.perform(project, currentFile, languageServer, null)
            }, null, text, text, Collections.emptyList())
        }
        return super.createCodeVisionEntry(codeLens, providerId, codeLensContext)
    }

    companion object {
        private const val EXPORT_PDF_COMMAND = "tinymist.exportPdf"
    }
}