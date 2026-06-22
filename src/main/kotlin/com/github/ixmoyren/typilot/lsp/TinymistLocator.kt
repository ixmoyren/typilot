package com.github.ixmoyren.typilot.lsp

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil

fun interface TinymistLocator {
    fun locate(): String?

    fun version(path: String? = locate()): String? = path?.let {
        val commandLine = GeneralCommandLine(it, "-V").apply { withCharset(Charsets.UTF_8) }
        return ExecUtil.execAndGetOutput(commandLine, 30_000).takeIf { result -> result.exitCode == 0 }?.stdout?.trim()
    }
}

class TinymistInstallerLocator(private val installer: TinymistInstaller) : TinymistLocator {
    override fun locate(): String? = installer.resolvedCommand
}