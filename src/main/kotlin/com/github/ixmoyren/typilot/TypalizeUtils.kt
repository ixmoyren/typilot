package com.github.ixmoyren.typilot

import com.intellij.openapi.util.SystemInfo
import java.io.File

object TypalizeUtils {
    private val WINDOWS_EXECUTABLE_EXTS = setOf("exe", "cmd", "bat", "com")
    val osName = System.getProperty("os.name")?.lowercase()
    val osArch = System.getProperty("os.arch")?.lowercase()

    /** Well-known directories where tools like tinymist/typst are commonly installed. */
    private fun getWellKnownDirs(): List<String> =
        buildList {
            val home = File(System.getProperty("user.home"))
            if (SystemInfo.isWindows) {
                add(File(home, ".cargo/bin").absolutePath)
                add(File(home, "scoop/shims").absolutePath)
                System.getenv("LOCALAPPDATA")?.let { localAppData ->
                    add(File(localAppData, "Microsoft/WinGet/Links").absolutePath)
                    add(File(localAppData, "Programs/tinymist").absolutePath)
                }
                System.getenv("ChocolateyInstall")?.let { choco ->
                    add(File(choco, "bin").absolutePath)
                } ?: add("C:/ProgramData/chocolatey/bin")
                System.getenv("ProgramFiles")?.let { progFiles ->
                    add(File(progFiles, "tinymist").absolutePath)
                }
                add(File(home, ".local/bin").absolutePath)
            } else {
                System.getenv("XDG_DATA_HOME")?.let { xdgData ->
                    File(xdgData).parentFile?.let { parent ->
                        add(File(parent, "bin").absolutePath)
                    }
                } ?: add(File(home, ".local/bin").absolutePath)
                add(File(home, ".bin").absolutePath)
                add(File(home, ".cargo/bin").absolutePath)
                if (SystemInfo.isMac) {
                    add("/opt/homebrew/bin")
                    add("/usr/local/bin")
                }
                if (SystemInfo.isLinux) {
                    add("/home/linuxbrew/.linuxbrew/bin")
                    add(File(home, ".linuxbrew/bin").absolutePath)
                }
                add("/usr/local/bin")
                add("/usr/bin")
                add(File(home, ".nix-profile/bin").absolutePath)
                add("/run/current-system/sw/bin")
                add(File(home, ".volta/bin").absolutePath)
            }
        }.distinct()

    /** Searches for a binary by name in system PATH and well-known directories. */
    fun findBinary(binaryName: String): String? {
        val extensions = if (SystemInfo.isWindows) listOf(".exe", ".cmd", ".bat", "") else listOf("")
        val pathDirs = System.getenv("PATH")?.split(File.pathSeparator).orEmpty()
        val allDirs = (pathDirs + getWellKnownDirs()).distinct()

        return allDirs.firstNotNullOfOrNull { dir ->
            extensions.firstNotNullOfOrNull { ext ->
                val file = File(dir, binaryName + ext)
                if (file.isFile && file.canExecute()) file.absolutePath else null
            }
        }
    }

    fun isBinaryExecutable(file: File): Boolean =
        file.isFile &&
                when {
                    SystemInfo.isWindows -> file.extension.lowercase() in WINDOWS_EXECUTABLE_EXTS || file.canExecute()
                    else -> file.canExecute()
                }
}
