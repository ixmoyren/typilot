package com.github.ixmoyren.typilot.lsp.services

import com.github.ixmoyren.typilot.PlatformConfig
import com.github.ixmoyren.typilot.TYPILOT_NOTIFICATION_GROUP_ID
import com.github.ixmoyren.typilot.TypalizeUtils
import com.github.ixmoyren.typilot.TypilotBundle
import com.github.ixmoyren.typilot.lsp.TinymistHelper
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.util.io.HttpRequests
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.util.concurrent.atomic.AtomicBoolean

@Service(Service.Level.APP)
class TinymistDownloadService {
    private val logger = logger<TinymistDownloadService>()
    private val downloading = AtomicBoolean(false)

    /** Downloads tinymist in a background task with a progress indicator. Calls [onComplete] on the EDT when done (true = success, false = failure). */
    fun downloadInBackground(project: Project?, onComplete: ((Boolean) -> Unit)? = null) {
        if (!downloading.compareAndSet(false, true)) {
            onComplete?.let { ApplicationManager.getApplication().invokeLater { it(false) } }
            return
        }

        object : Task.Backgroundable(project, TypilotBundle["download.tinymist.task.title"], true) {
            override fun run(indicator: ProgressIndicator) {
                try {
                    performDownload(project, indicator)
                    onComplete?.let { ApplicationManager.getApplication().invokeLater { it(true) } }
                } catch (e: Exception) {
                    if (indicator.isCanceled) {
                        logger.info("Tinymist download cancelled by user")
                    } else {
                        logger.warn("Failed to download tinymist", e)
                        notifyError(project, TypilotBundle["download.tinymist.failed", e.message ?: ""])
                    }
                    onComplete?.let { ApplicationManager.getApplication().invokeLater { it(false) } }
                } finally {
                    downloading.set(false)
                }
            }
        }
            .queue()
    }

    private fun performDownload(project: Project?, indicator: ProgressIndicator) {
        indicator.isIndeterminate = false
        indicator.text = TypilotBundle["download.tinymist.resolving"]
        indicator.fraction = 0.0

        val assetName =
            TinymistHelper.getPlatformAssetName() ?: throw UnsupportedOperationException(unsupportedPlatformMessage())

        val downloadUrl = resolveLatestDownloadUrl(PlatformConfig.tinymistBaseUrl, assetName) ?: throw IOException(
            TypilotBundle["download.tinymist.notFound", assetName]
        )

        checkCanceled(indicator)

        indicator.text = TypilotBundle["download.tinymist.downloading"]
        indicator.fraction = 0.1

        val targetFile = TinymistHelper.getInstance().getDownloadedBinaryPath()
        downloadFile(downloadUrl, targetFile, indicator)

        if (!TypalizeUtils.isWindows()) {
            targetFile.setExecutable(true, false)
        }

        indicator.fraction = 1.0
        indicator.text = TypilotBundle["download.tinymist.success"]

        logger.info("Tinymist downloaded to: ${targetFile.absolutePath}")

        NotificationGroupManager.getInstance()
            .getNotificationGroup(TYPILOT_NOTIFICATION_GROUP_ID)
            .createNotification(
                TypilotBundle["notification.tinymist.downloaded.title"],
                TypilotBundle["notification.tinymist.downloaded.body"],
                NotificationType.INFORMATION
            )
            .notify(project)
    }

    private fun checkCanceled(indicator: ProgressIndicator) {
        if (indicator.isCanceled) {
            throw InterruptedException("Download canceled")
        }
    }

    fun resolveLatestDownloadUrl(baseUrl: String, assetName: String): String? {
        val url = "$baseUrl/$assetName"
        return try {
            HttpRequests.head(url)
                .tuner { connection ->
                    (connection as? HttpURLConnection)?.instanceFollowRedirects = true
                }
                .tryConnect()
            url
        } catch (e: IOException) {
            logger.warn("Could not resolve download URL for $assetName: ${e.message}")
            null
        }
    }

    private fun downloadFile(url: String, target: File, indicator: ProgressIndicator) {
        target.parentFile.mkdirs()
        val tempFile = File(target.parent, "${target.name}.download")

        try {
            HttpRequests.request(url).forceHttps(true).saveToFile(tempFile, indicator)
            if (tempFile.length() == 0L) {
                throw IOException("Downloaded file is empty: $url")
            }
            atomicMove(tempFile, target)
        } finally {
            if (tempFile.exists() && !tempFile.delete()) {
                logger.warn("Failed to delete temporary file: ${tempFile.absolutePath}")
            }
        }
    }

    private fun notifyError(project: Project?, message: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(TYPILOT_NOTIFICATION_GROUP_ID)
            .createNotification(
                TypilotBundle["notification.tinymist.download.failed.title"],
                message,
                NotificationType.ERROR
            )
            .notify(project)
    }

    companion object {
        fun getInstance(): TinymistDownloadService =
            ApplicationManager.getApplication().getService(TinymistDownloadService::class.java)

        /**
         * Moves [tempFile] to [target], overwriting if [target] exists. Tries a fast rename first, falling back to copy + delete when the rename isn't possible (e.g. across
         * filesystems).
         */
        internal fun atomicMove(tempFile: File, target: File) {
            if (target.exists() && !target.delete()) {
                throw IOException("Failed to delete existing target file: ${target.absolutePath}")
            }
            if (!tempFile.renameTo(target)) {
                tempFile.copyTo(target, overwrite = true)
                if (!tempFile.delete()) {
                    logger<TinymistDownloadService>().warn("Failed to delete temporary file after copy: ${tempFile.absolutePath}")
                }
            }
        }

        internal fun unsupportedPlatformMessage(): String {
            val os = System.getProperty("os.name")
            val arch = System.getProperty("os.arch")
            return "Your platform (os=$os, arch=$arch) is not fully supported. " +
                    "The plugin requires both tinymist and typst, available on: " +
                    "${PlatformConfig.supportedPlatformsDescription()}. " +
                    "On other platforms, install the tools manually and set their paths " +
                    "in Settings → Tools → Typilot."
        }
    }
}
