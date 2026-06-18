package com.github.ixmoyren.typilot

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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
         * Normalizes `os.name` and `os.arch` into the canonical values used in `platforms.json`.
         * Returns `null` if either property is missing or cannot be recognized.
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
                "aarch64", "arm64" -> "arm64"
                "x86_64", "amd64", "x64" -> "x64"
                else -> null
            }
        }
    }
}

/**
 * Download artifact information for a specific platform.
 *
 * @property asset The filename of the asset to download.
 * @property archive The archive extension (e.g., `zip`, `tar.gz`) if the asset is archived,
 *                   or `null` if it's a raw executable.
 */
data class PlatformEntry(val asset: String, val archive: String?)

/**
 * Configuration for a tool, including its download base URL and supported platforms.
 */
data class ToolConfig(
    val baseUrl: String,
    val platforms: Map<PlatformInfo, PlatformEntry>,
) {
    fun assetFor(key: PlatformInfo): PlatformEntry? = platforms[key]
    fun supportedPlatforms(): Set<PlatformInfo> = platforms.keys
}

/**
 * Declarative platform matrix for `tinymist` downloads.
 *
 * Loads the platform configuration from `/platforms.json` on the plugin classpath.
 * The authoritative [supported] set is the set of platforms for which `tinymist`
 * binaries are available.
 */
object PlatformConfig {
    private val logger = logger<PlatformConfig>()

    private val configs: Map<String, ToolConfig> by lazy { load() }

    val tinymist: ToolConfig
        get() = configs["tinymist"]
            ?: error("platforms.json missing 'tinymist' section")

    val tinymistBaseUrl: String
        get() = tinymist.baseUrl

    /**
     * Platforms on which the `tinymist` binary is available.
     *
     * This set drives the auto-download flow. If the current host platform is not
     * in this set, the plugin will display an unsupported-platform error and skip
     * automatic downloads.
     */
    val supported: Set<PlatformInfo> by lazy {
        val platforms = tinymist.supportedPlatforms()
        if (platforms.isEmpty()) {
            logger.warn("tinymist platform set is empty — auto-download will be disabled for all hosts")
        }
        platforms
    }

    /**
     * Checks whether a given platform key is supported (i.e., tinymist is available).
     */
    fun isSupported(key: PlatformInfo): Boolean = key in supported

    /**
     * Human-readable summary of supported platforms, e.g.:
     * `darwin/arm64, darwin/x64, linux/arm64, linux/x64, windows/x64`
     *
     * Used in the unsupported-platform error notification.
     */
    fun supportedPlatformsDescription(): String =
        supported.sortedWith(compareBy({ it.os }, { it.arch })).joinToString(", ") { it.toString() }

    /**
     * DTO for a single platform entry in `platforms.json`.
     *
     * The JSON schema ships `platforms` as an array of objects with `os`, `arch`,
     * `asset`, and optional `archive` fields.
     */
    private data class PlatformEntryDto(
        val os: String,
        val arch: String,
        val asset: String,
        val archive: String? = null,
    )

    private data class ToolConfigDto(
        val baseUrl: String,
        val platforms: List<PlatformEntryDto>,
    ) {
        /**
         * Converts the DTO into the public [ToolConfig] model, transforming the
         * platform list into a map keyed by [PlatformInfo] for fast lookup.
         */
        fun toToolConfig(): ToolConfig {
            val map = LinkedHashMap<PlatformInfo, PlatformEntry>()
            for (entry in platforms) {
                val key = PlatformInfo(entry.os, entry.arch)
                val previous = map.put(key, PlatformEntry(entry.asset, entry.archive))
                if (previous != null) {
                    logger.warn(
                        "platforms.json: duplicate entry for $key. " +
                                "Previous: $previous, new: $entry. Later entry wins.",
                    )
                }
            }
            return ToolConfig(baseUrl, map)
        }
    }

    private fun load(): Map<String, ToolConfig> {
        val resourcePath = "/platforms.json"
        val stream = PlatformConfig::class.java.getResourceAsStream(resourcePath)
            ?: error("$resourcePath not found on classpath")

        val typeRef = object : TypeReference<Map<String, ToolConfigDto>>() {}
        return stream.use { inputStream ->
            val raw: Map<String, ToolConfigDto> = jacksonObjectMapper().readValue(inputStream, typeRef)
            raw.mapValues { (_, dto) -> dto.toToolConfig() }
        }
    }
}