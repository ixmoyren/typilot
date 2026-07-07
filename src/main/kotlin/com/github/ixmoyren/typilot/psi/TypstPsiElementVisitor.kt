package com.github.ixmoyren.typilot.psi

import com.intellij.psi.PsiElementVisitor

abstract class TypstPsiElementVisitor : PsiElementVisitor() {
    open fun visitTypstElement(element: TypstPsiElement) {
        element.acceptChildren(this)
    }

    /** An invalid sequence of characters. */
    open fun visitError(element: TypstErrorPsiElement) = visitTypstElement(element)

    /** A shebang: `#! ...` */
    open fun visitShebang(element: TypstShebangPsiElement) = visitTypstElement(element)

    /** A line comment: `// ...`. */
    open fun visitLineComment(element: TypstLineCommentPsiElement) = visitTypstElement(element)

    /** A block comment: `/* ... */`. */
    open fun visitBlockComment(element: TypstBlockCommentPsiElement) = visitTypstElement(element)

    /** The contents of a file or content block. */
    open fun visitMarkup(element: TypstMarkupPsiElement) = visitTypstElement(element)

    /** Plain text without markup. */
    open fun visitText(element: TypstTextPsiElement) = visitTypstElement(element)

    /** Whitespace. Contains at most one newline in markup, as more indicate a paragraph break. */
    open fun visitSpace(element: TypstSpacePsiElement) = visitTypstElement(element)

    /** A forced line break: `\`. */
    open fun visitLinebreak(element: TypstLinebreakPsiElement) = visitTypstElement(element)

    /** A paragraph break, indicated by one or multiple blank lines. */
    open fun visitParbreak(element: TypstParbreakPsiElement) = visitTypstElement(element)

    /** An escape sequence: `\#`, `\u{1F5FA}`. */
    open fun visitEscape(element: TypstEscapePsiElement) = visitTypstElement(element)

    /** A shorthand for a Unicode codepoint. For example, `~` for non-breaking space or `-?` for a soft hyphen. */
    open fun visitShorthand(element: TypstShorthandPsiElement) = visitTypstElement(element)

    /** A smart quote: `'` or `"`. */
    open fun visitSmartQuote(element: TypstSmartQuotePsiElement) = visitTypstElement(element)

    /** Strong content: `*Strong*`. */
    open fun visitStrong(element: TypstStrongPsiElement) = visitTypstElement(element)

    /** Emphasized content: `_Emphasized_`. */
    open fun visitEmph(element: TypstEmphPsiElement) = visitTypstElement(element)

    /** Raw text with optional syntax highlighting: `` `...` ``. */
    open fun visitRaw(element: TypstRawPsiElement) = visitTypstElement(element)

    /** A language tag at the start of raw text: ``typ ``. */
    open fun visitRawLang(element: TypstRawLangPsiElement) = visitTypstElement(element)

    /** A raw delimiter consisting of 1 or 3+ backticks: `` ` ``. */
    open fun visitRawDelim(element: TypstRawDelimPsiElement) = visitTypstElement(element)

    /** A sequence of whitespace to ignore in a raw text: ` `. */
    open fun visitRawTrimmed(element: TypstRawTrimmedPsiElement) = visitTypstElement(element)

    /** A hyperlink: `https://typst.org`. */
    open fun visitLink(element: TypstLinkPsiElement) = visitTypstElement(element)

    /** A label: `<intro>`. */
    open fun visitLabel(element: TypstLabelPsiElement) = visitTypstElement(element)

    /** A reference: `@target`, `@target[..]`. */
    open fun visitRef(element: TypstRefPsiElement) = visitTypstElement(element)

    /** Introduces a reference: `@target`. */
    open fun visitRefMarker(element: TypstRefMarkerPsiElement) = visitTypstElement(element)

    /** A section heading: `= Introduction`. */
    open fun visitHeading(element: TypstHeadingPsiElement) = visitTypstElement(element)

    /** Introduces a section heading: `=`, `==`, ... */
    open fun visitHeadingMarker(element: TypstHeadingMarkerPsiElement) = visitTypstElement(element)

    /** An item in a bullet list: `- ...`. */
    open fun visitListItem(element: TypstListItemPsiElement) = visitTypstElement(element)

