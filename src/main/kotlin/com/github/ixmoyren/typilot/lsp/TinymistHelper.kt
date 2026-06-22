package com.github.ixmoyren.typilot.lsp

import com.github.ixmoyren.typilot.PlatformConfig
import com.github.ixmoyren.typilot.PlatformInfo
import com.github.ixmoyren.typilot.TypalizeUtils
import com.github.ixmoyren.typilot.TypalizeUtils.findBinary
import com.github.ixmoyren.typilot.TypalizeUtils.osArch
import com.github.ixmoyren.typilot.TypalizeUtils.osName
import com.github.ixmoyren.typilot.settings.TinymistSettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.Service
import java.io.File

@Service(Service.Level.APP)
class TinymistHelper : TinymistLocator {
    val tinymistBinary: String? by lazy {
        resolveTinymistPath()
    }

    override fun locate(): String? = tinymistBinary

    /** Resolves the tinymist binary path, or null if not available anywhere. */
    fun resolveTinymistPath(): String? =
        resolveBinaryPath(
            configuredPath = TinymistSettings.getInstance().tinymistPath,
            findOnPath = { findBinary("tinymist") },
            downloadedBinary = getDownloadedBinaryPath(),
        )

    /** Returns the directory where the downloaded tinymist binary is stored. */
    fun getDownloadDir(): File {
        val dir = File(PathManager.getPluginsPath(), "typilot${File.separator}bin")
        dir.mkdirs()
        return dir
    }

    /** Returns the expected path for the downloaded tinymist binary. */
    fun getDownloadedBinaryPath(): File {
        val binaryName = if (TypalizeUtils.isWindows()) "tinymist.exe" else "tinymist"
        return File(getDownloadDir(), binaryName)
    }

    companion object {
        fun getInstance() = ApplicationManager.getApplication().getService(TinymistHelper::class.java)!!

        /** Determines the GitHub release asset name for tinymist on the current platform. Returns null if the host platform is not in tinymist's supported matrix. */
        fun getPlatformAssetName(): String? {
            val platformInfo = PlatformInfo.currentHost(osName, osArch) ?: return null
            return PlatformConfig.tinymistAsset(platformInfo)
        }

        /**
         * Pure-function core of the 3-stage binary resolution fallback. The instance methods [resolveTinymistPath] are thin wrappers that supply the real settings, PATH lookup,
         * and downloaded file. Exposed for unit testing without an IntelliJ fixture.
         */
        internal fun resolveBinaryPath(
            configuredPath: String,
            findOnPath: () -> String?,
            downloadedBinary: File,
        ): String? {
            if (configuredPath.isNotBlank() && isBinaryExecutable(File(configuredPath))) {
                return configuredPath
            }
            findOnPath()?.let {
                return it
            }
            if (isBinaryExecutable(downloadedBinary)) {
                return downloadedBinary.absolutePath
            }
            return null
        }

        internal fun isBinaryExecutable(file: File): Boolean {
            if (!file.isFile) return false
            if (TypalizeUtils.isWindows()) {
                val ext = file.extension.lowercase()
                return ext in listOf("exe", "cmd", "bat", "com") || file.canExecute()
            }
            return file.canExecute()
        }
    }
}