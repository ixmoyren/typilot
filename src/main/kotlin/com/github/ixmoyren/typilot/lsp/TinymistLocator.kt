package com.github.ixmoyren.typilot.lsp

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil

fun interface TinymistLocator {
    fun locate(): String?

    fun version(): String? = locate()?.let { path ->
        val commandLine = GeneralCommandLine(path, "-V").apply { withCharset(Charsets.UTF_8) }
        return ExecUtil.execAndGetOutput(commandLine, 30_000).takeIf { it.exitCode == 0 }?.stdout?.trim()
    }
}

class TinymistInstallerLocator(private val installer: TinymistInstaller) : TinymistLocator {
    override fun locate(): String? = installer.resolvedCommand
}