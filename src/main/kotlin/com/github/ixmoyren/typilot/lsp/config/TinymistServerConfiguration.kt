package com.github.ixmoyren.typilot.lsp.config

import com.github.ixmoyren.typilot.settings.TinymistSettings
import com.google.gson.JsonParser

/**
 * The `tinymist` language server configuration that is sent through the LSP `initializationOptions` field.
 *
 * Previously this JSON was provided to LSP4IJ through a `ServerConfigurationContributor`. It is now a regular plugin setting so users can still tune the server, and the value is
 * used directly by the built-in LSP client.
 */
object TinymistServerConfiguration {

    fun currentConfiguration(): String = TinymistSettings.getInstance().serverConfiguration.ifBlank { DEFAULT_CONFIGURATION }

    /** Parses the configured JSON, falling back to the defaults when the user value is not valid JSON. */
    fun parse(): Any = runCatching { JsonParser.parseString(currentConfiguration()) }.getOrElse { JsonParser.parseString(DEFAULT_CONFIGURATION) }

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