    /** Introduces a list item: `-`. */
    open fun visitListMarker(element: TypstListMarkerPsiElement) = visitTypstElement(element)

    /** An item in an enumeration (numbered list): `+ ...` or `1. ...`. */
    open fun visitEnumItem(element: TypstEnumItemPsiElement) = visitTypstElement(element)

    /** Introduces an enumeration item: `+`, `1.`. */
    open fun visitEnumMarker(element: TypstEnumMarkerPsiElement) = visitTypstElement(element)

    /** An item in a term list: `/ Term: Details`. */
    open fun visitTermItem(element: TypstTermItemPsiElement) = visitTypstElement(element)

    /** Introduces a term item: `/`. */
    open fun visitTermMarker(element: TypstTermMarkerPsiElement) = visitTypstElement(element)

    /** A mathematical equation: `$x$`, `$ x^2 $`. */
    open fun visitEquation(element: TypstEquationPsiElement) = visitTypstElement(element)

    /** The contents of a mathematical equation: `x^2 + 1`. */
    open fun visitMath(element: TypstMathPsiElement) = visitTypstElement(element)

    /** A lone text fragment in math: `x`, `25`, `3.1415`, `=`, `|`, `[`. */
    open fun visitMathText(element: TypstMathTextPsiElement) = visitTypstElement(element)

    /** An identifier in math: `pi`. */
    open fun visitMathIdent(element: TypstMathIdentPsiElement) = visitTypstElement(element)

    /** A shorthand for a Unicode codepoint in math: `a <= b`. */
    open fun visitMathShorthand(element: TypstMathShorthandPsiElement) = visitTypstElement(element)

    /** An alignment point in math: `&`. */
    open fun visitMathAlignPoint(element: TypstMathAlignPointPsiElement) = visitTypstElement(element)

    /** Matched delimiters in math: `[x + y]`. */
    open fun visitMathDelimited(element: TypstMathDelimitedPsiElement) = visitTypstElement(element)

    /** A base with optional attachments in math: `a_1^2`. */
    open fun visitMathAttach(element: TypstMathAttachPsiElement) = visitTypstElement(element)

    /** Grouped primes in math: `a'''`. */
    open fun visitMathPrimes(element: TypstMathPrimesPsiElement) = visitTypstElement(element)

    /** A fraction in math: `x/2`. */
    open fun visitMathFrac(element: TypstMathFracPsiElement) = visitTypstElement(element)

    /** A root in math: `√x`, `∛x` or `∜x`. */
    open fun visitMathRoot(element: TypstMathRootPsiElement) = visitTypstElement(element)

    /** A field access in math: `arrow.r.long.double.bar`. */
    open fun visitMathFieldAccess(element: TypstMathFieldAccessPsiElement) = visitTypstElement(element)

    /** A function call in math: `mat(delim: "[", a, b; ..#($c$,), d)`. */
    open fun visitMathCall(element: TypstMathCallPsiElement) = visitTypstElement(element)

    /** Function arguments in math: `(delim: "[", a, b; ..#($c$,), d)`. */
    open fun visitMathArgs(element: TypstMathArgsPsiElement) = visitTypstElement(element)

    /** A hash that switches into code mode: `#`. */
    open fun visitHash(element: TypstHashPsiElement) = visitTypstElement(element)

    /** A left curly brace, starting a code block: `{`. */
    open fun visitLeftBrace(element: TypstLeftBracePsiElement) = visitTypstElement(element)

    /** A right curly brace, terminating a code block: `}`. */
    open fun visitRightBrace(element: TypstRightBracePsiElement) = visitTypstElement(element)

    /** A left square bracket, starting a content block: `[`. */
    open fun visitLeftBracket(element: TypstLeftBracketPsiElement) = visitTypstElement(element)

    /** A right square bracket, terminating a content block: `]`. */
    open fun visitRightBracket(element: TypstRightBracketPsiElement) = visitTypstElement(element)

    /** A left round parenthesis, starting a grouped expression, collection, argument or parameter list: `(`. */
    open fun visitLeftParen(element: TypstLeftParenPsiElement) = visitTypstElement(element)

    /** A right round parenthesis, terminating a grouped expression, collection, argument or parameter list: `)`. */
    open fun visitRightParen(element: TypstRightParenPsiElement) = visitTypstElement(element)

