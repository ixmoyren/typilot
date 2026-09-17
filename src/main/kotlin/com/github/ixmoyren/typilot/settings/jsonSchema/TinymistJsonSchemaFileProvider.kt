package com.github.ixmoyren.typilot.settings.jsonSchema

import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory
import com.jetbrains.jsonSchema.extension.SchemaType
import com.jetbrains.jsonSchema.impl.JsonSchemaVersion
import java.util.UUID

/**
 * Associates the tinymist configuration editor with the JSON schema bundled in the plugin distribution, so the editor provides schema-based validation and completion.
 *
 * The editor is application-level but its backing light file must live in a project, so every editor gets a uniquely named file. Reusing a fixed name across projects makes the
 * platform cache the same light file's PSI in one project and reuse it in another, which breaks navigation and logs `LightFileViewProviderCache` errors.
 */
class TinymistJsonSchemaFileProvider : JsonSchemaFileProvider {

    override fun isAvailable(file: VirtualFile): Boolean = isTinymistSettingsFile(file)

    override fun getName(): String = JSON_FILE_NAME

    override fun getSchemaFile(): VirtualFile? = JsonSchemaProviderFactory.getResourceFile(javaClass, SCHEMA_RESOURCE_PATH)

    override fun getSchemaType(): SchemaType = SchemaType.schema

    override fun getSchemaVersion(): JsonSchemaVersion = JsonSchemaVersion.SCHEMA_7

    override fun isUserVisible(): Boolean = false

    companion object {
        /** Name of the in-memory JSON file used by the settings editor. */
        const val JSON_FILE_NAME: String = "typilot.tinymist.settings.json"

        /** Prefix of the uniquely named in-memory JSON files created for settings editors. */
        const val FILE_NAME_PREFIX: String = "typilot.tinymist.settings."

        /** Classpath resource with the tinymist settings JSON schema. */
        private const val SCHEMA_RESOURCE_PATH: String = "/lsp/tinymist.settings.schema.json"

        private const val FILE_NAME_SUFFIX: String = ".json"

        /** Whether [file] is one of the in-memory files backing a tinymist settings editor. */
        fun isTinymistSettingsFile(file: VirtualFile): Boolean = file.name.startsWith(FILE_NAME_PREFIX) && file.name.endsWith(FILE_NAME_SUFFIX)

        /** A unique name for an in-memory settings editor file, so two editors (and two projects) never share the same light file. */
        fun uniqueFileName(): String = "$FILE_NAME_PREFIX${UUID.randomUUID()}$FILE_NAME_SUFFIX"
    }
}
