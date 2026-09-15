package com.github.ixmoyren.typilot.navigation

import kotlinx.serialization.json.Json

/**
 * Maps the name of a Typst built-in function, type or module to its page in the official documentation.
 *
 * `tinymist` reports no `textDocument/definition` target for built-ins because their definition has no source file, so the plugin provides this documentation page as the
 * navigation target instead. The table is generated from the Typst documentation search index at `https://typst.app/assets/search.json`.
 */
object TypstBuiltinDocumentation {

    private const val RESOURCE_PATH = "/lsp/typst-builtin-docs.json"

    private val json = Json

    private val documentationUrls: Map<String, String> by lazy {
        val stream = TypstBuiltinDocumentation::class.java.getResourceAsStream(RESOURCE_PATH) ?: return@lazy emptyMap()
        runCatching { json.decodeFromString<Map<String, String>>(stream.use { it.readBytes().toString(Charsets.UTF_8) }) }.getOrElse { emptyMap() }
    }

    /** The documentation URL of the built-in [name], or `null` when the name is not a documented built-in. */
    fun urlFor(name: String): String? = documentationUrls[name]
}
