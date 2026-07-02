package com.github.ixmoyren.typilot

import com.github.ixmoyren.typalize.TypstSyntaxKind
import com.github.ixmoyren.typilot.psi.TypstSyntaxKindUtils
import com.github.ixmoyren.typilot.psi.elementType
import com.github.ixmoyren.typilot.psi.tokenType
import com.github.ixmoyren.typilot.psi.type
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class TypalizeTest : BasePlatformTestCase() {
    fun testTypstVersion() {
        val versionResult =
            typalizer.version() ?: throw IllegalStateException("The typst lexer returned null")
        if (versionResult.failure()) {
            throw IllegalStateException("The typst lexer couldn't work.", versionResult.error)
        }
        val version = versionResult.result ?: throw IllegalStateException("The typst version result is null")
        assertEquals("typst-syntax 0.15.0", version)
    }

    fun testTypstSyntaxKindEntries() {
        val map = TypstSyntaxKindUtils.entriesMap
        val endTokenName = TypstSyntaxKind.End::class.java.simpleName
        assertEquals(map[endTokenName], TypstSyntaxKind.End())
    }

    fun testTypstTokenize() {
        val text = ""
        val tokenResult =
            typalizer.tokenize(text) ?: throw IllegalStateException("The typst lexer returned null")
        if (tokenResult.failure()) {
            throw IllegalStateException("The typst lexer couldn't work.", tokenResult.error)
        }
        val tokens =
            tokenResult.result?.value ?: throw IllegalStateException("The typst tokenize result is null")
        val token = tokens.first()
        assertSame(TypstSyntaxKind.Markup().tokenType, token.type)
    }

    fun testTypstParse() {
        val text = " "
        val parseResult =
            typalizer.parse(text) ?: throw IllegalStateException("The typst parser returned null")
        if (parseResult.failure()) {
            throw IllegalStateException("The typst parser couldn't work.", parseResult.error)
        }
        val astNodes =
            parseResult.result?.value ?: throw IllegalStateException("The typst parse result is null")
        val astNode = astNodes.first()
        assertSame(TypstSyntaxKind.Markup().elementType, astNode.type)
    }

    fun testTypstTokenize2() {
        val text = "test\n "
        val tokenResult =
            typalizer.tokenize(text) ?: throw IllegalStateException("The typst lexer returned null")
        if (tokenResult.failure()) {
            throw IllegalStateException("The typst lexer couldn't work.", tokenResult.error)
        }
        val tokens =
            tokenResult.result?.value ?: throw IllegalStateException("The typst tokenize result is null")
        val token = tokens.first()
        assertSame(TypstSyntaxKind.Text().tokenType, token.type)
    }

    fun testTypstParse2() {
        val text = "test\n "
        val parseResult =
            typalizer.parse(text) ?: throw IllegalStateException("The typst parser returned null")
        if (parseResult.failure()) {
            throw IllegalStateException("The typst parser couldn't work.", parseResult.error)
        }
        val astNodes =
            parseResult.result?.value ?: throw IllegalStateException("The typst parse result is null")
        val astNode = astNodes.first()
        assertSame(TypstSyntaxKind.Markup().elementType, astNode.type)
        assertEquals(false, astNode.is_leaf)
    }
}
