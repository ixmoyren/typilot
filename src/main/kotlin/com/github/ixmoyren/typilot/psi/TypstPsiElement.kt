package com.github.ixmoyren.typilot.psi

import com.github.ixmoyren.typilot.TypstSyntaxKind
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.tree.util.children
import com.intellij.openapi.util.TextRange
import com.intellij.psi.ElementManipulators
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiLanguageInjectionHost

inline fun PsiElement.nextSiblingOf(
    stop: ((PsiElement) -> Boolean) = { false },
    inclusive: Boolean = false,
    condition: (PsiElement) -> Boolean
): PsiElement? {
    var next = if (inclusive) this else this.nextSibling
    while (next != null && !stop(next)) {
        if (condition(next)) return next
        next = next.nextSibling
    }
    return null
}

sealed interface TypstPsiElement : NavigatablePsiElement {
    fun accept(visitor: TypstPsiElementVisitor)

    val kind: TypstSyntaxKind?
        get() =
            when (val type = node.elementType) {
                is TypstElementType -> type.kind
                else -> return null
            }
}

sealed class ATypstPsiElement(node: ASTNode) : ASTWrapperPsiElement(node), TypstPsiElement {
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is TypstPsiElementVisitor) accept(visitor) else super.accept(visitor)
    }
}

sealed interface TypstMarkupPart

sealed interface TypstCodePart

sealed interface TypstMathPart

sealed interface TypstIgnored

sealed interface TypstRawPart

sealed interface TypstKeyword

sealed interface TypstBinOp

sealed interface TypstUnOp

sealed interface TypstAssignOp

sealed interface TypstExpr : TypstCodePart, TypstParam

sealed interface TypstLiteral : TypstExpr

sealed interface TypstKeywordLiteral : TypstKeyword, TypstLiteral

sealed interface TypstNumericLiteral : TypstLiteral

sealed interface TypstParam

sealed interface TypstImportPart

/** An invalid sequence of characters. */
class TypstErrorPsiElement(node: ASTNode) : ATypstPsiElement(node) {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitError(this)
    }
}

/**
 * A shebang: `#! ...`
 *
 * Is always a Leaf.
 */
class TypstShebangPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstIgnored {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitShebang(this)
    }
}

/**
 * A line comment: `// ...`.
 *
 * Is always a Leaf.
 */
class TypstLineCommentPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstIgnored {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitLineComment(this)
    }
}

/**
 * A block comment: `/* ... */`.
 *
 * Is always a Leaf.
 */
class TypstBlockCommentPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstIgnored {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitBlockComment(this)
    }
}

/**
 * The contents of a file or content block.
 *
 * Consists of markup elements:
 *  ```
 *  Node Markup:
 *      Node Strong:
 *          Node Star: *
 *          Node Markup:
 *              Node Text: Hi
 *          Node Star: *
 *      Node Space:
 *      Node Text: there!
 *  ```
 */
class TypstMarkupPsiElement(node: ASTNode) : ATypstPsiElement(node) {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitMarkup(this)
    }
}

/**
 * Plain text without markup.
 *
 * Always a leaf.
 */
class TypstTextPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstMarkupPart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitText(this)
    }
}

/** Whitespace in markup or math. Contains at most one newline in markup, as more indicate a paragraph break. */
class TypstSpacePsiElement(node: ASTNode) : ATypstPsiElement(node), TypstMarkupPart, TypstMathPart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitSpace(this)
    }
}

/** A forced line break: `\`. */
class TypstLinebreakPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstMarkupPart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitLinebreak(this)
    }
}

/** A paragraph break, indicated by one or multiple blank lines. */
class TypstParbreakPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstMarkupPart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitParbreak(this)
    }
}

/** An escape sequence: `\#`, `\u{1F5FA}`. */
class TypstEscapePsiElement(node: ASTNode) : ATypstPsiElement(node), TypstMarkupPart, TypstMathPart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitEscape(this)
    }
}

/** A shorthand for a Unicode codepoint. For example, `~` for non-breaking space or `-?` for a soft hyphen. */
class TypstShorthandPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstMarkupPart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitShorthand(this)
    }
}

/** A smart quote: `'` or `"`. */
class TypstSmartQuotePsiElement(node: ASTNode) : ATypstPsiElement(node), TypstMarkupPart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitSmartQuote(this)
    }
}

/** Strong content: `*Strong*`. */
class TypstStrongPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstMarkupPart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitStrong(this)
    }
}

/** Emphasized content: `_Emphasized_`. */
class TypstEmphPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstMarkupPart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitEmph(this)
    }
}

