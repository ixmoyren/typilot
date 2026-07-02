package com.github.ixmoyren.typilot.lsp

import com.github.ixmoyren.typilot.TypalizeUtils.isBinaryExecutable
import com.github.ixmoyren.typilot.lsp.config.TinymistInstaller
import com.github.ixmoyren.typilot.settings.TinymistSettings
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import java.io.File

fun interface TinymistLocator {
    fun locate(): String?

    fun version(path: String? = locate()): String? = path?.let {
        val commandLine = GeneralCommandLine(it, "-V").apply { withCharset(Charsets.UTF_8) }
        return ExecUtil.execAndGetOutput(commandLine, 30_000).takeIf { result -> result.exitCode == 0 }?.stdout?.trim()
    }
}

class TinymistInstallerLocator(private val installer: TinymistInstaller) : TinymistLocator {
    override fun locate(): String? = installer.commandLine
}

@Service(Service.Level.APP)
class ConfigureLocator : TinymistLocator {
    private val configuredPath = TinymistSettings.getInstance().tinymistPath

    override fun locate(): String? = configuredPath.takeIf { it.isNotBlank() && isBinaryExecutable(File(it)) }

    companion object {
        fun getInstance(): ConfigureLocator = ApplicationManager.getApplication().getService(ConfigureLocator::class.java)
    }
}
