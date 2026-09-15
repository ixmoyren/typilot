package com.github.ixmoyren.typilot.lsp.config

import com.google.gson.JsonParser
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.system.CpuArch
import java.net.HttpURLConnection
import java.net.URI
import kotlinx.serialization.json.Json

const val TINYMIST_INSTALLER_CONFIG_JSON: String = "/lsp/installer.json"

val TINYMIST_INSTALLER_CONFIG: TinymistInstallerConfig? by lazy {
    runCatching {
            val text = TinymistInstallerConfig::class.java.getResourceAsStream(TINYMIST_INSTALLER_CONFIG_JSON)?.use { it.readBytes().toString(Charsets.UTF_8) } ?: return@lazy null
            json.decodeFromString<TinymistInstallerConfig>(text)
        }
        .getOrNull()
}

val TINYMIST_GITHUB_DOWNLOAD_URL: String? by lazy {
    val github = TINYMIST_INSTALLER_CONFIG?.run?.download?.github ?: return@lazy null
    val assetName = github.asset?.resolve() ?: return@lazy null

    fetchLatestGitHubAssetUrl(github.owner, github.repository, assetName, github.prerelease)
}

val TINYMIST_SUPPORTED_PLATFORMS: Set<String>? by lazy {
    TINYMIST_INSTALLER_CONFIG?.run?.download?.github?.asset?.supportedPlatforms()
}

val IS_SUPPORTED_PLATFORM: Boolean by lazy {
    val currentOs =
        when {
            SystemInfo.isWindows -> OsType.WINDOWS
            SystemInfo.isMac -> OsType.MAC
            SystemInfo.isLinux or SystemInfo.isFreeBSD or SystemInfo.isUnix -> OsType.UNIX
            else -> return@lazy false
        }
    val currentArch =
        when {
            CpuArch.isArm64() and SystemInfo.isMac -> ArchType.AARCH64
            CpuArch.isArm64() -> ArchType.ARM64
            CpuArch.isIntel64() -> ArchType.X86_64
            else -> return@lazy false
        }
    val platform = "${currentOs.key}/${currentArch.key}"

    return@lazy TINYMIST_SUPPORTED_PLATFORMS?.contains(platform) ?: false
}

/**
 * Resolves the `browser_download_url` of [assetName] from the newest release of the repository that matches [prerelease]. This replaces the LSP4IJ `GitHubAssetFetcher`, which was
 * the only remaining LSP4IJ dependency of the plugin.
 */
private fun fetchLatestGitHubAssetUrl(owner: String, repository: String, assetName: String, prerelease: Boolean): String? =
    runCatching {
            val url = URI("https://api.github.com/repos/$owner/$repository/releases").toURL()
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.setRequestProperty("User-Agent", "typilot-intellij-plugin")
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            if (connection.responseCode != 200) {
                LOG.warn("GitHub releases request for $owner/$repository returned HTTP ${connection.responseCode}")
                return@runCatching null
            }
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val releases = JsonParser.parseString(body).asJsonArray
            val release = releases.firstOrNull { it.asJsonObject.get("prerelease")?.asBoolean == prerelease } ?: return@runCatching null
            val assets = release.asJsonObject.getAsJsonArray("assets")
            val names = assets.mapNotNull { it.asJsonObject.get("name")?.asString }
            val matchedName = names.firstOrNull { it == assetName } ?: names.firstOrNull { it.contains(assetName, ignoreCase = true) }
            assets.firstOrNull { it.asJsonObject.get("name")?.asString == matchedName }?.asJsonObject?.get("browser_download_url")?.asString
        }
        .getOrNull()

private val json = Json { ignoreUnknownKeys = true }

private val LOG: Logger = Logger.getInstance("com.github.ixmoyren.typilot.lsp.config.TinymistConfig")
