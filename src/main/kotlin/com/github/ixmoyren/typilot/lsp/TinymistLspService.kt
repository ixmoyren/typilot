package com.github.ixmoyren.typilot.lsp

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Notifies interested components, such as the preview editor, that a `tinymist` language server finished starting. The built-in LSP client calls
 * [TypstLspClientDescriptor.lspServerListener] on every start, including restarts triggered from the Language Services widget, so preview state can be reset accordingly.
 */
fun interface TinymistServerStartedListener {
    fun serverStarted()
}

@Service(Service.Level.PROJECT)
class TinymistLspService {
    private val listeners = CopyOnWriteArrayList<TinymistServerStartedListener>()

    fun addListener(listener: TinymistServerStartedListener) {
        listeners.addIfAbsent(listener)
    }

    fun removeListener(listener: TinymistServerStartedListener) {
        listeners.remove(listener)
    }

    fun serverStarted() {
        listeners.forEach { it.serverStarted() }
    }

    companion object {
        fun getInstance(project: Project): TinymistLspService = project.service()
    }
}
