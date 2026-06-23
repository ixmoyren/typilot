package com.github.ixmoyren.typilot.lsp.services

import com.github.ixmoyren.typilot.TypalizeUtils
import com.github.ixmoyren.typilot.lsp.TinymistLocator
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service

@Service(Service.Level.APP)
class TinymistFindService : TinymistLocator {
    val tinymistBinary: String? by lazy {
        TypalizeUtils.findBinary("tinymist")
    }

    override fun locate(): String? = tinymistBinary

    companion object {
        fun getInstance() = ApplicationManager.getApplication().getService(TinymistFindService::class.java)!!
    }
}
