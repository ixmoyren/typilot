package com.github.ixmoyren.typilot.settings

import com.github.ixmoyren.typilot.lsp.TypstLspIntegrationProvider
import com.github.ixmoyren.typilot.lsp.services.TinymistLocateService
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.ProjectManager
import com.intellij.platform.lsp.api.LspClientManager
import javax.swing.JComponent

class TinymistConfigurable : Configurable {
    private var settingsForm: TinymistSettingsForm? = null

    private val settings = TinymistSettings.getInstance()

    override fun getDisplayName(): String = "Typst"

    override fun createComponent(): JComponent? {
        settingsForm = settingsForm ?: TinymistSettingsForm()
        return settingsForm
    }

    override fun isModified(): Boolean {
        return settingsForm?.run { settings.tinymistPath != tinymistPath.get() || settings.serverConfiguration != serverConfiguration.get() } ?: false
    }

    override fun apply() {
        val form = settingsForm ?: return
        val previousPath = settings.tinymistPath
        val previousConfiguration = settings.serverConfiguration

        settings.tinymistPath = form.tinymistPath.get()
        settings.serverConfiguration = form.serverConfiguration.get()

        if (previousPath != settings.tinymistPath || previousConfiguration != settings.serverConfiguration) {
            TinymistLocateService.getInstance().invalidate()
            restartLanguageServers()
        }
    }

    override fun reset() {
        settingsForm?.reset()
    }

    override fun disposeUIResources() {
        settingsForm = null
    }

    /** Restarts the built-in LSP clients so a changed tinymist path or configuration takes effect immediately. */
    private fun restartLanguageServers() {
        for (project in ProjectManager.getInstance().openProjects) {
            if (project.isDefault) continue
            LspClientManager.getInstance(project).stopAndRestartClientsIfNeeded(TypstLspIntegrationProvider::class.java)
        }
    }
}