    /** A comma separator in a sequence: `,`. */
    open fun visitComma(element: TypstCommaPsiElement) = visitTypstElement(element)

    /** A semicolon terminating an expression: `;`. */
    open fun visitSemicolon(element: TypstSemicolonPsiElement) = visitTypstElement(element)

    /** A colon between name/key and value in a dictionary, argument or parameter list, or between the term and body of a term list term: `:`. */
    open fun visitColon(element: TypstColonPsiElement) = visitTypstElement(element)

    /** The strong text toggle, multiplication operator, and wildcard import symbol: `*`. */
    open fun visitStar(element: TypstStarPsiElement) = visitTypstElement(element)

    /** Toggles emphasized text and indicates a subscript in math: `_`. */
    open fun visitUnderscore(element: TypstUnderscorePsiElement) = visitTypstElement(element)

    /** Starts and ends a mathematical equation: `$`. */
    open fun visitDollar(element: TypstDollarPsiElement) = visitTypstElement(element)

    /** The unary plus and binary addition operator: `+`. */
    open fun visitPlus(element: TypstPlusPsiElement) = visitTypstElement(element)

    /** The unary negation and binary subtraction operator: `-`. */
    open fun visitMinus(element: TypstMinusPsiElement) = visitTypstElement(element)

    /** The division operator and fraction operator in math: `/`. */
    open fun visitSlash(element: TypstSlashPsiElement) = visitTypstElement(element)

    /** The superscript operator in math: `^`. */
    open fun visitHat(element: TypstHatPsiElement) = visitTypstElement(element)

    /** The prime in math: `'`. */
    open fun visitPrime(element: TypstPrimePsiElement) = visitTypstElement(element)

    /** The field access and method call operator: `.`. */
    open fun visitDot(element: TypstDotPsiElement) = visitTypstElement(element)

    /** The assignment operator: `=`. */
    open fun visitEq(element: TypstEqPsiElement) = visitTypstElement(element)

    /** The equality operator: `==`. */
    open fun visitEqEq(element: TypstEqEqPsiElement) = visitTypstElement(element)

    /** The inequality operator: `!=`. */
    open fun visitExclEq(element: TypstExclEqPsiElement) = visitTypstElement(element)

    /** The less-than operator: `<`. */
    open fun visitLt(element: TypstLtPsiElement) = visitTypstElement(element)

    /** The less-than or equal operator: `<=`. */
    open fun visitLtEq(element: TypstLtEqPsiElement) = visitTypstElement(element)

    /** The greater-than operator: `>`. */
    open fun visitGt(element: TypstGtPsiElement) = visitTypstElement(element)

    /** The greater-than or equal operator: `>=`. */
    open fun visitGtEq(element: TypstGtEqPsiElement) = visitTypstElement(element)

    /** The add-assign operator: `+=`. */
    open fun visitPlusEq(element: TypstPlusEqPsiElement) = visitTypstElement(element)

    /** The subtract-assign operator: `-=`. */
    open fun visitHyphEq(element: TypstHyphEqPsiElement) = visitTypstElement(element)

    /** The multiply-assign operator: `*=`. */
    open fun visitStarEq(element: TypstStarEqPsiElement) = visitTypstElement(element)

    /** The divide-assign operator: `/=`. */
    open fun visitSlashEq(element: TypstSlashEqPsiElement) = visitTypstElement(element)

    /** Indicates a spread or sink: `..`. */
    open fun visitDots(element: TypstDotsPsiElement) = visitTypstElement(element)

    /** An arrow between a closure's parameters and body: `=>`. */
    open fun visitArrow(element: TypstArrowPsiElement) = visitTypstElement(element)

    /** A root: `√`, `∛` or `∜`. */
    open fun visitRoot(element: TypstRootPsiElement) = visitTypstElement(element)

    /** An exclamation mark; groups with directly preceding text in math: `!`. */
    open fun visitBang(element: TypstBangPsiElement) = visitTypstElement(element)

    /** The `not` operator. */
    open fun visitNot(element: TypstNotPsiElement) = visitTypstElement(element)

    /** The `and` operator. */
    open fun visitAnd(element: TypstAndPsiElement) = visitTypstElement(element)

    /** The `or` operator. */
    open fun visitOr(element: TypstOrPsiElement) = visitTypstElement(element)

    /** The `none` literal. */
    open fun visitNone(element: TypstNonePsiElement) = visitTypstElement(element)

