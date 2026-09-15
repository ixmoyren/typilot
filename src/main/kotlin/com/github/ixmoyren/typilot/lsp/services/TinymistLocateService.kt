package com.github.ixmoyren.typilot.lsp.services

import com.github.ixmoyren.typilot.lsp.ConfigureLocator
import com.github.ixmoyren.typilot.lsp.TinymistLocator
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service

@Service(Service.Level.APP)
class TinymistLocateService {
    val locators: List<TinymistLocator> by lazy {
        listOf(
            ConfigureLocator.getInstance(),
            TinymistFindService.getInstance(),
            TinymistDownloadService.getInstance(),
        )
    }

    @Volatile private var cachedLocator: TinymistLocator? = null
    @Volatile private var locatorResolved = false

    val firstValidLocator: TinymistLocator?
        get() {
            if (!locatorResolved) {
                cachedLocator = locators.firstOrNull { it.version()?.contains("tinymist", ignoreCase = true) == true }
                locatorResolved = true
            }
            return cachedLocator
        }

    @Volatile private var cachedVersion: String? = null
    @Volatile private var versionResolved = false

    val version: String?
        get() {
            if (!versionResolved) {
                cachedVersion = firstValidLocator?.version()
                versionResolved = true
            }
            return cachedVersion
        }

    /** Drops the cached resolution so a changed tinymist path is picked up on the next access. */
    fun invalidate() {
        locatorResolved = false
        cachedLocator = null
        versionResolved = false
        cachedVersion = null
    }

    companion object {
        fun getInstance(): TinymistLocateService = ApplicationManager.getApplication().getService(TinymistLocateService::class.java)
    }
}
