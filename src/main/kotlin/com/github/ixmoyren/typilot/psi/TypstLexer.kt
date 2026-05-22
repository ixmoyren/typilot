package com.github.ixmoyren.typilot.psi

import com.github.ixmoyren.typilot.Token
import com.github.ixmoyren.typilot.TypstParser
import com.intellij.lexer.LexerBase
import com.intellij.psi.tree.IElementType

class TypstLexer : LexerBase() {
    private var buffer: CharSequence = ""
    private var startOffset: Int = 0
    private var endOffset: Int = 0
    private var tokens: List<Token> = emptyList()
    private var index: Int = 0

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        this.buffer = buffer
        this.startOffset = startOffset
        this.endOffset = endOffset

        val text = buffer.subSequence(startOffset, endOffset).toString()
        this.tokens = parser.parseMarkup(text)
        this.index = 0
    }

    override fun advance() {
        index++
    }

    override fun getBufferSequence(): CharSequence {
        return buffer
    }

    override fun getTokenType(): IElementType? {
        val token = currentToken() ?: return null
        return token.kind.tokenType
    }

    override fun getTokenStart(): Int {
        val token = currentToken() ?: return endOffset
        return startOffset + token.start.toInt()
    }

    override fun getTokenEnd(): Int {
        val token = currentToken() ?: return endOffset
        return startOffset + token.end.toInt()
    }

    override fun getBufferEnd(): Int = endOffset

    override fun getState(): Int = index

    private fun currentToken(): Token? = if (index >= tokens.size) null else tokens[index]

    companion object {
        val parser: TypstParser by lazy { TypstParser() }
    }
}
