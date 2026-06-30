package com.github.ixmoyren.typilot.lsp.config

import com.redhat.devtools.lsp4ij.settings.contributors.LanguageServerSettingsContributorBase
import com.redhat.devtools.lsp4ij.settings.contributors.ServerConfigurationContributor

@Suppress("UsePropertyAccessSyntax")
class TinymistSettingsContributor : LanguageServerSettingsContributorBase() {
    init {
        setServerConfigurationContributor(TinymistServerConfigurationContributor())
    }

    private class TinymistServerConfigurationContributor : ServerConfigurationContributor {

        override fun getDefaultConfigurationContent(): String =
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
            """.trimIndent()

        override fun getDefaultConfigurationSchemaContent(): String =
            // language=json
            $$"""
            {  
              "$schema": "http://json-schema.org/draft-07/schema#",
              "$id": "LSP4IJ/tinymist/settings.schema.json",
              "title": "Tinymist language server settings JSON schema",  
              "description": "JSON schema for tinymist language server settings.",  
              "type": "object",  
              "additionalProperties": false,  
              "properties": {  
                "outputPath": {  
                  "type": "string",  
                  "default": "",  
                  "description": "Output path template for exported files. Variables: $root (project root), $dir (parent dir of input file), $name (filename without extension). Example: $root/target/$dir/$name"  
                },  
                "exportTarget": {  
                  "type": "string",  
                  "default": "paged",  
                  "enum": ["paged", "html", "bundle"],  
                  "description": "Default export target format"  
                },  
                "exportPdf": {  
                  "type": "string",  
                  "default": "never",  
                  "enum": ["never", "onSave", "onType", "onDocumentHasTitle"],  
                  "description": "When to automatically export PDF"  
                },  
                "semanticTokens": {  
                  "type": "string",  
                  "default": "enable",  
                  "enum": ["enable", "disable"],  
                  "description": "Whether to enable semantic highlight tokens"  
                },  
                "formatterMode": {  
                  "type": "string",  
                  "default": "typstyle",  
                  "enum": ["disable", "typstyle", "typstfmt"],  
                  "description": "Formatter to use"  
                },  
                "formatterPrintWidth": {  
                  "type": ["number", "null"],  
                  "default": 120,  
                  "description": "Soft line wrap width for the formatter (in characters)"  
                },  
                "formatterIndentSize": {  
                  "type": ["number", "null"],  
                  "default": 2,  
                  "description": "Number of spaces per indentation level for the formatter"  
                },  
                "formatterProseWrap": {  
                  "type": ["boolean", "null"],  
                  "default": false,  
                  "description": "Whether the formatter should hard-wrap prose"  
                },  
                "projectResolution": {  
                  "type": "string",  
                  "default": "singleFile",  
                  "enum": ["singleFile", "lockDatabase"],  
                  "description": "Project resolution mode"  
                },  
                "syntaxOnly": {  
                  "type": "string",  
                  "default": "auto",  
                  "enum": ["auto", "enable", "disable", "onPowerSaving"],  
                  "description": "Syntax-only mode (disables compilation-dependent features)"  
                },  
                "rootPath": {  
                  "type": ["string", "null"],  
                  "default": null,  
                  "description": "Typst project root directory (overrides workspace root)"  
                },  
                "fontPaths": {  
                  "type": ["array", "null"],  
                  "items": { "type": "string" },  
                  "default": null,  
                  "description": "Additional font search paths"  
                },  
                "systemFonts": {  
                  "type": ["boolean", "null"],  
                  "default": true,  
                  "description": "Whether to load system fonts"  
                },  
                "typstExtraArgs": {  
                  "type": "array",  
                  "items": { "type": "string" },  
                  "default": [],  
                  "description": "Extra CLI arguments passed to the Typst compiler, e.g. [\"--input\", \"key=value\"]"  
                },  
                "compileStatus": {  
                  "type": "string",  
                  "default": "disable",  
                  "enum": ["enable", "disable"],  
                  "description": "Whether to push compile status notifications to the client"  
                },  
                "colorTheme": {  
                  "type": ["string", "null"],  
                  "default": null,  
                  "description": "Current color theme (\"dark\" or \"light\"), affects hoverPeriscope color inversion"  
                },  
                "hoverPeriscope": {  
                  "default": null,  
                  "description": "Render a document preview image on hover. Use \"enable\" for defaults, or pass an object to customize.",  
                  "anyOf": [  
                    { "type": "string", "enum": ["enable"] },  
                    {  
                      "type": "object",  
                      "properties": {  
                        "invertColor": { "type": "string", "enum": ["never", "auto", "always"], "default": "never" },  
                        "scale": { "type": "number", "default": 2.0 }  
                      }  
                    },  
                    { "type": "null" }  
                  ]  
                },  
                "triggerSuggest": {  
                  "type": "boolean",  
                  "default": false,  
                  "description": "Whether to automatically trigger completion suggestions after accepting a completion"  
                },  
                "triggerParameterHints": {  
                  "type": "boolean",  
                  "default": false,  
                  "description": "Whether to automatically trigger parameter hints after accepting a completion"  
                },  
                "triggerSuggestAndParameterHints": {  
                  "type": "boolean",  
                  "default": false,  
                  "description": "Whether to trigger both completion suggestions and parameter hints after accepting a completion"  
                },  
                "supportHtmlInMarkdown": {  
                  "type": "boolean",  
                  "default": false,  
                  "description": "Whether hover/documentation responses may contain HTML content"  
                },  
                "supportClientCodelens": {  
                  "type": "boolean",  
                  "default": false,  
                  "description": "Whether the client handles CodeLens commands itself. When true, the server returns tinymist.runCodeLens; when false, it returns direct commands like tinymist.exportPdf"  
                },  
                "supportExtendedCodeAction": {  
                  "type": "boolean",  
                  "default": false,  
                  "description": "Whether to enable extended CodeActions (tinymist.resolveCodeAction)"  
                },  
                "customizedShowDocument": {  
                  "type": "boolean",  
                  "default": false,  
                  "description": "Whether to use a customized showDocument notification"  
                },  
                "delegateFsRequests": {  
                  "type": "boolean",  
                  "default": false,  
                  "description": "Whether to delegate file system access to the client"  
                },  
                "development": {  
                  "type": "boolean",  
                  "default": false,  
                  "description": "Enable development mode"  
                },  
                "lint": {  
                  "type": "object",  
                  "properties": {  
                    "enabled": { "type": "boolean", "default": false },  
                    "when": { "type": "string", "enum": ["onSave", "onType"], "default": "onSave" }  
                  },  
                  "description": "Linting configuration"  
                },  
                "completion": {  
                  "type": "object",  
                  "description": "Completion feature configuration",  
                  "properties": {  
                    "triggerOnSnippetPlaceholders": { "type": "boolean", "default": false },  
                    "postfix": { "type": "boolean", "default": true },  
                    "postfixUfcs": { "type": "boolean", "default": true },  
                    "postfixUfcsLeft": { "type": "boolean", "default": true },  
                    "postfixUfcsRight": { "type": "boolean", "default": true },  
                    "symbol": { "type": "string", "enum": ["step", "stepless"], "default": "step" }  
                  }  
                },  
                "onEnter": {  
                  "type": "object",  
                  "description": "On-enter behavior configuration",  
                  "properties": {  
                    "enabled": { "type": "boolean", "default": true },  
                    "typingContinueCommentsOnNewline": { "type": "boolean", "default": true }  
                  }  
                },  
                "preview": {  
                  "type": "object",  
                  "description": "Preview feature configuration",  
                  "properties": {  
                    "refresh": { "type": "string", "enum": ["onSave", "onType"], "default": "onType" },  
                    "scrollSync": {  
                      "type": "string",  
                      "enum": ["never", "onSelectionChangeByMouse", "onSelectionChange"],  
                      "default": "onSelectionChangeByMouse"  
                    },  
                    "partialRendering": { "type": "boolean", "default": true },  
                    "invertColors": {  
                      "anyOf": [  
                        { "type": "string", "enum": ["never", "auto", "always"] },  
                        {  
                          "type": "object",  
                          "properties": {  
                            "rest": { "type": "string" },  
                            "image": { "type": "string" }  
                          }  
                        }  
                      ],  
                      "default": "never"  
                    },  
                    "cursorIndicator": { "type": "boolean", "default": false }  
                  }  
                }  
              }  
            }
            """.trimIndent()
    }
}