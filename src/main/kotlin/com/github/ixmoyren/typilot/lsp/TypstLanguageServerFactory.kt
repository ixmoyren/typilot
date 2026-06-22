package com.github.ixmoyren.typilot.lsp

import com.intellij.openapi.project.Project
import com.redhat.devtools.lsp4ij.LanguageServerFactory
import com.redhat.devtools.lsp4ij.server.StreamConnectionProvider

class TypstLanguageServerFactory : LanguageServerFactory {
    private val installer = TinymistInstaller()

    override fun createConnectionProvider(project: Project): StreamConnectionProvider =
        TinymistStreamConnectionProvider(
            project,
            listOf(
                TinymistHelper(),
                TinymistInstallerLocator(installer),
            )
        )
}