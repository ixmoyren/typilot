package com.github.ixmoyren.typilot.preview

import com.github.ixmoyren.typilot.TYPST_LANGUAGE_SERVER_ID
import com.google.gson.Gson
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JCEFHtmlPanel
import com.redhat.devtools.lsp4ij.commands.CommandExecutor
import com.redhat.devtools.lsp4ij.commands.LSPCommandContext
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandler
import org.cef.handler.CefLoadHandlerAdapter
import org.cef.network.CefRequest
import org.eclipse.lsp4j.Command
import java.beans.PropertyChangeListener
import java.util.*
import javax.swing.JComponent
import javax.swing.JLabel

class TypstPreviewFileEditor(private val project: Project, private val virtualFile: VirtualFile) :
    JCEFHtmlPanel(false, null, null), FileEditor {

    private val logger = logger<TypstPreviewFileEditor>()

    private val gson: Gson by lazy {
        Gson()
    }

    companion object {
        private const val PREVIEW_COMMAND = "tinymist.doStartPreview"
    }

    @Volatile
    private var isServerReady = false
    private var unsupportedLabel: JLabel? = null

    init {
        if (!JBCefApp.isSupported()) {
            logger.warn("JCEF is not supported, preview will show an error message.")
            unsupportedLabel = JLabel("JCEF browser is not supported in this environment.")
        } else {
            logger.info("JCEF is supported, setting up browser.")
            setupLoadHandler()
            ApplicationManager.getApplication().invokeLater {
                if (!isDisposed) {
                    startPreview()
                } else {
                    logger.info("Editor disposed before server check could start.")
                }
            }
        }
        logger.info("TypstPreviewFileEditor: Initialization complete.")
    }

    private fun startPreview() {
        val fsPath = virtualFile.path
        val taskId = UUID.randomUUID().toString().substring(0, 7)
        val previewArgs = listOf("--task-id", taskId, "--data-plane-host", "127.0.0.1:0", fsPath)
        val args: List<Any> = listOf(previewArgs)
        val command = Command("Preview", PREVIEW_COMMAND, args)
        val context = LSPCommandContext(command, project).setPreferredLanguageServerId(TYPST_LANGUAGE_SERVER_ID)

        val response = CommandExecutor.executeCommand(context)
        if (!response.exists()) {
            logger.warn("Command $PREVIEW_COMMAND not found on server $TYPST_LANGUAGE_SERVER_ID")
            showErrorHtml("typst-preview.preview command not found. Is tinymist running?")
            return
        }

        response
            .response()
            ?.thenAccept { result ->
                val url = extractPreviewUrl(result)
                if (url != null) {
                    logger.info("Preview URL: $url")
                    loadUrlSafely(url)
                } else {
                    logger.warn("Cannot extract preview URL from result: $result")
                    showErrorHtml("Could not get preview URL from tinymist (result=$result)")
                }
            }
            ?.exceptionally { error ->
                logger.warn("Error starting preview: ${error.message}")
                showErrorHtml("Error starting preview: ${error.message}")
                null
            }
    }

    private fun extractPreviewUrl(result: Any?): String? =
        result
            ?.let { gson.fromJson(gson.toJsonTree(it), StartPreviewResponse::class.java) }
            ?.run {
                staticServerAddr?.let { "http://$it" } ?: staticServerPort?.let { "http://127.0.0.1:$it" }
            }

    @Suppress("SameParameterValue")
    private fun loadUrlSafely(url: String) {
        ApplicationManager.getApplication().invokeLater {
            if (!isDisposed && JBCefApp.isSupported()) {
                loadURL(url)
            }
        }
    }

    private fun showErrorHtml(message: String) {
        ApplicationManager.getApplication().invokeLater {
            if (!isDisposed && JBCefApp.isSupported()) {
                loadHTML("<html><body><h3>Error</h3><p>$message</p></body></html>")
            }
        }
    }

    override fun getComponent(): JComponent = unsupportedLabel ?: super.getComponent()

    override fun getPreferredFocusedComponent(): JComponent = getComponent()

    override fun getName(): String = "Tinymist Preview"

    override fun setState(state: FileEditorState) = Unit

    override fun isModified(): Boolean = false

    override fun isValid(): Boolean = true

    override fun addPropertyChangeListener(listener: PropertyChangeListener) = Unit

    override fun removePropertyChangeListener(listener: PropertyChangeListener) = Unit

    override fun getFile(): VirtualFile = virtualFile

    private val userData = mutableMapOf<Key<*>, Any?>()

    override fun <T> getUserData(key: Key<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return userData[key] as T?
    }

    override fun <T> putUserData(key: Key<T>, value: T?) {
        userData[key] = value
    }

    override fun selectNotify() {
        logger.info("selectNotify called for ${virtualFile.name}")
        if (JBCefApp.isSupported() && isServerReady && !isDisposed) {
            logger.info("Reloading preview")
            startPreview()
        } else {
            logger.warn("selectNotify skipped: supported=${JBCefApp.isSupported()}, ready=$isServerReady, disposed=$isDisposed")
        }
    }

    override fun deselectNotify() {
        logger.info("deselectNotify called for ${virtualFile.name}")
    }

    override fun dispose() {
        logger.info("Disposing editor...")
        try {
            if (JBCefApp.isSupported() && !isDisposed) {
                cefBrowser.stopLoad()
            }
        } catch (e: Exception) {
            logger.error("Error during stopLoad: ${e.message}", e)
        }
        super.dispose()
        logger.info("Disposal complete.")
    }

    private fun setupLoadHandler() {
        jbCefClient.addLoadHandler(
            object : CefLoadHandlerAdapter() {
                override fun onLoadingStateChange(
                    browser: CefBrowser?,
                    isLoading: Boolean,
                    canGoBack: Boolean,
                    canGoForward: Boolean
                ) {
                    logger.debug("Loading state changed: isLoading=$isLoading")
                }

                override fun onLoadStart(
                    browser: CefBrowser?,
                    frame: CefFrame?,
                    transitionType: CefRequest.TransitionType?
                ) {
                    logger.debug("Load started: URL=${frame?.url}, main=${frame?.isMain}")
                }

                override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
                    logger.debug("Load ended: URL=${frame?.url}, status=$httpStatusCode, main=${frame?.isMain}")
                }

                override fun onLoadError(
                    browser: CefBrowser,
                    frame: CefFrame,
                    errorCode: CefLoadHandler.ErrorCode,
                    errorText: String,
                    failedUrl: String
                ) {
                    logger.warn("Load error: code=$errorCode, text=$errorText, url=$failedUrl, main=${frame.isMain}")
                }
            },
            cefBrowser
        )
    }
}

data class StartPreviewResponse(
    val staticServerPort: Int? = null,
    val staticServerAddr: String? = null,
    val dataPlanePort: Int? = null,
    val isPrimary: Boolean = false,
)
