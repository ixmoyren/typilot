package com.github.ixmoyren.typilot

import com.github.ixmoyren.typilot.language.TypstFileType
import com.github.ixmoyren.typilot.lsp.TypstLspClientDescriptor
import com.github.ixmoyren.typilot.lsp.TypstLspIntegrationProvider
import com.github.ixmoyren.typilot.lsp.services.TinymistFindService
import com.intellij.platform.lsp.api.LspClient
import com.intellij.platform.lsp.api.LspClientManager
import com.intellij.platform.lsp.api.LspServerState
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.eclipse.lsp4j.ConfigurationItem
import org.junit.Assume

/**
 * Verifies that the IntelliJ Platform's built-in LSP client starts the `tinymist` server through the plugin's [TypstLspIntegrationProvider]. Skipped when `tinymist` is not
 * available on the machine.
 */
class TypstLspIntegrationTest : BasePlatformTestCase() {

    fun testTinymistServerStartsThroughBuiltInLspClient() {
        Assume.assumeTrue("tinymist is not installed, skipping the integration test", TinymistFindService.getInstance().locate() != null)

        val file = myFixture.configureByText(TypstFileType, "#set page(width: 10cm)\n= Hello\n").virtualFile
        assertTrue(TypstLspIntegrationProvider.isTypstFile(file))

        val client = awaitRunningClient()
        assertNotNull("tinymist did not reach the Running state", client)
        assertNotNull("tinymist did not return an initialize result", client!!.initializeResult)
    }

    fun testDescriptorProvidesTypstLanguageAndConfiguration() {
        val file = myFixture.tempDirFixture.createFile("main.typ", "#let x = 1\n")
        val descriptor = TypstLspClientDescriptor(project)

        assertTrue(descriptor.isSupportedFile(file))
        assertEquals("typst", descriptor.getLanguageId(file))
        assertNotNull(descriptor.createInitializationOptions())
        assertNotNull(descriptor.getWorkspaceConfiguration(ConfigurationItem()))
    }

    private fun awaitRunningClient(): LspClient? {
        val manager = LspClientManager.getInstance(project)
        var client: LspClient? = null
        PlatformTestUtil.waitWithEventsDispatching(
            "tinymist did not start in time",
            {
                client = manager.getClients(TypstLspIntegrationProvider::class.java).firstOrNull { it.state == LspServerState.Running }
                client != null
            },
            60)
        return client
    }
}
