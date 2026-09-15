package com.github.ixmoyren.typilot.preview

import com.github.ixmoyren.typilot.lsp.TinymistCommands
import com.github.ixmoyren.typilot.lsp.TinymistLspService
import com.github.ixmoyren.typilot.lsp.TinymistServerStartedListener
import com.google.gson.Gson
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JCEFHtmlPanel
import java.beans.PropertyChangeListener
import java.util.*
import javax.swing.JComponent
import javax.swing.JLabel
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandler
import org.cef.handler.CefLoadHandlerAdapter
import org.cef.network.CefRequest

@Suppress("UnstableApiUsage")
class TypstPreviewFileEditor(private val project: Project, private val virtualFile: VirtualFile) : JCEFHtmlPanel(false, null, null), FileEditor {

    private val logger = logger<TypstPreviewFileEditor>()

    private val gson: Gson by lazy {
        Gson()
    }

    @Volatile private var previewUrl: String? = null

    @Volatile private var previewTaskId: String? = null

    @Volatile private var loadRetryCount = 0

    private val maxRetryCount = 10

    private val serverStartedListener = TinymistServerStartedListener { onTinymistServerStarted() }

    private var unsupportedLabel: JLabel? = null

    init {
        if (!JBCefApp.isSupported()) {
            logger.warn("JCEF is not supported, preview will show an error message.")
            unsupportedLabel = JLabel("JCEF browser is not supported in this environment.")
        } else {
            logger.info("JCEF is supported, setting up browser.")
            setupLoadHandler()
            registerServerStartedListener()
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

    private fun registerServerStartedListener() {
        TinymistLspService.getInstance(project).addListener(serverStartedListener)
    }

    private fun onTinymistServerStarted() {
        if (isDisposed) return
        if (previewUrl != null || previewTaskId != null) {
            logger.info("tinymist (re)started, resetting preview state and restarting preview.")
            previewUrl = null
            previewTaskId = null
            startPreview()
        }
    }

    private fun startPreview() {
        if (isDisposed) return
        val file = virtualFile
        ApplicationManager.getApplication().executeOnPooledThread {
            if (isDisposed) return@executeOnPooledThread
            val taskId = UUID.randomUUID().toString().substring(0, 7)
            val result = TinymistCommands.startPreview(project, file, taskId)
            if (result == null) {
                logger.warn("Command ${PREVIEW_COMMAND} failed or tinymist is not running")
                showErrorHtml("typst-preview.preview command not found. Is tinymist running?")
                return@executeOnPooledThread
            }

            val url = extractPreviewUrl(result)
            if (url != null) {
                logger.info("Preview URL: $url")
                previewUrl = url
                previewTaskId = taskId
                loadRetryCount = 0
                loadUrlSafely(url)
            } else {
                logger.warn("Cannot extract preview URL from result: $result")
                showErrorHtml("Could not get preview URL from tinymist (result=$result)")
            }
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
        when {
            !JBCefApp.isSupported() || isDisposed -> logger.warn("selectNotify skipped: supported=${JBCefApp.isSupported()}, disposed=$isDisposed")

            previewUrl != null -> {
                logger.info("Reloading preview")
                loadUrlSafely(previewUrl!!)
            }

            else -> startPreview()
        }
    }

    override fun deselectNotify() {
        logger.info("deselectNotify called for ${virtualFile.name}")
    }

    override fun dispose() {
        logger.info("Disposing editor...")
        runCatching { TinymistLspService.getInstance(project).removeListener(serverStartedListener) }
        previewTaskId?.let { taskId -> TinymistCommands.killPreview(project, taskId) }
        runCatching {
                if (JBCefApp.isSupported() && !isDisposed) cefBrowser.stopLoad()
            }
            .onFailure { e ->
                logger.error("Error during stopLoad: ${e.message}", e)
            }
        super.dispose()
        logger.info("Disposal complete.")
    }

    private fun setupLoadHandler() {
        jbCefClient.addLoadHandler(
            object : CefLoadHandlerAdapter() {
                override fun onLoadingStateChange(browser: CefBrowser?, isLoading: Boolean, canGoBack: Boolean, canGoForward: Boolean) {
                    logger.debug("Loading state changed: isLoading=$isLoading")
                }

                override fun onLoadStart(browser: CefBrowser?, frame: CefFrame?, transitionType: CefRequest.TransitionType?) {
                    logger.debug("Load started: URL=${frame?.url}, main=${frame?.isMain}")
                }

                override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
                    logger.debug("Load ended: URL=${frame?.url}, status=$httpStatusCode, main=${frame?.isMain}")
                }

                override fun onLoadError(browser: CefBrowser, frame: CefFrame, errorCode: CefLoadHandler.ErrorCode, errorText: String, failedUrl: String) {
                    if (!frame.isMain) return
                    if (errorCode == CefLoadHandler.ErrorCode.ERR_ABORTED) return

                    logger.warn("Load error: code=$errorCode, text=$errorText, url=$failedUrl")

                    if (errorCode == CefLoadHandler.ErrorCode.ERR_CONNECTION_REFUSED) {
                        val retryUrl = previewUrl ?: return
                        if (loadRetryCount >= maxRetryCount) {
                            showErrorHtml("Preview server did not start after $maxRetryCount retries.")
                            return
                        }
                        loadRetryCount += 1
                        ApplicationManager.getApplication()
                            .invokeLater(
                                {
                                    if (!isDisposed && JBCefApp.isSupported()) {
                                        logger.info("Retrying preview URL: $retryUrl")
                                        browser.loadURL(retryUrl)
                                    }
                                },
                                ModalityState.any())
                        return
                    }

                    showErrorHtml("Preview load error: $errorCode — $errorText<br>URL: $failedUrl")
                }
            },
            cefBrowser)
    }

    companion object {
        private const val PREVIEW_COMMAND = TinymistCommands.START_PREVIEW_COMMAND
    }
}

data class StartPreviewResponse(
    val staticServerPort: Int? = null,
    val staticServerAddr: String? = null,
    val dataPlanePort: Int? = null,
    val isPrimary: Boolean = false,
)
