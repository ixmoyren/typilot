package com.github.ixmoyren.typilot.lsp.services

import com.github.ixmoyren.typilot.lsp.TinymistHelper
import com.github.ixmoyren.typilot.lsp.TinymistInstaller
import com.github.ixmoyren.typilot.lsp.TinymistInstallerLocator
import com.github.ixmoyren.typilot.lsp.TinymistLocator
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service

@Service(Service.Level.APP)
class TinymistLocatorService {
    val installer = TinymistInstaller()

    val locators: List<TinymistLocator> by lazy {
        listOf(
            TinymistHelper.getInstance(),
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
        fun getInstance(): TinymistLocatorService =
            ApplicationManager.getApplication().getService(TinymistLocatorService::class.java)
    }
}