    /** The `auto` literal. */
    open fun visitAuto(element: TypstAutoPsiElement) = visitTypstElement(element)

    /** The `let` keyword. */
    open fun visitLet(element: TypstLetPsiElement) = visitTypstElement(element)

    /** The `set` keyword. */
    open fun visitSet(element: TypstSetPsiElement) = visitTypstElement(element)

    /** The `show` keyword. */
    open fun visitShow(element: TypstShowPsiElement) = visitTypstElement(element)

    /** The `context` keyword. */
    open fun visitContext(element: TypstContextPsiElement) = visitTypstElement(element)

    /** The `if` keyword. */
    open fun visitIf(element: TypstIfPsiElement) = visitTypstElement(element)

    /** The `else` keyword. */
    open fun visitElse(element: TypstElsePsiElement) = visitTypstElement(element)

    /** The `for` keyword. */
    open fun visitFor(element: TypstForPsiElement) = visitTypstElement(element)

    /** The `in` keyword. */
    open fun visitIn(element: TypstInPsiElement) = visitTypstElement(element)

    /** The `while` keyword. */
    open fun visitWhile(element: TypstWhilePsiElement) = visitTypstElement(element)

    /** The `break` keyword. */
    open fun visitBreak(element: TypstBreakPsiElement) = visitTypstElement(element)

    /** The `continue` keyword. */
    open fun visitContinue(element: TypstContinuePsiElement) = visitTypstElement(element)

    /** The `return` keyword. */
    open fun visitReturn(element: TypstReturnPsiElement) = visitTypstElement(element)

    /** The `import` keyword. */
    open fun visitImport(element: TypstImportPsiElement) = visitTypstElement(element)

    /** The `include` keyword. */
    open fun visitInclude(element: TypstIncludePsiElement) = visitTypstElement(element)

    /** The `as` keyword. */
    open fun visitAs(element: TypstAsPsiElement) = visitTypstElement(element)

    /** The contents of a code block. */
    open fun visitCode(element: TypstCodePsiElement) = visitTypstElement(element)

    /** A boolean: `true`, `false`. */
    open fun visitBool(element: TypstBoolPsiElement) = visitTypstElement(element)

    /** An integer: `120`. */
    open fun visitInt(element: TypstIntPsiElement) = visitTypstElement(element)

    /** A floating-point number: `1.2`, `10e-4`. */
    open fun visitFloat(element: TypstFloatPsiElement) = visitTypstElement(element)

    /** A numeric value with a unit: `12pt`, `3cm`, `2em`, `90deg`, `50%`. */
    open fun visitNumeric(element: TypstNumericPsiElement) = visitTypstElement(element)

    /** A quoted string: `"..."`. */
    open fun visitStr(element: TypstStrPsiElement) = visitTypstElement(element)

    /** A code block: `{ let x = 1; x + 2 }`. */
    open fun visitCodeBlock(element: TypstCodeBlockPsiElement) = visitTypstElement(element)

    /** A content block: `[*Hi* there!]`. */
    open fun visitContentBlock(element: TypstContentBlockPsiElement) = visitTypstElement(element)

    /** A grouped expression: `(1 + 2)`. */
    open fun visitParenthesized(element: TypstParenthesizedPsiElement) = visitTypstElement(element)

    /** An array: `(1, "hi", 12cm)`. */
    open fun visitArray(element: TypstArrayPsiElement) = visitTypstElement(element)

    /** A dictionary: `(thickness: 3pt, dash: "solid")`. */
    open fun visitDict(element: TypstDictPsiElement) = visitTypstElement(element)

    /** A named pair: `thickness: 3pt`. */
    open fun visitNamed(element: TypstNamedPairPsiElement) = visitTypstElement(element)

    /** A keyed pair: `"spacy key": true`. */
    open fun visitKeyed(element: TypstKeyedPairPsiElement) = visitTypstElement(element)

    /** A unary operation: `-x`. */
    open fun visitUnary(element: TypstUnaryPsiElement) = visitTypstElement(element)

    /** A binary operation: `a + b`. */
    open fun visitBinary(element: TypstBinaryPsiElement) = visitTypstElement(element)

    /** A field access: `properties.age`. */
    open fun visitFieldAccess(element: TypstFieldAccessPsiElement) = visitTypstElement(element)

