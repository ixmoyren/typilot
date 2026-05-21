package com.github.ixmoyren.typilot

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class TypalizeTest : BasePlatformTestCase() {
    fun testTypstVersion() {
        val version = version()
        assertEquals("typst 0.14.2", version)
    }
}