/** Raw text with optional syntax highlighting: `` `...` ``. */
class TypstRawPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstMarkupPart, TypstCodePart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitRaw(this)
    }

    override fun clone() = TypstRawPsiElement(node.clone() as ASTNode)
}

/** A language tag at the start of raw text: ``typ ``. */
class TypstRawLangPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstRawPart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitRawLang(this)
    }
}

/** A raw delimiter consisting of 1 or 3+ backticks: `` ` ``. */
class TypstRawDelimPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstRawPart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitRawDelim(this)
    }
}

/** A sequence of whitespace to ignore in a raw text: ` `. */
class TypstRawTrimmedPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstRawPart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitRawTrimmed(this)
    }
}

/** A hyperlink: `https://typst.org`. */
class TypstLinkPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstMarkupPart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitLink(this)
    }
}

/** A label: `<intro>`. */
class TypstLabelPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstMarkupPart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitLabel(this)
    }
}

/** A reference: `@target`, `@target[..]`. */
class TypstRefPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstMarkupPart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitRef(this)
    }
}

/** Introduces a reference: `@target`. */
class TypstRefMarkerPsiElement(node: ASTNode) : ATypstPsiElement(node) {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitRefMarker(this)
    }
}

/** A section heading: `= Introduction`. */
class TypstHeadingPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstMarkupPart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitHeading(this)
    }
}

/** Introduces a section heading: `=`, `==`, ... */
class TypstHeadingMarkerPsiElement(node: ASTNode) : ATypstPsiElement(node) {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitHeadingMarker(this)
    }
}

/** An item in a bullet list: `- ...`. */
class TypstListItemPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstMarkupPart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitListItem(this)
    }
}

/** Introduces a list item: `-`. */
class TypstListMarkerPsiElement(node: ASTNode) : ATypstPsiElement(node) {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitListMarker(this)
    }
}

/** An item in an enumeration (numbered list): `+ ...` or `1. ...`. */
class TypstEnumItemPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstMarkupPart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitEnumItem(this)
    }
}

/** Introduces an enumeration item: `+`, `1.`. */
class TypstEnumMarkerPsiElement(node: ASTNode) : ATypstPsiElement(node) {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitEnumMarker(this)
    }
}

/** An item in a term list: `/ Term: Details`. */
class TypstTermItemPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstMarkupPart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitTermItem(this)
    }
}

/** Introduces a term item: `/`. */
class TypstTermMarkerPsiElement(node: ASTNode) : ATypstPsiElement(node) {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitTermMarker(this)
    }
}

/** A mathematical equation: `$x$`, `$ x^2 $`. */
class TypstEquationPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstMarkupPart, TypstCodePart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitEquation(this)
    }
}

/** The contents of a mathematical equation: `x^2 + 1`. */
class TypstMathPsiElement(node: ASTNode) : ATypstPsiElement(node) {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitMath(this)
    }
}

/** A lone text fragment in math: `x`, `25`, `3.1415`, `=`, `|`, `[`. */
class TypstMathTextPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstMathPart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitMathText(this)
    }
}

/** An identifier in math: `pi`. */
class TypstMathIdentPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstMathPart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitMathIdent(this)
    }
}

/** A shorthand for a Unicode codepoint in math: `a <= b`. */
class TypstMathShorthandPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstMathPart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitMathShorthand(this)
    }
}

/** An alignment point in math: `&`. */
class TypstMathAlignPointPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstMathPart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitMathAlignPoint(this)
    }
}

/** Matched delimiters in math: `[x + y]`. */
class TypstMathDelimitedPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstMathPart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitMathDelimited(this)
    }
}

/**
 * A base with optional attachments in math: `a_1^2`.
 *
 * Contains ^superscripts, _subscripts and primes: `1''^2_3` ->
 *
 * ```
 * Node MathAttach:
 *     Node MathText: 1
 *     Node MathPrimes:
 *         Node Prime: '
 *         Node Prime: '
 *     Node Hat: ^
 *     Node MathText: 2
 *     Node Underscore: _
 *     Node MathText: 3
 * ```
 */
class TypstMathAttachPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstMathPart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitMathAttach(this)
    }
}

/** Grouped primes in math: `a'''`. */
class TypstMathPrimesPsiElement(node: ASTNode) : ATypstPsiElement(node) {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitMathPrimes(this)
    }
}

/** A fraction in math: `x/2`. */
class TypstMathFracPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstMathPart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitMathFrac(this)
    }
}

/** A root in math: `√x`, `∛x` or `∜x`. */
class TypstMathRootPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstMathPart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitMathRoot(this)
    }
}

