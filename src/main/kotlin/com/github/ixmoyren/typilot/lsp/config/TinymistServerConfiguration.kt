package com.github.ixmoyren.typilot.lsp.config

import com.github.ixmoyren.typilot.settings.TinymistSettings
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.longOrNull

/**
 * The `tinymist` language server configuration that is sent through the LSP `initializationOptions` field.
 *
 * Previously this JSON was provided to LSP4IJ through a `ServerConfigurationContributor`. It is now a regular plugin setting so users can still tune the server, and the value is
 * used directly by the built-in LSP client.
 */
object TinymistServerConfiguration {

    private val json = Json { ignoreUnknownKeys = true }

    fun currentConfiguration(): String = TinymistSettings.getInstance().serverConfiguration.ifBlank { DEFAULT_CONFIGURATION }

    /** Parses the configured JSON, falling back to the defaults when the user value is not a valid JSON object. */
    fun parse(): JsonObject = runCatching { json.parseToJsonElement(currentConfiguration()).jsonObject }.getOrElse { json.parseToJsonElement(DEFAULT_CONFIGURATION).jsonObject }

    /**
     * The configuration as plain JSON-compatible values (`Map`, `List`, `String`, `Long`, `Double`, `Boolean`, `null`).
     *
     * The built-in LSP client passes the value to LSP4J, which serializes it with Gson. Gson cannot serialize the kotlinx.serialization elements returned by [parse], so they are
     * lowered to plain values before being handed to the client.
     */
    fun parseForLsp(): Map<String, Any?> = parse().mapValues { (_, value) -> value.toPlainValue() }

    val DEFAULT_CONFIGURATION: String =
        // language=json
        $$"""
        {
          "outputPath": "$root/target/$dir/$name",
          "exportPdf": "never",
          "semanticTokens": "enable",
          "systemFonts": true,
          "fontPaths": [],
          "typstExtraArgs": [],
          "formatterMode": "typstyle",
          "formatterPrintWidth": 120,
          "formatterIndentSize": 2,
          "triggerSuggest": false,
          "triggerParameterHints": false,
          "triggerSuggestAndParameterHints": false,
          "supportHtmlInMarkdown": false,
          "supportClientCodelens": false,
          "supportExtendedCodeAction": false,
          "customizedShowDocument": false,
          "compileStatus": "disable"
        }
        """
            .trimIndent()
}

private fun JsonElement.toPlainValue(): Any? =
    when (this) {
        is JsonNull -> null
        is JsonObject -> entries.associate { (key, value) -> key to value.toPlainValue() }
        is JsonArray -> map { it.toPlainValue() }
        is JsonPrimitive -> if (isString) content else booleanOrNull ?: longOrNull ?: doubleOrNull ?: content
    }
