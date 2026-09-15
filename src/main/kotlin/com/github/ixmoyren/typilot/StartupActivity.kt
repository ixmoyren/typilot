package com.github.ixmoyren.typilot

import com.github.ixmoyren.typilot.lsp.TypstLspIntegrationProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.platform.lsp.api.LspClientManager

class StartupActivity : ProjectActivity, DumbAware {
    override suspend fun execute(project: Project) {
        // Starts the built-in LSP client for the Typst files that are already open at startup.
        LspClientManager.getInstance(project).startClientsIfNeeded(TypstLspIntegrationProvider::class.java)
    }
}