/** A hash that switches into code mode: `#`. */
class TypstHashPsiElement(node: ASTNode) : ATypstPsiElement(node) {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitHash(this)
    }
}

/** A left curly brace, starting a code block: `{`. */
class TypstLeftBracePsiElement(node: ASTNode) : ATypstPsiElement(node) {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitLeftBrace(this)
    }
}

/** A right curly brace, terminating a code block: `}`. */
class TypstRightBracePsiElement(node: ASTNode) : ATypstPsiElement(node) {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitRightBrace(this)
    }
}

/** A left square bracket, starting a content block: `[`. */
class TypstLeftBracketPsiElement(node: ASTNode) : ATypstPsiElement(node) {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitLeftBracket(this)
    }
}

/** A right square bracket, terminating a content block: `]`. */
class TypstRightBracketPsiElement(node: ASTNode) : ATypstPsiElement(node) {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitRightBracket(this)
    }
}

/** A left round parenthesis, starting a grouped expression, collection, argument or parameter list: `(`. */
class TypstLeftParenPsiElement(node: ASTNode) : ATypstPsiElement(node) {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitLeftParen(this)
    }
}

/** A right round parenthesis, terminating a grouped expression, collection, argument or parameter list: `)`. */
class TypstRightParenPsiElement(node: ASTNode) : ATypstPsiElement(node) {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitRightParen(this)
    }
}

/** A comma separator in a sequence: `,`. */
class TypstCommaPsiElement(node: ASTNode) : ATypstPsiElement(node) {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitComma(this)
    }
}

/** A semicolon terminating an expression: `;`. */
class TypstSemicolonPsiElement(node: ASTNode) : ATypstPsiElement(node) {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitSemicolon(this)
    }
}

/** A colon between name/key and value in a dictionary, argument or parameter list, or between the term and body of a term list term: `:`. */
class TypstColonPsiElement(node: ASTNode) : ATypstPsiElement(node) {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitColon(this)
    }
}

/** The strong text toggle, multiplication operator, and wildcard import symbol: `*`. */
class TypstStarPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstBinOp {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitStar(this)
    }
}

/** Toggles emphasized text and indicates a subscript in math: `_`. */
class TypstUnderscorePsiElement(node: ASTNode) : ATypstPsiElement(node), TypstParam {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitUnderscore(this)
    }
}

/** Starts and ends a mathematical equation: `$`. */
class TypstDollarPsiElement(node: ASTNode) : ATypstPsiElement(node) {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitDollar(this)
    }
}

/** The unary plus and binary addition operator: `+`. */
class TypstPlusPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstBinOp, TypstUnOp {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitPlus(this)
    }
}

/** The unary negation and binary subtraction operator: `-`. */
class TypstMinusPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstBinOp, TypstUnOp {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitMinus(this)
    }
}

/** The division operator and fraction operator in math: `/`. */
class TypstSlashPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstBinOp {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitSlash(this)
    }
}

/** The superscript operator in math: `^`. */
class TypstHatPsiElement(node: ASTNode) : ATypstPsiElement(node) {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitHat(this)
    }
}

/** The prime in math: `'`. */
class TypstPrimePsiElement(node: ASTNode) : ATypstPsiElement(node) {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitPrime(this)
    }
}

/** The field access and method call operator: `.`. */
class TypstDotPsiElement(node: ASTNode) : ATypstPsiElement(node) {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitDot(this)
    }
}

/** The assignment operator: `=`. */
class TypstEqPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstAssignOp {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitEq(this)
    }
}

/** The equality operator: `==`. */
class TypstEqEqPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstBinOp {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitEqEq(this)
    }
}

/** The inequality operator: `!=`. */
class TypstExclEqPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstBinOp {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitExclEq(this)
    }
}

/** The less-than operator: `<`. */
class TypstLtPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstBinOp {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitLt(this)
    }
}

/** The less-than or equal operator: `<=`. */
class TypstLtEqPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstBinOp {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitLtEq(this)
    }
}

/** The greater-than operator: `>`. */
class TypstGtPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstBinOp {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitGt(this)
    }
}

/** The greater-than or equal operator: `>=`. */
class TypstGtEqPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstBinOp {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitGtEq(this)
    }
}

/** The add-assign operator: `+=`. */
class TypstPlusEqPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstAssignOp {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitPlusEq(this)
    }
}

/** The subtract-assign operator: `-=`. */
class TypstHyphEqPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstAssignOp {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitHyphEq(this)
    }
}

/** The multiply-assign operator: `*=`. */
class TypstStarEqPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstAssignOp {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitStarEq(this)
    }
}

/** The divide-assign operator: `/=`. */
class TypstSlashEqPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstAssignOp {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitSlashEq(this)
    }
}

