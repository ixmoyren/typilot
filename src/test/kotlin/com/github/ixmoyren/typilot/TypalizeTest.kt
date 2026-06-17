package com.github.ixmoyren.typilot

import com.github.ixmoyren.typalize.TypstSyntaxKind
import com.github.ixmoyren.typilot.psi.TypstSyntaxKindUtils
import com.github.ixmoyren.typilot.psi.tokenType
import com.github.ixmoyren.typilot.psi.type
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import kotlin.jvm.java

class TypalizeTest : BasePlatformTestCase() {
    fun testTypstVersion() {
        val version = typalizer.version().run {
            if (this == null || failure()) {
                throw Exception("The typst lexer couldn't work.", this?.error)
            }
            result ?: throw Exception("The typst version result is null")
        }
        assertEquals("typst-syntax 0.14.2", version)
    }

    fun testTypstSyntaxKindEntries() {
        val map = TypstSyntaxKindUtils.entriesMap
        val endTokenName = TypstSyntaxKind.End::class.java.simpleName
        assertEquals(map[endTokenName], TypstSyntaxKind.End())
    }

    fun testTypstTokenize() {
        val  text = ""
        val tokens = typalizer.tokenize(text).run {
            if (this == null || failure()) {
                throw Exception("The typst lexer couldn't work.", this?.error)
            }
            result ?: throw Exception("The typst tokenize result is null")
        }.value
        val token = tokens.first()
        assertSame(TypstSyntaxKind.Markup().tokenType, token.type)
    }
}
