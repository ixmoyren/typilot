package com.github.ixmoyren.typilot.settings.jsonSchema

import com.intellij.openapi.project.Project
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory

/** Contributes the tinymist settings schema to the JSON plugin. Registered on the `JavaScript.JsonSchema.ProviderFactory` extension point in `plugin.xml`. */
class TinymistJsonSchemaProviderFactory : JsonSchemaProviderFactory {
    override fun getProviders(project: Project): List<JsonSchemaFileProvider> = listOf(TinymistJsonSchemaFileProvider())
}