/** Indicates a spread or sink: `..`. */
class TypstDotsPsiElement(node: ASTNode) : ATypstPsiElement(node) {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitDots(this)
    }
}

/** An arrow between a closure's parameters and body: `=>`. */
class TypstArrowPsiElement(node: ASTNode) : ATypstPsiElement(node) {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitArrow(this)
    }
}

/** A root: `√`, `∛` or `∜`. */
class TypstRootPsiElement(node: ASTNode) : ATypstPsiElement(node) {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitRoot(this)
    }
}

/** The `not` operator. */
class TypstNotPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstUnOp, TypstKeyword {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitNot(this)
    }
}

/** The `and` operator. */
class TypstAndPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstBinOp, TypstKeyword {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitAnd(this)
    }
}

/** The `or` operator. */
class TypstOrPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstBinOp, TypstKeyword {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitOr(this)
    }
}

/** The `none` literal. */
class TypstNonePsiElement(node: ASTNode) : ATypstPsiElement(node), TypstKeywordLiteral {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitNone(this)
    }
}

/** The `auto` literal. */
class TypstAutoPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstKeywordLiteral {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitAuto(this)
    }
}

/** The `let` keyword. */
class TypstLetPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstKeyword {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitLet(this)
    }
}

/** The `set` keyword. */
class TypstSetPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstKeyword {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitSet(this)
    }
}

/** The `show` keyword. */
class TypstShowPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstKeyword {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitShow(this)
    }
}

/** The `context` keyword. */
class TypstContextPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstKeyword {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitContext(this)
    }
}

/** The `if` keyword. */
class TypstIfPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstKeyword {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitIf(this)
    }
}

/** The `else` keyword. */
class TypstElsePsiElement(node: ASTNode) : ATypstPsiElement(node), TypstKeyword {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitElse(this)
    }
}

/** The `for` keyword. */
class TypstForPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstKeyword {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitFor(this)
    }
}

/** The `in` keyword. */
class TypstInPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstKeyword, TypstBinOp {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitIn(this)
    }
}

/** The `while` keyword. */
class TypstWhilePsiElement(node: ASTNode) : ATypstPsiElement(node), TypstKeyword {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitWhile(this)
    }
}

/** The `break` keyword. */
class TypstBreakPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstKeyword {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitBreak(this)
    }
}

/** The `continue` keyword. */
class TypstContinuePsiElement(node: ASTNode) : ATypstPsiElement(node), TypstKeyword {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitContinue(this)
    }
}

/** The `return` keyword. */
class TypstReturnPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstKeyword {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitReturn(this)
    }
}

/** The `import` keyword. */
class TypstImportPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstKeyword {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitImport(this)
    }
}

/** The `include` keyword. */
class TypstIncludePsiElement(node: ASTNode) : ATypstPsiElement(node), TypstKeyword {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitInclude(this)
    }
}

/** The `as` keyword. */
class TypstAsPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstKeyword {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitAs(this)
    }
}

/** The contents of a code block. */
class TypstCodePsiElement(node: ASTNode) : ATypstPsiElement(node) {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitCode(this)
    }
}

/** A boolean: `true`, `false`. */
class TypstBoolPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstKeywordLiteral {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitBool(this)
    }
}

/** An integer: `120`. */
class TypstIntPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstNumericLiteral {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitInt(this)
    }
}

/** A floating-point number: `1.2`, `10e-4`. */
class TypstFloatPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstNumericLiteral {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitFloat(this)
    }
}

/** A numeric value with a unit: `12pt`, `3cm`, `2em`, `90deg`, `50%`. */
class TypstNumericPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstNumericLiteral {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitNumeric(this)
    }
}

/** A quoted string: `"..."`. */
class TypstStrPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstLiteral, TypstMathPart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitStr(this)
    }
}

/** A code block: `{ let x = 1; x + 2 }`. */
class TypstCodeBlockPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstExpr {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitCodeBlock(this)
    }
}

/** A content block: `[*Hi* there!]`. */
class TypstContentBlockPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstExpr {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitContentBlock(this)
    }
}

/** A grouped expression: `(1 + 2)`. */
class TypstParenthesizedPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstExpr {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitParenthesized(this)
    }
}

/** An array: `(1, "hi", 12cm)`. */
class TypstArrayPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstExpr {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitArray(this)
    }
}

/** A dictionary: `(thickness: 3pt, dash: "solid")`. */
class TypstDictPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstExpr {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitDict(this)
    }
}

/** A named pair: `thickness: 3pt`. */
class TypstNamedPairPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstParam {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitNamed(this)
    }
}

