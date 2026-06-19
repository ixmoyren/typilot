package com.github.ixmoyren.typilot

import com.github.ixmoyren.typilot.settings.TinymistSettings
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.Service
import java.io.File

/**
 * Resolves the tinymist and typst binary paths using the following priority:
 * 1. User-configured path in settings
 * 2. Binary found on system PATH and well-known install locations
 * 3. Previously downloaded binary in the plugin data directory
 * 4. `null` (not found)
 *
 * Note: IntelliJ as a GUI app on macOS/Windows may not inherit the user's full shell/terminal PATH, so we also probe well-known directories where cargo, homebrew, scoop, etc.
 * install binaries.
 *
 * On Linux, we follow XDG Base Directory Specification: if XDG_DATA_HOME is set, we also search $XDG_DATA_HOME/../bin (e.g., ~/.local/share/../bin -> ~/.local/bin).
 */
@Service(Service.Level.APP)
class TinymistManager {

    /** Resolves the tinymist binary path, or null if not available anywhere. */
    fun resolveTinymistPath(): String? =
        resolveBinaryPath(
            configuredPath = TinymistSettings.getInstance().tinymistPath,
            findOnPath = { findBinary("tinymist") },
            downloadedBinary = getDownloadedBinaryPath(),
        )

    /** Get the tinymist version number by resolve tinymist path */
    fun tinymistVersion(): String? = resolveTinymistPath()?.let { tinymistVersion(it) }

    /** Get the tinymist version number by path */
    fun tinymistVersion(path: String): String? {
        val commandLine = GeneralCommandLine(path, "-V").apply { withCharset(Charsets.UTF_8) }
        return ExecUtil.execAndGetOutput(commandLine, 30_000).takeIf { it.exitCode == 0 }?.stdout?.trim()
    }

    /** Returns the directory where the downloaded tinymist binary is stored. */
    fun getDownloadDir(): File {
        val dir = File(PathManager.getPluginsPath(), "typilot${File.separator}bin")
        dir.mkdirs()
        return dir
    }

    /** Returns the expected path for the downloaded tinymist binary. */
    fun getDownloadedBinaryPath(): File {
        val binaryName = if (isWindows()) "tinymist.exe" else "tinymist"
        return File(getDownloadDir(), binaryName)
    }

    companion object {
        fun getInstance(): TinymistManager = ApplicationManager.getApplication().getService(TinymistManager::class.java)

        val osName: String? = System.getProperty("os.name")
        val osArch: String? = System.getProperty("os.arch")

        fun isWindows(): Boolean = osName?.lowercase()?.contains("win") ?: false

        fun isMacOS(): Boolean = osName?.lowercase()?.contains("mac") ?: false

        fun isLinux(): Boolean = osName?.lowercase()?.contains("linux") ?: false

        /** Determines the GitHub release asset name for tinymist on the current platform. Returns null if the host platform is not in tinymist's supported matrix. */
        fun getPlatformAssetName(): String? {
            val platformInfo = PlatformInfo.currentHost(osName, osArch) ?: return null
            return PlatformConfig.tinymist.assetFor(platformInfo)?.asset
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
            if (isWindows()) {
                val ext = file.extension.lowercase()
                return ext in listOf("exe", "cmd", "bat", "com") || file.canExecute()
            }
            return file.canExecute()
        }

        /** Well-known directories where tools like tinymist/typst are commonly installed. Returns only directories relevant to the current OS. */
        private fun getWellKnownDirs(): List<String> {
            val home = File(System.getProperty("user.home"))
            val dirs = mutableListOf<String>()
            if (isWindows()) {
                addWindowsDirs(dirs, home)
            } else {
                addUnixDirs(dirs, home)
            }
            return dirs.distinct()
        }

        /** Windows-specific well-known install directories. */
        private fun addWindowsDirs(dirs: MutableList<String>, home: File) {
            // Cargo (Rust) — most common install method for both tinymist and typst
            dirs.add(File(home, ".cargo${File.separator}bin").absolutePath)

            // Scoop
            dirs.add(File(home, "scoop${File.separator}shims").absolutePath)

            // WinGet / App Installer default paths
            val localAppData = System.getenv("LOCALAPPDATA")
            if (localAppData != null) {
                dirs.add(File(localAppData, "Microsoft${File.separator}WinGet${File.separator}Links").absolutePath)
                dirs.add(File(localAppData, "Programs${File.separator}tinymist").absolutePath)
            }

            // Chocolatey
            val chocoInstall = System.getenv("ChocolateyInstall")
            if (chocoInstall != null) {
                dirs.add(File(chocoInstall, "bin").absolutePath)
            } else {
                dirs.add(File("C:${File.separator}ProgramData${File.separator}chocolatey${File.separator}bin").absolutePath)
            }

            // Program Files
            val programFiles = System.getenv("ProgramFiles")
            if (programFiles != null) {
                dirs.add(File(programFiles, "tinymist").absolutePath)
            }

            // Common user-local bin
            dirs.add(File(home, ".local${File.separator}bin").absolutePath)
        }

        /** macOS and Linux well-known install directories. On Linux, follows XDG Base Directory Specification when possible. */
        private fun addUnixDirs(dirs: MutableList<String>, home: File) {
            // User local bins (priority over system)
            // XDG: if XDG_DATA_HOME is set, $XDG_DATA_HOME/../bin is likely user's bin dir
            val xdgDataHome = System.getenv("XDG_DATA_HOME")
            if (xdgDataHome != null) {
                File(xdgDataHome).parentFile?.let { parent ->
                    dirs.add(File(parent, "bin").absolutePath)
                }
            } else {
                // Fallback to standard ~/.local/bin
                dirs.add(File(home, ".local/bin").absolutePath)
            }
            // Additional common user bin locations
            dirs.add(File(home, ".bin").absolutePath)

            // Cargo (Rust) — most common install method
            dirs.add(File(home, ".cargo/bin").absolutePath)

            // Homebrew
            if (isMacOS()) {
                dirs.add("/opt/homebrew/bin")
                dirs.add("/usr/local/bin")
            }
            if (isLinux()) {
                dirs.add("/home/linuxbrew/.linuxbrew/bin")
                dirs.add(File(home, ".linuxbrew/bin").absolutePath)
            }

            // Common system paths
            dirs.add("/usr/local/bin")
            dirs.add("/usr/bin")

            // Nix
            dirs.add(File(home, ".nix-profile/bin").absolutePath)
            dirs.add("/run/current-system/sw/bin")

            // Volta (Node.js version manager)
            dirs.add(File(home, ".volta/bin").absolutePath)
        }

        /** Searches for a binary by name on the system PATH and well-known install directories. */
        fun findBinary(binaryName: String): String? {
            val extensions = if (isWindows()) listOf(".exe", ".cmd", ".bat", "") else listOf("")

            // Combine system PATH dirs with well-known dirs
            val pathDirs = System.getenv("PATH")?.split(File.pathSeparator).orEmpty()
            val allDirs = (pathDirs + getWellKnownDirs()).distinct()

            for (dir in allDirs) {
                for (ext in extensions) {
                    val candidate = File(dir, binaryName + ext)
                    if (isBinaryExecutable(candidate)) {
                        return candidate.absolutePath
                    }
                }
            }
            return null
        }
    }
}
