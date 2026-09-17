package com.github.ixmoyren.typilot

import com.github.ixmoyren.typilot.language.TypstFileType
import com.github.ixmoyren.typilot.navigation.TypstLspGotoDeclarationHandler
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class TypstLspGotoDeclarationHandlerTest : BasePlatformTestCase() {

    fun testHandlerIsRegistered() {
        val handlers = GotoDeclarationHandler.EP_NAME.extensionList
        assertTrue("TypstLspGotoDeclarationHandler is not registered", handlers.any { it is TypstLspGotoDeclarationHandler })
    }

    fun testIgnoresNonTypstFiles() {
        val file = myFixture.configureByText("Main.java", "class Main {}\n")
        val element = file.findElementAt(6)
        assertNotNull(element)

        val targets = TypstLspGotoDeclarationHandler().getGotoDeclarationTargets(element, 6, myFixture.editor)
        assertNull("non-Typst files must not be handled", targets)
    }

    fun testDoesNotThrowWithoutRunningLanguageServer() {
        val text = "#let greet(name) = [Hello, #name!]\n#greet(\"World\")\n"
        val file = myFixture.configureByText(TypstFileType, text)
        val offset = text.indexOf("#greet") + 2
        val element = file.findElementAt(offset)
        assertNotNull(element)

        // No assertion on the result: whether the server is running depends on the machine. The handler must simply stay well-behaved.
        TypstLspGotoDeclarationHandler().getGotoDeclarationTargets(element, offset, myFixture.editor)
    }
}
