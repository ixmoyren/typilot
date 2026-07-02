package com.github.ixmoyren.typilot.lsp.config

import com.github.ixmoyren.typilot.lsp.ConfigureLocator
import com.github.ixmoyren.typilot.lsp.services.TinymistDownloadService
import com.github.ixmoyren.typilot.lsp.services.TinymistFindService
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressIndicator
import com.redhat.devtools.lsp4ij.installation.CommandLineUpdater
import com.redhat.devtools.lsp4ij.installation.DeclarativeLanguageServerInstaller
import com.redhat.devtools.lsp4ij.installation.ServerInstallationStatus
import com.redhat.devtools.lsp4ij.installation.definition.InstallerContext
import com.redhat.devtools.lsp4ij.installation.definition.ServerInstallerDescriptor
import com.redhat.devtools.lsp4ij.installation.definition.ServerInstallerManager
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture
import org.jetbrains.annotations.Nullable

class TinymistInstaller : DeclarativeLanguageServerInstaller(), CommandLineUpdater {
    private val logger = logger<TinymistInstaller>()
    private val descriptor: ServerInstallerDescriptor? by lazy { loadDescriptor() }

    override fun getCommandLine(): String? =
        PropertiesComponent.getInstance().getValue(KEY_COMMAND)
            ?: ConfigureLocator.getInstance().locate()
            ?: TinymistFindService.getInstance().locate()
            ?: TinymistDownloadService.getInstance().locate()

    override fun setCommandLine(commandLine: String) {
        PropertiesComponent.getInstance().setValue(KEY_COMMAND, commandLine)
    }

    override fun createInstallerContext(action: InstallerContext.InstallerAction, indicator: ProgressIndicator): InstallerContext {
        return super.createInstallerContext(action, indicator).also {
            it.commandLineUpdater = this
        }
    }

    @Nullable protected override fun getServerInstallerDescriptor(): ServerInstallerDescriptor? = descriptor

    override fun canExecute(serverInstallerDescriptor: ServerInstallerDescriptor): Boolean =
        !PropertiesComponent.getInstance().isTrueValue(KEY_INSTALLED) && super.canExecute(serverInstallerDescriptor)

    override fun execute(checkInstallationFuture: CompletableFuture<ServerInstallationStatus>): CompletableFuture<ServerInstallationStatus> {
        return checkInstallationFuture.whenComplete { result, _ ->
            if (result == ServerInstallationStatus.INSTALLED) {
                PropertiesComponent.getInstance().setValue(KEY_INSTALLED, true)
            }
        }
    }

    override fun reset() {
        super.reset()
        PropertiesComponent.getInstance().unsetValue(KEY_INSTALLED)
    }

    @Nullable
    private fun loadDescriptor(): ServerInstallerDescriptor? {
        return try {
            javaClass.getResourceAsStream(TINYMIST_INSTALLER_CONFIG_JSON)?.use { stream ->
                val json = String(stream.readAllBytes(), StandardCharsets.UTF_8)
                ServerInstallerManager.getInstance().loadInstaller(json)
            }
        } catch (e: IOException) {
            logger.error(e)
            null
        }
    }

    companion object {
        private const val KEY_COMMAND = "tinymist.server.command"
        private const val KEY_INSTALLED = "tinymist.install.done"
    }
}
