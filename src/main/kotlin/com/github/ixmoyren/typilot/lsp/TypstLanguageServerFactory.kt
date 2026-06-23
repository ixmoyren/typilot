package com.github.ixmoyren.typilot.lsp

import com.github.ixmoyren.typilot.lsp.services.TinymistLocatorService
import com.intellij.openapi.project.Project
import com.redhat.devtools.lsp4ij.LanguageServerFactory
import com.redhat.devtools.lsp4ij.installation.ServerInstaller
import com.redhat.devtools.lsp4ij.server.StreamConnectionProvider

class TypstLanguageServerFactory : LanguageServerFactory {

    override fun createConnectionProvider(project: Project): StreamConnectionProvider =
        TinymistStreamConnectionProvider(project, TinymistLocatorService.getInstance().firstValidLocator)

    override fun createServerInstaller(): ServerInstaller = TinymistLocatorService.getInstance().installer
}
