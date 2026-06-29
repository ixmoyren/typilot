package com.github.ixmoyren.typilot.lsp

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.util.execution.ParametersListUtil
import com.redhat.devtools.lsp4ij.server.OSProcessStreamConnectionProvider

class TinymistStreamConnectionProvider(private val project: Project, private val locator: TinymistLocator?) :
    OSProcessStreamConnectionProvider() {
    private val logger = Logger.getInstance(TinymistStreamConnectionProvider::class.java)

    override fun start() {
        val locator = locator ?: run { logger.error("Could not get TinymistLocator"); return }
        val path = locator.locate()
            ?: run { logger.error("Could not locate Tinymist"); return }
        commandLine = GeneralCommandLine(ParametersListUtil.parse(path))
        super.start()
    }
}
