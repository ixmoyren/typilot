package com.github.ixmoyren.typilot.psi

import com.github.ixmoyren.typilot.AstNode
import com.github.ixmoyren.typilot.TypstSyntaxKind
import com.github.ixmoyren.typilot.language.TypstLanguage
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

class TypstElementType(val kind: TypstSyntaxKind?, name: String = kind!!.name) :
    IElementType(name, TypstLanguage.INSTANCE) {
    override fun toString(): String {
        return "TypstElementType." + super.toString()
    }

    companion object {
        private val elementTypeMap: Map<TypstSyntaxKind, TypstElementType> by lazy {
            TypstSyntaxKind.entries.associateWith { TypstElementType(it) }
        }

        fun getElementType(kind: TypstSyntaxKind): TypstElementType = elementTypeMap.getValue(kind)

        val COMMENT_ELEMENTS: TokenSet = TokenSet.create(
            TypstSyntaxKind.LINE_COMMENT.elementType,
            TypstSyntaxKind.BLOCK_COMMENT.elementType,
            TypstSyntaxKind.SHEBANG.elementType
        )

        val KEYWORD_ELEMENTS: TokenSet = TokenSet.create(
            TypstSyntaxKind.NOT.elementType,
            TypstSyntaxKind.AND.elementType,
            TypstSyntaxKind.OR.elementType,
            TypstSyntaxKind.NONE.elementType,
            TypstSyntaxKind.AUTO.elementType,
            TypstSyntaxKind.LET.elementType,
            TypstSyntaxKind.SET.elementType,
            TypstSyntaxKind.SHOW.elementType,
            TypstSyntaxKind.CONTEXT.elementType,
            TypstSyntaxKind.IF.elementType,
            TypstSyntaxKind.ELSE.elementType,
            TypstSyntaxKind.FOR.elementType,
            TypstSyntaxKind.IN.elementType,
            TypstSyntaxKind.WHILE.elementType,
            TypstSyntaxKind.BREAK.elementType,
            TypstSyntaxKind.CONTINUE.elementType,
            TypstSyntaxKind.RETURN.elementType,
            TypstSyntaxKind.IMPORT.elementType,
            TypstSyntaxKind.INCLUDE.elementType,
            TypstSyntaxKind.AS.elementType
        )

        val IDENT_ELEMENTS: TokenSet =
            TokenSet.create(TypstSyntaxKind.IDENT.elementType, TypstSyntaxKind.MATH_IDENT.elementType)

        val LITERAL_ELEMENTS: TokenSet = TokenSet.create(
            TypstSyntaxKind.BOOL.elementType,
            TypstSyntaxKind.INT.elementType,
            TypstSyntaxKind.FLOAT.elementType,
            TypstSyntaxKind.NUMERIC.elementType,
            TypstSyntaxKind.STR.elementType,
            TypstSyntaxKind.TEXT.elementType,
            TypstSyntaxKind.LINK.elementType,
            TypstSyntaxKind.LABEL.elementType,
            TypstSyntaxKind.MATH_TEXT.elementType,
            TypstSyntaxKind.MATH_SHORTHAND.elementType
        )

        val OPERATOR_ELEMENTS: TokenSet = TokenSet.create(
            TypstSyntaxKind.HASH.elementType,
            TypstSyntaxKind.LEFT_BRACE.elementType,
            TypstSyntaxKind.RIGHT_BRACE.elementType,
            TypstSyntaxKind.LEFT_BRACKET.elementType,
            TypstSyntaxKind.RIGHT_BRACKET.elementType,
            TypstSyntaxKind.LEFT_PAREN.elementType,
            TypstSyntaxKind.RIGHT_PAREN.elementType,
            TypstSyntaxKind.COMMA.elementType,
            TypstSyntaxKind.SEMICOLON.elementType,
            TypstSyntaxKind.COLON.elementType,
            TypstSyntaxKind.STAR.elementType,
            TypstSyntaxKind.UNDERSCORE.elementType,
            TypstSyntaxKind.DOLLAR.elementType,
            TypstSyntaxKind.PLUS.elementType,
            TypstSyntaxKind.MINUS.elementType,
            TypstSyntaxKind.SLASH.elementType,
            TypstSyntaxKind.HAT.elementType,
            TypstSyntaxKind.DOT.elementType,
            TypstSyntaxKind.EQ.elementType,
            TypstSyntaxKind.EQ_EQ.elementType,
            TypstSyntaxKind.EXCL_EQ.elementType,
            TypstSyntaxKind.LT.elementType,
            TypstSyntaxKind.LT_EQ.elementType,
            TypstSyntaxKind.GT.elementType,
            TypstSyntaxKind.GT_EQ.elementType,
            TypstSyntaxKind.PLUS_EQ.elementType,
            TypstSyntaxKind.HYPH_EQ.elementType,
            TypstSyntaxKind.STAR_EQ.elementType,
            TypstSyntaxKind.SLASH_EQ.elementType,
            TypstSyntaxKind.DOTS.elementType,
            TypstSyntaxKind.ARROW.elementType,
            TypstSyntaxKind.ROOT.elementType,
            TypstSyntaxKind.BANG.elementType,
            TypstSyntaxKind.MATH_ALIGN_POINT.elementType
        )
    }
}

val TypstSyntaxKind.elementType
    get() = TypstElementType.getElementType(this)

val AstNode.type
    get() = this.kind.elementType
