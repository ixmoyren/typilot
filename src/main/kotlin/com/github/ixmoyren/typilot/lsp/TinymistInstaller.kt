package com.github.ixmoyren.typilot.lsp

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressIndicator
import com.redhat.devtools.lsp4ij.installation.CommandLineUpdater
import com.redhat.devtools.lsp4ij.installation.DeclarativeLanguageServerInstaller
import com.redhat.devtools.lsp4ij.installation.definition.InstallerContext
import com.redhat.devtools.lsp4ij.installation.definition.ServerInstallerDescriptor
import com.redhat.devtools.lsp4ij.installation.definition.ServerInstallerManager
import org.jetbrains.annotations.Nullable
import java.io.IOException
import java.nio.charset.StandardCharsets


class TinymistInstaller : DeclarativeLanguageServerInstaller(), CommandLineUpdater {
    private val logger = logger<TinymistInstaller>()
    private val descriptor: ServerInstallerDescriptor? by lazy { loadDescriptor() }

    @Volatile
    var resolvedCommand: String? = null
        private set

    override fun getCommandLine(): String? = resolvedCommand

    override fun setCommandLine(commandLine: String) {
        resolvedCommand = commandLine
    }

    override fun createInstallerContext(
        action: InstallerContext.InstallerAction,
        indicator: ProgressIndicator
    ): InstallerContext {
        return super.createInstallerContext(action, indicator).also {
            it.commandLineUpdater = this
        }
    }


    @Nullable
    protected override fun getServerInstallerDescriptor(): ServerInstallerDescriptor? = descriptor

    @Nullable
    private fun loadDescriptor(): ServerInstallerDescriptor? {
        return try {
            javaClass.getResourceAsStream("/lsp/installer.json")?.use { stream ->
                val json = String(stream.readAllBytes(), StandardCharsets.UTF_8)
                ServerInstallerManager.getInstance().loadInstaller(json)
            }
        } catch (e: IOException) {
            logger.error(e)
            null
        }
    }
}