/** A keyed pair: `"spacy key": true`. */
class TypstKeyedPairPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstParam {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitKeyed(this)
    }
}

/** A unary operation: `-x`. */
class TypstUnaryPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstExpr {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitUnary(this)
    }
}

/** A binary operation: `a + b`. */
class TypstBinaryPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstExpr {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitBinary(this)
    }
}

/** A field access: `properties.age`. */
class TypstFieldAccessPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstExpr {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitFieldAccess(this)
    }
}

/** An invocation of a function or method: `f(x, y)`. */
class TypstFuncCallPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstExpr {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitFuncCall(this)
    }
}

/** A function call's argument list: `(12pt, y)`. */
class TypstArgsPsiElement(node: ASTNode) : ATypstPsiElement(node) {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitArgs(this)
    }
}

/** Spread arguments or an argument sink: `..x`. */
class TypstSpreadPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstParam {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitSpread(this)
    }
}

/** A closure: `(x, y) => z`. */
class TypstClosurePsiElement(node: ASTNode) : ATypstPsiElement(node), TypstExpr {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitClosure(this)
    }
}

/** A closure's parameters: `(x, y)`. */
class TypstParamsPsiElement(node: ASTNode) : ATypstPsiElement(node) {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitParams(this)
    }
}

/** A let binding: `let x = 1`. */
class TypstLetBindingPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstCodePart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitLetBinding(this)
    }
}

/** A set rule: `set text(...)`. */
class TypstSetRulePsiElement(node: ASTNode) : ATypstPsiElement(node), TypstExpr {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitSetRule(this)
    }
}

/** A show rule: `show heading: it => emph(it.body)`. */
class TypstShowRulePsiElement(node: ASTNode) : ATypstPsiElement(node), TypstCodePart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitShowRule(this)
    }
}

/** A contextual expression: `context text.lang`. */
class TypstContextualPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstExpr {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitContextual(this)
    }
}

/** An if-else conditional: `if x { y } else { z }`. */
class TypstConditionalPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstExpr {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitConditional(this)
    }
}

/** A while loop: `while x { y }`. */
class TypstWhileLoopPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstExpr {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitWhileLoop(this)
    }
}

/** A for loop: `for x in y { z }`. */
class TypstForLoopPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstExpr {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitForLoop(this)
    }
}

/** A module import: `import "utils.typ": a, b, c`. */
class TypstModuleImportPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstCodePart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitModuleImport(this)
    }
}

/** Items to import from a module: `a, b, c`. */
class TypstImportItemsPsiElement(node: ASTNode) : ATypstPsiElement(node) {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitImportItems(this)
    }
}

/** A path to an imported name from a submodule: `a.b.c`. */
class TypstImportItemPathPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstImportPart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitImportItemPath(this)
    }
}

/** A renamed import item: `a as d`. */
class TypstRenamedImportItemPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstImportPart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitRenamedImportItem(this)
    }
}

/** A module include: `include "chapter1.typ"`. */
class TypstModuleIncludePsiElement(node: ASTNode) : ATypstPsiElement(node), TypstExpr {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitModuleInclude(this)
    }
}

/** A break from a loop: `break`. */
class TypstLoopBreakPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstCodePart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitLoopBreak(this)
    }
}

/** A continued in a loop: `continue`. */
class TypstLoopContinuePsiElement(node: ASTNode) : ATypstPsiElement(node), TypstCodePart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitLoopContinue(this)
    }
}

/** A return from a function: `return`, `return x + 1`. */
class TypstFuncReturnPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstCodePart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitFuncReturn(this)
    }
}

/** A destructuring pattern: `(x, _, ..y)`. */
class TypstDestructuringPsiElement(node: ASTNode) : ATypstPsiElement(node) {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitDestructuring(this)
    }
}

/** A destructuring assignment expression: `(x, y) = (1, 2)`. */
class TypstDestructAssignmentPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstCodePart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitDestructAssignment(this)
    }
}

/** An in-code whitespace */
class TypstWhitespacePsiElement(node: ASTNode) : ATypstPsiElement(node), TypstCodePart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        // TODO
    }
}

/** An embedded code expression: `#f(1)` */
class TypstEmbeddedCodePsiElement(node: ASTNode) : ATypstPsiElement(node), TypstMarkupPart, TypstMathPart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitEmbeddedCode(this)
    }
}

/** A text in raw. */
class TypstRawTextPsiElement(node: ASTNode) : ATypstPsiElement(node), TypstRawPart {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitRawText(this)
    }
}

