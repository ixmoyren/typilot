package com.github.ixmoyren.typilot.psi

import com.github.ixmoyren.typalize.Token
import com.github.ixmoyren.typilot.typalizer
import com.intellij.lexer.LexerBase
import com.intellij.psi.tree.IElementType

class TypstLexer : LexerBase() {
    private var buffer: CharSequence = ""
    private var startOffset: Int = 0
    private var endOffset: Int = 0
    private lateinit var tokenList: List<Token>
    private var index: Int = 0

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        this.buffer = buffer
        this.startOffset = startOffset
        this.endOffset = endOffset
        val text = buffer.subSequence(startOffset, endOffset)
        this.tokenList =
            typalizer
                .tokenize(text)
                .run {
                    if (this == null || failure()) {
                        throw Exception("The typst lexer couldn't work.", this?.error)
                    }
                    result ?: throw Exception("The typst tokenize result is null")
                }
                .value
        this.index = 0
    }

    override fun advance() {
        index += 1
    }

    override fun getBufferSequence(): CharSequence = buffer

    override fun getTokenType(): IElementType? = currentToken()?.type

    override fun getTokenStart(): Int = currentToken()?.let { startOffset + it.start.toInt() } ?: endOffset

    override fun getTokenEnd(): Int = currentToken()?.let { startOffset + it.end.toInt() } ?: endOffset

    override fun getBufferEnd(): Int = endOffset

    override fun getState(): Int = currentToken()?.let { 1 } ?: 0

    override fun toString(): String = "Typst Lexer"

    private fun currentToken(): Token? = tokenList.getOrNull(index)
}
