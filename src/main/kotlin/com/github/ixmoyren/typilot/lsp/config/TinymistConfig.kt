package com.github.ixmoyren.typilot.lsp.config

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.io.HttpRequests
import com.intellij.util.system.CpuArch
import java.util.concurrent.atomic.AtomicReference
import kotlinx.serialization.json.*

const val TINYMIST_INSTALLER_CONFIG_JSON: String = "/lsp/installer.json"

val TINYMIST_INSTALLER_CONFIG: TinymistInstallerConfig? by lazy {
    runCatching {
        val text = TinymistInstallerConfig::class.java.getResourceAsStream(TINYMIST_INSTALLER_CONFIG_JSON)?.use { it.readBytes().toString(Charsets.UTF_8) } ?: return@lazy null
        json.decodeFromString<TinymistInstallerConfig>(text)
    }
        .getOrNull()
}

private val githubDownloadUrlCache = AtomicReference<String?>()

/**
 * The `browser_download_url` of the configured tinymist asset for the current platform. Successful resolutions are cached, while failures are not, so a transient network error or
 * a GitHub rate limit does not permanently disable the download.
 */
val TINYMIST_GITHUB_DOWNLOAD_URL: String?
    get() {
        githubDownloadUrlCache.get()?.let {
            return it
        }

        val github = TINYMIST_INSTALLER_CONFIG?.run?.download?.github ?: return null
        val assetName = github.asset?.resolve() ?: return null
        val url = fetchLatestGitHubAssetUrl(github.owner, github.repository, assetName, github.prerelease)
        if (url != null) {
            githubDownloadUrlCache.set(url)
        }
        return url
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
private fun fetchLatestGitHubAssetUrl(owner: String, repository: String, assetName: String, prerelease: Boolean): String? = runCatching {
    val url = "https://api.github.com/repos/$owner/$repository/releases"
    val body =
        try {
            HttpRequests.request(url).accept("application/vnd.github+json").userAgent("typilot-intellij-plugin").connectTimeout(10_000).readTimeout(10_000).readString()
        } catch (e: HttpRequests.HttpStatusException) {
            LOG.warn("GitHub releases request for $owner/$repository returned HTTP ${e.statusCode}")
            return@runCatching null
        }

    val releases = json.parseToJsonElement(body).jsonArray
    val release =
        releases.firstOrNull { element ->
            val obj = element.jsonObject
            obj.booleanOrNull("prerelease") == prerelease && obj.booleanOrNull("draft") != true
        } ?: return@runCatching null

    val assets = release.jsonObject["assets"]?.jsonArray ?: return@runCatching null
    assets
        .firstOrNull {
            it.jsonObject["name"]?.jsonPrimitive?.contentOrNull == assetName
        }
        ?.jsonObject
        ?.get("browser_download_url")
        ?.jsonPrimitive
        ?.contentOrNull
}
    .getOrNull()

private fun JsonObject.booleanOrNull(memberName: String): Boolean? = (get(memberName) as? JsonPrimitive)?.booleanOrNull

private val json = Json { ignoreUnknownKeys = true }

private val LOG: Logger = Logger.getInstance("com.github.ixmoyren.typilot.lsp.config.TinymistConfig")
