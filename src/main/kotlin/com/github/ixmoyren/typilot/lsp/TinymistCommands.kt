package com.github.ixmoyren.typilot.lsp

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspClient
import com.intellij.platform.lsp.api.LspClientManager
import com.intellij.platform.lsp.api.LspServerState
import org.eclipse.lsp4j.ExecuteCommandParams

/** Executes the custom `tinymist` commands that are not covered by the standard LSP feature set (PDF export and the preview server) through the built-in LSP client. */
object TinymistCommands {
    const val EXPORT_PDF_COMMAND: String = "tinymist.exportPdf"
    const val START_PREVIEW_COMMAND: String = "tinymist.doStartPreview"
    private const val KILL_PREVIEW_COMMAND: String = "tinymist.doKillPreview"

    private const val DEFAULT_CLIENT_WAIT_MS: Long = 30_000
    private const val PREVIEW_REQUEST_TIMEOUT_MS: Int = 120_000
    private const val EXPORT_PDF_TIMEOUT_MS: Int = 120_000

    private val logger = logger<TinymistCommands>()

    /** Returns the running `tinymist` LSP client of [project], or `null` when none is running yet. */
    fun findRunningClient(project: Project): LspClient? {
        if (project.isDisposed) return null
        return LspClientManager.getInstance(project).getClients(TypstLspIntegrationProvider::class.java).firstOrNull { it.state == LspServerState.Running }
    }

    /** Exports [file] to PDF in the background. */
    fun exportPdf(project: Project, file: VirtualFile) {
        ApplicationManager.getApplication().executeOnPooledThread {
            val client = findRunningClient(project)
            if (client == null) {
                logger.warn("No running tinymist language server, cannot export PDF for ${file.path}")
                return@executeOnPooledThread
            }
            val result = client.sendRequestSync(EXPORT_PDF_TIMEOUT_MS) { it.workspaceService.executeCommand(exportPdfParams(file)) }
            if (result == null) {
                logger.warn("tinymist export PDF returned no result for ${file.path}")
            } else {
                logger.info("tinymist export PDF finished for ${file.path}: $result")
            }
        }
    }

    /**
     * Starts the tinymist preview server for [file]. Must be called from a background thread.
     *
     * @return the raw `ExecuteCommand` result, which contains the preview server address.
     */
    fun startPreview(project: Project, file: VirtualFile, taskId: String): Any? {
        val client = awaitRunningClient(project) ?: return null
        val previewArguments = listOf("--task-id", taskId, "--data-plane-host", "127.0.0.1:0", file.path)
        return client.sendRequestSync(PREVIEW_REQUEST_TIMEOUT_MS) {
            it.workspaceService.executeCommand(ExecuteCommandParams(START_PREVIEW_COMMAND, listOf(previewArguments)))
        }
    }

    /** Stops the preview server with the given [taskId]. */
    fun killPreview(project: Project, taskId: String) {
        val client = findRunningClient(project) ?: return
        client.sendNotification {
            it.workspaceService.executeCommand(ExecuteCommandParams(KILL_PREVIEW_COMMAND, listOf(taskId)))
        }
    }

    private fun exportPdfParams(file: VirtualFile): ExecuteCommandParams =
        ExecuteCommandParams(EXPORT_PDF_COMMAND, listOf(file.path, emptyMap<String, Any>(), mapOf<String, Any>("open" to false)))

    /** Waits until the project-wide `tinymist` client is running. Asks the platform to start it first, so the preview works even when it is the only editor opened for the file. */
    private fun awaitRunningClient(project: Project, timeoutMs: Long = DEFAULT_CLIENT_WAIT_MS): LspClient? {
        findRunningClient(project)?.let {
            return it
        }

        if (!project.isDisposed) {
            LspClientManager.getInstance(project).ensureClientStarted(TypstLspIntegrationProvider::class.java, TypstLspClientDescriptor(project))
        }

        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            findRunningClient(project)?.let {
                return it
            }
            Thread.sleep(200)
        }
        logger.warn("Timed out waiting for the tinymist language server to start")
        return null
    }
}
