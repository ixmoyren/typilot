package com.github.ixmoyren.typilot.lsp.services

import com.github.ixmoyren.typilot.lsp.ConfigureLocator
import com.github.ixmoyren.typilot.lsp.TinymistInstallerLocator
import com.github.ixmoyren.typilot.lsp.TinymistLocator
import com.github.ixmoyren.typilot.lsp.config.TinymistInstaller
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service

@Service(Service.Level.APP)
class TinymistLocateService {
    val installer = TinymistInstaller()

    val locators: List<TinymistLocator> by lazy {
        listOf(
            ConfigureLocator.getInstance(),
            TinymistFindService.getInstance(),
            TinymistDownloadService.getInstance(),
            TinymistInstallerLocator(installer),
        )
    }

    val firstValidLocator: TinymistLocator? by lazy {
        locators.firstOrNull {
            it.version()?.contains("tinymist", ignoreCase = true) == true
        }
    }

    val version: String? by lazy {
        firstValidLocator?.version()
    }

    companion object {
        fun getInstance(): TinymistLocateService =
            ApplicationManager.getApplication().getService(TinymistLocateService::class.java)
    }
}