/** A declaration of an identifier: `it` in `let it = 1` */
class TypstIdentDeclPsiElement(node: ASTNode) : ATypstPsiElement(node) /*, PsiNamedElement*/ {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitIdentDecl(this)
    }
}

/** A reference to an identifier: `x` in `x + 1` */
class TypstIdentRefPsiElement(node: ASTNode) : ATypstPsiElement(node) {
    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitIdentRef(this)
    }
}

class TypstRawBlockPsiElement(node: ASTNode) : ATypstPsiElement(node), PsiLanguageInjectionHost {
    fun langTag(): String? = node.firstChildNode?.treeNext?.text

    fun textChildren(): List<PsiElement> =
        node
            .children()
            .filter { child ->
                when (val type = child.elementType) {
                    is TypstTokenType -> type.kind == TypstSyntaxKind.TEXT
                    else -> false
                }
            }
            .map { node -> node.psi!! }
            .toCollection(mutableListOf())

    fun getContentRange(): TextRange {
        var start = -1
        var end = -1
        for (child in node.children()) {
            if (child.elementType == TypstSyntaxKind.TEXT.tokenType) {
                if (start == -1) {
                    start = child.startOffsetInParent
                }
                end = child.startOffsetInParent + child.textLength
            }
        }
        return if (start == -1) TextRange.EMPTY_RANGE else TextRange(start, end)
    }

    override fun isValidHost(): Boolean = langTag() != null

    override fun updateText(text: String): PsiLanguageInjectionHost {
        return ElementManipulators.handleContentChange(this, text)
    }

    override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost?> {
        return object : LiteralTextEscaper<TypstRawBlockPsiElement>(this) {
            override fun decode(rangeInsideHost: TextRange, outChars: StringBuilder): Boolean {
                outChars.append(rangeInsideHost.substring(originalElement.text))
                return true
            }

            override fun getOffsetInHost(offset: Int, rangeInsideHost: TextRange): Int {
                return offset + rangeInsideHost.startOffset
            }

            override fun isOneLine(): Boolean {
                return false
            }
        }
    }

    override fun accept(visitor: TypstPsiElementVisitor) {
        visitor.visitRawBlock(this)
    }
}

