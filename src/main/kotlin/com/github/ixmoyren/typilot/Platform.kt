package com.github.ixmoyren.typilot

import com.github.ixmoyren.typilot.PlatformConfig.supported
import com.intellij.openapi.diagnostic.logger

/**
 * Represents a normalized operating system and architecture pair.
 *
 * Canonical values:
 * - os: `darwin`, `linux`, `windows`
 * - arch: `arm64`, `x64`
 */
data class PlatformInfo(val os: String, val arch: String) {
    override fun toString(): String = "$os/$arch"

    companion object {
        /**
         * Creates a [PlatformInfo] for the current host by reading system properties.
         *
         * Normalizes `os.name` and `os.arch` into the canonical values used in `tinymist.json`. Returns `null` if either property is missing or cannot be recognized.
         */
        fun currentHost(
            osName: String? = System.getProperty("os.name"),
            osArch: String? = System.getProperty("os.arch"),
        ): PlatformInfo? {
            val os = normalizeOs(osName) ?: return null
            val arch = normalizeArch(osArch) ?: return null
            return PlatformInfo(os, arch)
        }

        internal fun normalizeOs(osName: String?): String? {
            val n = osName?.lowercase() ?: return null
            return when {
                "mac" in n || "darwin" in n -> "darwin"
                "win" in n -> "windows"
                "linux" in n -> "linux"
                else -> null
            }
        }

        internal fun normalizeArch(osArch: String?): String? {
            val a = osArch?.lowercase() ?: return null
            return when (a) {
                "aarch64",
                "arm64" -> "arm64"

                "x86_64",
                "amd64",
                "x64" -> "x64"

                else -> null
            }
        }
    }
}

/**
 * Declarative platform matrix for `tinymist` downloads.
 *
 * Loads the platform configuration from `/tinymist.json` on the plugin classpath. The authoritative [supported] set is the set of platforms for which `tinymist` binaries are
 * available.
 */
object PlatformConfig {
    private val logger = logger<PlatformConfig>()

    val tinymistBaseUrl: String
        get() = TypilotBundle["download.tinymist.baseUrl"]

    fun tinymistAsset(info: PlatformInfo): String {
        val prefix = TypilotBundle["download.tinymist.setting.prefix"]
        val key = "$prefix.${info.os}.${info.arch}"
        return TypilotBundle[key]
    }

    /**
     * Platforms on which the `tinymist` binary is available.
     *
     * This set drives the auto-download flow. If the current host platform is not in this set, the plugin will display an unsupported-platform error and skip automatic downloads.
     */
    val supported: Set<PlatformInfo> by lazy {
        val platforms = TypilotBundle.tinymistPlatforms
        if (platforms.isEmpty()) {
            logger.warn("tinymist platform set is empty — auto-download will be disabled for all hosts")
        }
        platforms
    }

    /**
     * Human-readable summary of supported platforms, e.g.: `darwin/arm64, darwin/x64, linux/arm64, linux/x64, windows/x64`
     *
     * Used in the unsupported-platform error notification.
     */
    fun supportedPlatformsDescription(): String =
        supported.sortedWith(compareBy({ it.os }, { it.arch })).joinToString(", ") { it.toString() }
}
