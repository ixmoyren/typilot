package com.github.ixmoyren.typilot.psi

import com.github.ixmoyren.typilot.TypstSyntaxKind

val TypstSyntaxKind.Companion.KEYWORD_SET: Set<TypstSyntaxKind> by lazy {
    setOf(
        TypstSyntaxKind.NOT,
        TypstSyntaxKind.AND,
        TypstSyntaxKind.OR,
        TypstSyntaxKind.NONE,
        TypstSyntaxKind.AUTO,
        TypstSyntaxKind.LET,
        TypstSyntaxKind.SET,
        TypstSyntaxKind.SHOW,
        TypstSyntaxKind.CONTEXT,
        TypstSyntaxKind.IF,
        TypstSyntaxKind.ELSE,
        TypstSyntaxKind.FOR,
        TypstSyntaxKind.IN,
        TypstSyntaxKind.WHILE,
        TypstSyntaxKind.BREAK,
        TypstSyntaxKind.CONTINUE,
        TypstSyntaxKind.RETURN,
        TypstSyntaxKind.IMPORT,
        TypstSyntaxKind.INCLUDE,
        TypstSyntaxKind.AS)
}

val TypstSyntaxKind.Companion.IDENT_SET: Set<TypstSyntaxKind> by lazy { setOf(TypstSyntaxKind.IDENT, TypstSyntaxKind.MATH_IDENT) }

val TypstSyntaxKind.Companion.COMMENT_SET: Set<TypstSyntaxKind> by lazy { setOf(TypstSyntaxKind.LINE_COMMENT, TypstSyntaxKind.BLOCK_COMMENT, TypstSyntaxKind.SHEBANG) }

val TypstSyntaxKind.Companion.SPACE_SET: Set<TypstSyntaxKind> by lazy { setOf(TypstSyntaxKind.SPACE, TypstSyntaxKind.PARBREAK) }

val TypstSyntaxKind.Companion.LITERAL_SET: Set<TypstSyntaxKind> by lazy {
    setOf(
        TypstSyntaxKind.BOOL,
        TypstSyntaxKind.INT,
        TypstSyntaxKind.FLOAT,
        TypstSyntaxKind.NUMERIC,
        TypstSyntaxKind.STR,
        TypstSyntaxKind.TEXT,
        TypstSyntaxKind.LINK,
        TypstSyntaxKind.LABEL,
        TypstSyntaxKind.MATH_TEXT,
        TypstSyntaxKind.MATH_SHORTHAND)
}

val TypstSyntaxKind.Companion.OPERATOR_SET: Set<TypstSyntaxKind> by lazy {
    setOf(
        TypstSyntaxKind.HASH,
        TypstSyntaxKind.LEFT_BRACE,
        TypstSyntaxKind.RIGHT_BRACE,
        TypstSyntaxKind.LEFT_BRACKET,
        TypstSyntaxKind.RIGHT_BRACKET,
        TypstSyntaxKind.LEFT_PAREN,
        TypstSyntaxKind.RIGHT_PAREN,
        TypstSyntaxKind.COMMA,
        TypstSyntaxKind.SEMICOLON,
        TypstSyntaxKind.COLON,
        TypstSyntaxKind.STAR,
        TypstSyntaxKind.UNDERSCORE,
        TypstSyntaxKind.DOLLAR,
        TypstSyntaxKind.PLUS,
        TypstSyntaxKind.MINUS,
        TypstSyntaxKind.SLASH,
        TypstSyntaxKind.HAT,
        TypstSyntaxKind.DOT,
        TypstSyntaxKind.EQ,
        TypstSyntaxKind.EQ_EQ,
        TypstSyntaxKind.EXCL_EQ,
        TypstSyntaxKind.LT,
        TypstSyntaxKind.LT_EQ,
        TypstSyntaxKind.GT,
        TypstSyntaxKind.GT_EQ,
        TypstSyntaxKind.PLUS_EQ,
        TypstSyntaxKind.HYPH_EQ,
        TypstSyntaxKind.STAR_EQ,
        TypstSyntaxKind.SLASH_EQ,
        TypstSyntaxKind.DOTS,
        TypstSyntaxKind.ARROW,
        TypstSyntaxKind.ROOT,
        TypstSyntaxKind.MATH_ALIGN_POINT)
}