val TypstSyntaxKindToPsiElementMap: Map<TypstSyntaxKind, (ASTNode) -> PsiElement> =
    mapOf(
        TypstSyntaxKind.ERROR to ::TypstErrorPsiElement,
        TypstSyntaxKind.SHEBANG to ::TypstShebangPsiElement,
        TypstSyntaxKind.LINE_COMMENT to ::TypstLineCommentPsiElement,
        TypstSyntaxKind.BLOCK_COMMENT to ::TypstBlockCommentPsiElement,
        TypstSyntaxKind.MARKUP to ::TypstMarkupPsiElement,
        TypstSyntaxKind.TEXT to ::TypstTextPsiElement,
        TypstSyntaxKind.SPACE to ::TypstSpacePsiElement,
        TypstSyntaxKind.LINEBREAK to ::TypstLinebreakPsiElement,
        TypstSyntaxKind.PARBREAK to ::TypstParbreakPsiElement,
        TypstSyntaxKind.ESCAPE to ::TypstEscapePsiElement,
        TypstSyntaxKind.SHORTHAND to ::TypstShorthandPsiElement,
        TypstSyntaxKind.SMART_QUOTE to ::TypstSmartQuotePsiElement,
        TypstSyntaxKind.STRONG to ::TypstStrongPsiElement,
        TypstSyntaxKind.EMPH to ::TypstEmphPsiElement,
        TypstSyntaxKind.RAW to ::TypstRawPsiElement,
        TypstSyntaxKind.RAW_LANG to ::TypstRawLangPsiElement,
        TypstSyntaxKind.RAW_DELIM to ::TypstRawDelimPsiElement,
        TypstSyntaxKind.RAW_TRIMMED to ::TypstRawTrimmedPsiElement,
        TypstSyntaxKind.LINK to ::TypstLinkPsiElement,
        TypstSyntaxKind.LABEL to ::TypstLabelPsiElement,
        TypstSyntaxKind.REF to ::TypstRefPsiElement,
        TypstSyntaxKind.REF_MARKER to ::TypstRefMarkerPsiElement,
        TypstSyntaxKind.HEADING to ::TypstHeadingPsiElement,
        TypstSyntaxKind.HEADING_MARKER to ::TypstHeadingMarkerPsiElement,
        TypstSyntaxKind.LIST_ITEM to ::TypstListItemPsiElement,
        TypstSyntaxKind.LIST_MARKER to ::TypstListMarkerPsiElement,
        TypstSyntaxKind.ENUM_ITEM to ::TypstEnumItemPsiElement,
        TypstSyntaxKind.ENUM_MARKER to ::TypstEnumMarkerPsiElement,
        TypstSyntaxKind.TERM_ITEM to ::TypstTermItemPsiElement,
        TypstSyntaxKind.TERM_MARKER to ::TypstTermMarkerPsiElement,
        TypstSyntaxKind.EQUATION to ::TypstEquationPsiElement,
        TypstSyntaxKind.MATH to ::TypstMathPsiElement,
        TypstSyntaxKind.MATH_TEXT to ::TypstMathTextPsiElement,
        TypstSyntaxKind.MATH_IDENT to ::TypstMathIdentPsiElement,
        TypstSyntaxKind.MATH_SHORTHAND to ::TypstMathShorthandPsiElement,
        TypstSyntaxKind.MATH_ALIGN_POINT to ::TypstMathAlignPointPsiElement,
        TypstSyntaxKind.MATH_DELIMITED to ::TypstMathDelimitedPsiElement,
        TypstSyntaxKind.MATH_ATTACH to ::TypstMathAttachPsiElement,
        TypstSyntaxKind.MATH_PRIMES to ::TypstMathPrimesPsiElement,
        TypstSyntaxKind.MATH_FRAC to ::TypstMathFracPsiElement,
        TypstSyntaxKind.MATH_ROOT to ::TypstMathRootPsiElement,
        TypstSyntaxKind.HASH to ::TypstHashPsiElement,
        TypstSyntaxKind.LEFT_BRACE to ::TypstLeftBracePsiElement,
        TypstSyntaxKind.RIGHT_BRACE to ::TypstRightBracePsiElement,
        TypstSyntaxKind.LEFT_BRACKET to ::TypstLeftBracketPsiElement,
        TypstSyntaxKind.RIGHT_BRACKET to ::TypstRightBracketPsiElement,
        TypstSyntaxKind.LEFT_PAREN to ::TypstLeftParenPsiElement,
        TypstSyntaxKind.RIGHT_PAREN to ::TypstRightParenPsiElement,
        TypstSyntaxKind.COMMA to ::TypstCommaPsiElement,
        TypstSyntaxKind.SEMICOLON to ::TypstSemicolonPsiElement,
        TypstSyntaxKind.COLON to ::TypstColonPsiElement,
        TypstSyntaxKind.STAR to ::TypstStarPsiElement,
        TypstSyntaxKind.UNDERSCORE to ::TypstUnderscorePsiElement,
        TypstSyntaxKind.DOLLAR to ::TypstDollarPsiElement,
        TypstSyntaxKind.PLUS to ::TypstPlusPsiElement,
        TypstSyntaxKind.MINUS to ::TypstMinusPsiElement,
        TypstSyntaxKind.SLASH to ::TypstSlashPsiElement,
        TypstSyntaxKind.HAT to ::TypstHatPsiElement,
        TypstSyntaxKind.DOT to ::TypstDotPsiElement,
        TypstSyntaxKind.EQ to ::TypstEqPsiElement,
        TypstSyntaxKind.EQ_EQ to ::TypstEqEqPsiElement,
        TypstSyntaxKind.EXCL_EQ to ::TypstExclEqPsiElement,
        TypstSyntaxKind.LT to ::TypstLtPsiElement,
        TypstSyntaxKind.LT_EQ to ::TypstLtEqPsiElement,
        TypstSyntaxKind.GT to ::TypstGtPsiElement,
        TypstSyntaxKind.GT_EQ to ::TypstGtEqPsiElement,
        TypstSyntaxKind.PLUS_EQ to ::TypstPlusEqPsiElement,
        TypstSyntaxKind.HYPH_EQ to ::TypstHyphEqPsiElement,
        TypstSyntaxKind.STAR_EQ to ::TypstStarEqPsiElement,
        TypstSyntaxKind.SLASH_EQ to ::TypstSlashEqPsiElement,
        TypstSyntaxKind.DOTS to ::TypstDotsPsiElement,
        TypstSyntaxKind.ARROW to ::TypstArrowPsiElement,
        TypstSyntaxKind.ROOT to ::TypstRootPsiElement,
        TypstSyntaxKind.NOT to ::TypstNotPsiElement,
        TypstSyntaxKind.AND to ::TypstAndPsiElement,
        TypstSyntaxKind.OR to ::TypstOrPsiElement,
        TypstSyntaxKind.NONE to ::TypstNonePsiElement,
        TypstSyntaxKind.AUTO to ::TypstAutoPsiElement,
        TypstSyntaxKind.LET to ::TypstLetPsiElement,
        TypstSyntaxKind.SET to ::TypstSetPsiElement,
        TypstSyntaxKind.SHOW to ::TypstShowPsiElement,
        TypstSyntaxKind.CONTEXT to ::TypstContextPsiElement,
        TypstSyntaxKind.IF to ::TypstIfPsiElement,
        TypstSyntaxKind.ELSE to ::TypstElsePsiElement,
        TypstSyntaxKind.FOR to ::TypstForPsiElement,
        TypstSyntaxKind.IN to ::TypstInPsiElement,
        TypstSyntaxKind.WHILE to ::TypstWhilePsiElement,
        TypstSyntaxKind.BREAK to ::TypstBreakPsiElement,
        TypstSyntaxKind.CONTINUE to ::TypstContinuePsiElement,
        TypstSyntaxKind.RETURN to ::TypstReturnPsiElement,
        TypstSyntaxKind.IMPORT to ::TypstImportPsiElement,
        TypstSyntaxKind.INCLUDE to ::TypstIncludePsiElement,
        TypstSyntaxKind.AS to ::TypstAsPsiElement,
        TypstSyntaxKind.CODE to ::TypstCodePsiElement,
        TypstSyntaxKind.BOOL to ::TypstBoolPsiElement,
        TypstSyntaxKind.INT to ::TypstIntPsiElement,
        TypstSyntaxKind.FLOAT to ::TypstFloatPsiElement,
        TypstSyntaxKind.NUMERIC to ::TypstNumericPsiElement,
        TypstSyntaxKind.STR to ::TypstStrPsiElement,
        TypstSyntaxKind.CODE_BLOCK to ::TypstCodeBlockPsiElement,
        TypstSyntaxKind.CONTENT_BLOCK to ::TypstContentBlockPsiElement,
        TypstSyntaxKind.PARENTHESIZED to ::TypstParenthesizedPsiElement,
        TypstSyntaxKind.ARRAY to ::TypstArrayPsiElement,
        TypstSyntaxKind.DICT to ::TypstDictPsiElement,
        TypstSyntaxKind.NAMED to ::TypstNamedPairPsiElement,
        TypstSyntaxKind.KEYED to ::TypstKeyedPairPsiElement,
        TypstSyntaxKind.UNARY to ::TypstUnaryPsiElement,
        TypstSyntaxKind.BINARY to ::TypstBinaryPsiElement,
        TypstSyntaxKind.FIELD_ACCESS to ::TypstFieldAccessPsiElement,
        TypstSyntaxKind.FUNC_CALL to ::TypstFuncCallPsiElement,
        TypstSyntaxKind.ARGS to ::TypstArgsPsiElement,
        TypstSyntaxKind.SPREAD to ::TypstSpreadPsiElement,
        TypstSyntaxKind.CLOSURE to ::TypstClosurePsiElement,
        TypstSyntaxKind.PARAMS to ::TypstParamsPsiElement,
        TypstSyntaxKind.LET_BINDING to ::TypstLetBindingPsiElement,
        TypstSyntaxKind.SET_RULE to ::TypstSetRulePsiElement,
        TypstSyntaxKind.SHOW_RULE to ::TypstShowRulePsiElement,
        TypstSyntaxKind.CONTEXTUAL to ::TypstContextualPsiElement,
        TypstSyntaxKind.CONDITIONAL to ::TypstConditionalPsiElement,
        TypstSyntaxKind.WHILE_LOOP to ::TypstWhileLoopPsiElement,
        TypstSyntaxKind.FOR_LOOP to ::TypstForLoopPsiElement,
        TypstSyntaxKind.MODULE_IMPORT to ::TypstModuleImportPsiElement,
        TypstSyntaxKind.IMPORT_ITEMS to ::TypstImportItemsPsiElement,
        TypstSyntaxKind.IMPORT_ITEM_PATH to ::TypstImportItemPathPsiElement,
        TypstSyntaxKind.RENAMED_IMPORT_ITEM to ::TypstRenamedImportItemPsiElement,
        TypstSyntaxKind.MODULE_INCLUDE to ::TypstModuleIncludePsiElement,
        TypstSyntaxKind.LOOP_BREAK to ::TypstLoopBreakPsiElement,
        TypstSyntaxKind.LOOP_CONTINUE to ::TypstLoopContinuePsiElement,
        TypstSyntaxKind.FUNC_RETURN to ::TypstFuncReturnPsiElement,
        TypstSyntaxKind.DESTRUCTURING to ::TypstDestructuringPsiElement,
        TypstSyntaxKind.DESTRUCT_ASSIGNMENT to ::TypstDestructAssignmentPsiElement,
    )