    /** An invocation of an open function or method: `f(x, y)`. */
    open fun visitFuncCall(element: TypstFuncCallPsiElement) = visitTypstElement(element)

    /** An open function call's argument list: `(12pt, y)`. */
    open fun visitArgs(element: TypstArgsPsiElement) = visitTypstElement(element)

    /** Spread arguments or an argument sink: `..x`. */
    open fun visitSpread(element: TypstSpreadPsiElement) = visitTypstElement(element)

    /** A closure: `(x, y) => z`. */
    open fun visitClosure(element: TypstClosurePsiElement) = visitTypstElement(element)

    /** A closure's parameters: `(x, y)`. */
    open fun visitParams(element: TypstParamsPsiElement) = visitTypstElement(element)

    /** A let binding: `let x = 1`. */
    open fun visitLetBinding(element: TypstLetBindingPsiElement) = visitTypstElement(element)

    /** A set rule: `set text(...)`. */
    open fun visitSetRule(element: TypstSetRulePsiElement) = visitTypstElement(element)

    /** A show rule: `show heading: it => emph(it.body)`. */
    open fun visitShowRule(element: TypstShowRulePsiElement) = visitTypstElement(element)

    /** A contextual expression: `context text.lang`. */
    open fun visitContextual(element: TypstContextualPsiElement) = visitTypstElement(element)

    /** An if-else conditional: `if x { y } else { z }`. */
    open fun visitConditional(element: TypstConditionalPsiElement) = visitTypstElement(element)

    /** A while loop: `while x { y }`. */
    open fun visitWhileLoop(element: TypstWhileLoopPsiElement) = visitTypstElement(element)

    /** A for loop: `for x in y { z }`. */
    open fun visitForLoop(element: TypstForLoopPsiElement) = visitTypstElement(element)

    /** A module import: `import "utils.typ": a, b, c`. */
    open fun visitModuleImport(element: TypstModuleImportPsiElement) = visitTypstElement(element)

    /** Items to import from a module: `a, b, c`. */
    open fun visitImportItems(element: TypstImportItemsPsiElement) = visitTypstElement(element)

    /** A path to an imported name from a submodule: `a.b.c`. */
    open fun visitImportItemPath(element: TypstImportItemPathPsiElement) = visitTypstElement(element)

    /** A renamed import item: `a as d`. */
    open fun visitRenamedImportItem(element: TypstRenamedImportItemPsiElement) = visitTypstElement(element)

    /** A module include: `include "chapter1.typ"`. */
    open fun visitModuleInclude(element: TypstModuleIncludePsiElement) = visitTypstElement(element)

    /** A break from a loop: `break`. */
    open fun visitLoopBreak(element: TypstLoopBreakPsiElement) = visitTypstElement(element)

    /** A continued in a loop: `continue`. */
    open fun visitLoopContinue(element: TypstLoopContinuePsiElement) = visitTypstElement(element)

    /** A return from an open function: `return`, `return x + 1`. */
    open fun visitFuncReturn(element: TypstFuncReturnPsiElement) = visitTypstElement(element)

    /** A destructuring pattern: `(x, _, ..y)`. */
    open fun visitDestructuring(element: TypstDestructuringPsiElement) = visitTypstElement(element)

    /** A destructuring assignment expression: `(x, y) = (1, 2)`. */
    open fun visitDestructAssignment(element: TypstDestructAssignmentPsiElement) = visitTypstElement(element)

    /** An embedded code expression: `#f(1)` */
    open fun visitEmbeddedCode(element: TypstEmbeddedCodePsiElement) = visitTypstElement(element)

    /** A text in raw. */
    open fun visitRawText(element: TypstRawTextPsiElement) = visitTypstElement(element)

    /** A declaration of an identifier: `it` in `let it = 1` */
    open fun visitIdentDecl(element: TypstIdentDeclPsiElement) = visitTypstElement(element)

    /** A reference to an identifier: `x` in `x + 1` */
    open fun visitIdentRef(element: TypstIdentRefPsiElement) = visitTypstElement(element)

    open fun visitRawBlock(element: TypstRawBlockPsiElement) = visitTypstElement(element)

    open fun visitLinkFuncBlock(element: TypstLinkFuncPsiElement) = visitTypstElement(element)
}
