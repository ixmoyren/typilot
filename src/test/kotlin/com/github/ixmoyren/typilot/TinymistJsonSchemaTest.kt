package com.github.ixmoyren.typilot

import com.github.ixmoyren.typilot.lsp.config.TinymistServerConfiguration
import com.github.ixmoyren.typilot.settings.TinymistSettingsForm
import com.github.ixmoyren.typilot.settings.jsonSchema.TinymistConfigurationJsonTextField
import com.github.ixmoyren.typilot.settings.jsonSchema.TinymistJsonSchemaFileProvider
import com.github.ixmoyren.typilot.settings.jsonSchema.TinymistJsonSchemaProviderFactory
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory
import com.jetbrains.jsonSchema.ide.JsonSchemaService
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

class TinymistJsonSchemaTest : BasePlatformTestCase() {

    fun testProviderFactoryIsRegistered() {
        val factories = JsonSchemaProviderFactory.EP_NAME.extensionList
        assertTrue("TinymistJsonSchemaProviderFactory is not registered", factories.any { it is TinymistJsonSchemaProviderFactory })
    }

    fun testProviderMatchesEditorFileNameAndExposesSchema() {
        val provider = TinymistJsonSchemaFileProvider()

        val matching = myFixture.tempDirFixture.createFile(TinymistJsonSchemaFileProvider.JSON_FILE_NAME, "{}")
        val unique = myFixture.tempDirFixture.createFile(TinymistJsonSchemaFileProvider.uniqueFileName(), "{}")
        val other = myFixture.tempDirFixture.createFile("unrelated.json", "{}")
        assertTrue(provider.isAvailable(matching))
        assertTrue(provider.isAvailable(unique))
        assertFalse(provider.isAvailable(other))

        val schemaFile = provider.schemaFile
        assertNotNull("JSON schema resource was not found", schemaFile)
        val properties = Json.parseToJsonElement(VfsUtilCore.loadText(schemaFile!!)).jsonObject["properties"]?.jsonObject
        assertNotNull(properties)
        assertTrue(properties!!.containsKey("formatterMode"))
        assertTrue(properties.containsKey("semanticTokens"))
    }

    fun testJsonEditorIsAssociatedWithTinymistSchema() {
        val editor = TinymistConfigurationJsonTextField(project)
        Disposer.register(testRootDisposable, editor)

        val file = editor.underlyingFile
        assertNotNull("The JSON editor is not backed by a file", file)
        assertTrue("Unexpected editor file name: ${file!!.name}", TinymistJsonSchemaFileProvider.isTinymistSettingsFile(file))

        val schemaFiles = JsonSchemaService.Impl.get(project).getSchemaFilesForFile(file)
        assertTrue("The tinymist schema is not associated with the editor. Found: ${schemaFiles.map { it.name }}", schemaFiles.any { it.name == "tinymist.settings.schema.json" })
    }

    fun testJsonEditorUsesUniqueLightFilePerEditor() {
        val first = TinymistConfigurationJsonTextField(project)
        val second = TinymistConfigurationJsonTextField(project)
        Disposer.register(testRootDisposable, first)
        Disposer.register(testRootDisposable, second)

        val firstFile = first.underlyingFile
        val secondFile = second.underlyingFile
        assertNotNull(firstFile)
        assertNotNull(secondFile)
        assertTrue(TinymistJsonSchemaFileProvider.isTinymistSettingsFile(firstFile!!))
        assertTrue(TinymistJsonSchemaFileProvider.isTinymistSettingsFile(secondFile!!))
        assertNotSame("editors must not share a light file", firstFile, secondFile)
        assertFalse("editors must not share a light file name", firstFile.name == secondFile.name)
    }

    fun testRestoreDefaultConfigurationResetsEditor() {
        val form = TinymistSettingsForm()
        Disposer.register(testRootDisposable, form)

        form.serverConfigurationEditor.text = "{ \"formatterMode\": \"disable\" }"
        form.serverConfiguration.set("{ \"formatterMode\": \"disable\" }")

        form.restoreDefaultServerConfiguration()

        assertEquals(TinymistServerConfiguration.DEFAULT_CONFIGURATION, form.serverConfigurationEditor.text)
        assertEquals(TinymistServerConfiguration.DEFAULT_CONFIGURATION, form.serverConfiguration.get())
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
