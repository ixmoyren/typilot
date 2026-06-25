package com.github.ixmoyren.typilot.lsp.config

import com.intellij.openapi.util.SystemInfo
import com.intellij.util.system.CpuArch
import com.redhat.devtools.lsp4ij.installation.download.GitHubAssetFetcher
import com.redhat.devtools.lsp4ij.installation.download.GitHubAssetFetcherManager
import com.redhat.devtools.lsp4ij.installation.download.Reporter
import kotlinx.serialization.json.Json

const val TINYMIST_INSTALLER_CONFIG_JSON: String = "/lsp/installer.json"

val TINYMIST_INSTALLER_CONFIG: TinymistInstallerConfig? by lazy {
    runCatching {
        val text = TinymistInstallerConfig::class.java.getResourceAsStream(TINYMIST_INSTALLER_CONFIG_JSON)
            ?.use { it.readBytes().toString(Charsets.UTF_8) } ?: return@lazy null
        json.decodeFromString<TinymistInstallerConfig>(text)
    }.getOrNull()
}

val TINYMIST_GITHUB_DOWNLOAD_URL: String? by lazy {
    val github = TINYMIST_INSTALLER_CONFIG?.run?.download?.github ?: return@lazy null
    val assetName = github.asset?.resolve() ?: return@lazy null

    val releaseMatcher = if (github.prerelease)
        GitHubAssetFetcher.PRERELEASE_MATCHER
    else
        GitHubAssetFetcher.RELEASE_MATCHER

    val fetcher = GitHubAssetFetcherManager.getInstance()
        .getAssetFetcher(github.owner, github.repository)

    return@lazy fetcher.getDownloadUrl(
        releaseMatcher,
        GitHubAssetFetcher.AssetMatcher(assetName),
        NOOP_REPORTER
    )
}

val TINYMIST_SUPPORTED_PLATFORMS: Set<String>? by lazy {
    TINYMIST_INSTALLER_CONFIG?.run?.download?.github?.asset?.supportedPlatforms()
}

val IS_SUPPORTED_PLATFORM: Boolean by lazy {
    val currentOs = when {
        SystemInfo.isWindows -> OsType.WINDOWS
        SystemInfo.isMac -> OsType.MAC
        SystemInfo.isLinux or SystemInfo.isFreeBSD or SystemInfo.isUnix -> OsType.UNIX
        else -> return@lazy false
    }
    val currentArch = when {
        CpuArch.isArm64() and SystemInfo.isMac -> ArchType.AARCH64
        CpuArch.isArm64() -> ArchType.ARM64
        CpuArch.isIntel64() -> ArchType.X86_64
        else -> return@lazy false
    }
    val platform = "${currentOs.key}/${currentArch.key}"

    return@lazy TINYMIST_SUPPORTED_PLATFORMS?.contains(platform) ?: false
}

private val json = Json { ignoreUnknownKeys = true }

private val NOOP_REPORTER = object : Reporter {
    override fun setText(text: String) {}
    override fun setText(text: String, e: Exception) {}
    override fun checkCanceled() {}
}