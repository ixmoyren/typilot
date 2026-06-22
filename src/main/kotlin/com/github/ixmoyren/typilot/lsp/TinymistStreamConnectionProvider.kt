package com.github.ixmoyren.typilot.lsp

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.util.execution.ParametersListUtil
import com.redhat.devtools.lsp4ij.server.OSProcessStreamConnectionProvider

class TinymistStreamConnectionProvider(
    private val project: Project,
    private val locators: List<TinymistLocator>
) : OSProcessStreamConnectionProvider() {
    private val logger = Logger.getInstance(TinymistStreamConnectionProvider::class.java)

    override fun start() {
        val path = locators.firstNotNullOfOrNull { it.locate() }
        if (path == null) {
            logger.error("Could not locate Tinymist")
            return
        }
        val parts = ParametersListUtil.parse(path)
        commandLine = GeneralCommandLine(parts)
        super.start()
    }
}