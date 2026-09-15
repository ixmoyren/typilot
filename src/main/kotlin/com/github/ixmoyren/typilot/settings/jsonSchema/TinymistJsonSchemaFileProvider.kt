package com.github.ixmoyren.typilot.settings.jsonSchema

import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory
import com.jetbrains.jsonSchema.extension.SchemaType
import com.jetbrains.jsonSchema.impl.JsonSchemaVersion

/**
 * Associates the tinymist configuration editor (`typilot.tinymist.settings.json`) with the JSON schema bundled in the plugin distribution, so the editor provides schema-based
 * validation and completion.
 */
class TinymistJsonSchemaFileProvider : JsonSchemaFileProvider {

    override fun isAvailable(file: VirtualFile): Boolean = file.name == JSON_FILE_NAME

    override fun getName(): String = JSON_FILE_NAME

    override fun getSchemaFile(): VirtualFile? = JsonSchemaProviderFactory.getResourceFile(javaClass, SCHEMA_RESOURCE_PATH)

    override fun getSchemaType(): SchemaType = SchemaType.schema

    override fun getSchemaVersion(): JsonSchemaVersion = JsonSchemaVersion.SCHEMA_7

    override fun isUserVisible(): Boolean = false

    companion object {
        /** Name of the in-memory JSON file used by the settings editor. */
        const val JSON_FILE_NAME: String = "typilot.tinymist.settings.json"

        /** Classpath resource with the tinymist settings JSON schema. */
        private const val SCHEMA_RESOURCE_PATH: String = "/lsp/tinymist.settings.schema.json"
    }
}
