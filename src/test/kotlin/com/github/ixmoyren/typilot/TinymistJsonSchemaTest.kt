package com.github.ixmoyren.typilot

import com.github.ixmoyren.typilot.settings.jsonSchema.TinymistConfigurationJsonTextField
import com.github.ixmoyren.typilot.settings.jsonSchema.TinymistJsonSchemaFileProvider
import com.github.ixmoyren.typilot.settings.jsonSchema.TinymistJsonSchemaProviderFactory
import com.google.gson.JsonParser
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory
import com.jetbrains.jsonSchema.ide.JsonSchemaService

class TinymistJsonSchemaTest : BasePlatformTestCase() {

    fun testProviderFactoryIsRegistered() {
        val factories = JsonSchemaProviderFactory.EP_NAME.extensionList
        assertTrue("TinymistJsonSchemaProviderFactory is not registered", factories.any { it is TinymistJsonSchemaProviderFactory })
    }

    fun testProviderMatchesEditorFileNameAndExposesSchema() {
        val provider = TinymistJsonSchemaFileProvider()

        val matching = myFixture.tempDirFixture.createFile(TinymistJsonSchemaFileProvider.JSON_FILE_NAME, "{}")
        val other = myFixture.tempDirFixture.createFile("unrelated.json", "{}")
        assertTrue(provider.isAvailable(matching))
        assertFalse(provider.isAvailable(other))

        val schemaFile = provider.schemaFile
        assertNotNull("JSON schema resource was not found", schemaFile)
        val schema = JsonParser.parseString(VfsUtilCore.loadText(schemaFile!!)).asJsonObject
        assertTrue(schema.getAsJsonObject("properties").has("formatterMode"))
        assertTrue(schema.getAsJsonObject("properties").has("semanticTokens"))
    }

    fun testJsonEditorIsAssociatedWithTinymistSchema() {
        val editor = TinymistConfigurationJsonTextField(project)
        Disposer.register(testRootDisposable, editor)

        val file = editor.underlyingFile
        assertNotNull("The JSON editor is not backed by a file", file)
        assertEquals(TinymistJsonSchemaFileProvider.JSON_FILE_NAME, file!!.name)

        val schemaFiles = JsonSchemaService.Impl.get(project).getSchemaFilesForFile(file)
        assertTrue("The tinymist schema is not associated with the editor. Found: ${schemaFiles.map { it.name }}", schemaFiles.any { it.name == "tinymist.settings.schema.json" })
    }

    fun testJsonEditorReportsInvalidJson() {
        val editor = TinymistConfigurationJsonTextField(project)
        Disposer.register(testRootDisposable, editor)

        editor.text = "{ \"formatterMode\": }"
        assertTrue("Expected a JSON syntax error", editor.hasErrors())

        editor.text = "{\n  \"formatterMode\": \"typstyle\"\n}"
        assertFalse("Valid JSON should not report errors", editor.hasErrors())
    }
}
