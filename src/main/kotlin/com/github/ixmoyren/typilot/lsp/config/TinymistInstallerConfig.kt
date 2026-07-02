package com.github.ixmoyren.typilot.lsp.config

import com.intellij.openapi.util.SystemInfo
import com.intellij.util.system.CpuArch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

@Serializable
data class TinymistInstallerConfig(val id: String, val name: String, val executeOnStartServer: Boolean = false, val check: CheckConfig? = null, val run: RunConfig? = null)

@Serializable data class CheckConfig(val exec: ExecConfig? = null)

@Serializable data class ExecConfig(val name: String, val command: String, val timeout: Int = 3000)

@Serializable data class RunConfig(val download: DownloadConfig? = null)

@Serializable data class DownloadConfig(val name: String, val github: GitHubConfig? = null, val output: OutputConfig? = null, val onSuccess: OnSuccessConfig? = null)

@Serializable data class GitHubConfig(val owner: String, val repository: String, val prerelease: Boolean = false, val asset: AssetConfig? = null)

@Serializable data class OutputConfig(val dir: String, val file: OutputFileConfig? = null)

@Serializable data class OutputFileConfig(val name: PlatformIndent? = null, val executable: Boolean = false)

@Serializable data class OnSuccessConfig(val configureServer: ConfigureServerConfig? = null)

@Serializable data class ConfigureServerConfig(val name: String, val command: PlatformIndent, val update: Boolean = false)

enum class OsType(val key: String) {
    WINDOWS("windows"),
    MAC("mac"),
    UNIX("unix"),
    DEFAULT("default");

    companion object {
        fun fromKey(key: String): OsType? = entries.find { it.key == key }

        val current: OsType
            get() =
                when {
                    SystemInfo.isWindows -> WINDOWS
                    SystemInfo.isMac -> MAC
                    else -> UNIX
                }
    }
}

enum class ArchType(val key: String) {
    X86_64("x86_64"),
    ARM64("arm64"),
    AARCH64("aarch64");

    companion object {
        fun fromKey(key: String): ArchType? = entries.find { it.key == key }

        val current: ArchType
            get() = if (CpuArch.isArm64()) ARM64 else X86_64
    }
}

@Serializable(with = PlatformIndentSerializer::class)
data class PlatformIndent(val values: Map<OsType, String>) {
    fun resolve(): String? = values[OsType.current] ?: values[OsType.DEFAULT]
}

object PlatformIndentSerializer : KSerializer<PlatformIndent> {
    override val descriptor = buildClassSerialDescriptor("PlatformIndent")

    override fun deserialize(decoder: Decoder): PlatformIndent {
        val jsonDecoder = decoder as JsonDecoder
        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonPrimitive -> PlatformIndent(mapOf(OsType.DEFAULT to element.content))

            is JsonObject ->
                PlatformIndent(
                    element.entries
                        .mapNotNull { (k, v) ->
                            OsType.fromKey(k)?.let { os -> os to (v as JsonPrimitive).content }
                        }
                        .toMap())

            else -> PlatformIndent(emptyMap())
        }
    }

    override fun serialize(encoder: Encoder, value: PlatformIndent) {
        val obj = buildJsonObject {
            value.values.forEach { (os, v) -> put(os.key, v) }
        }
        (encoder as JsonEncoder).encodeJsonElement(obj)
    }
}

@Serializable(with = AssetConfigSerializer::class)
data class AssetConfig(val values: Map<OsType, Map<ArchType, String>>) {
    fun supportedPlatforms(): Set<String> =
        values
            .flatMap { (os, archMap) ->
                archMap.keys.map { arch -> "${os.key}/${arch.key}" }
            }
            .toSet()

    fun resolve(): String? {
        val osMap = values[OsType.current] ?: return null
        return osMap[ArchType.current] ?: if (ArchType.current == ArchType.ARM64) osMap[ArchType.AARCH64] else null
    }
}

object AssetConfigSerializer : KSerializer<AssetConfig> {
    override val descriptor = buildClassSerialDescriptor("AssetConfig")

    override fun deserialize(decoder: Decoder): AssetConfig {
        val jsonDecoder = decoder as JsonDecoder
        val element = jsonDecoder.decodeJsonElement() as JsonObject
        val values =
            element.entries
                .mapNotNull { (osKey, osVal) ->
                    val os = OsType.fromKey(osKey) ?: return@mapNotNull null
                    val archMap: Map<ArchType, String> =
                        when (osVal) {
                            is JsonPrimitive -> mapOf(ArchType.X86_64 to osVal.content)
                            is JsonObject ->
                                osVal.entries
                                    .mapNotNull { (archKey, archVal) ->
                                        ArchType.fromKey(archKey)?.let { arch ->
                                            arch to (archVal as JsonPrimitive).content
                                        }
                                    }
                                    .toMap()

                            else -> emptyMap()
                        }
                    os to archMap
                }
                .toMap()
        return AssetConfig(values)
    }

    override fun serialize(encoder: Encoder, value: AssetConfig) {
        throw UnsupportedOperationException()
    }
}
