package com.github.ixmoyren.typilot.highlight

import com.github.ixmoyren.typilot.TypstHighlightTag
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors as DLHC
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.Key
import java.util.EnumMap

object TypstHighlightTagKeys {
    val TAG: Key<TypstHighlightTag> = Key.create("TypstHighlightTag")
}

val DefaultColor: TextAttributesKey = DLHC.IDENTIFIER

val TypstHighlightTag.Color: TextAttributesKey
    get() = TagPropertyMap[this] ?: DefaultColor

private val TagPropertyMap =
    EnumMap(
        mapOf(
            TypstHighlightTag.COMMENT to key("TypstComment", DLHC.LINE_COMMENT),
            TypstHighlightTag.PUNCTUATION to key("TypstPunctuation", DLHC.BRACKETS),
            TypstHighlightTag.ESCAPE to key("TypstEscape", DLHC.VALID_STRING_ESCAPE),
            TypstHighlightTag.STRONG to key("TypstStrong"),
            TypstHighlightTag.LINK to key("TypstLink"),
            TypstHighlightTag.EMPH to key("TypstEmph"),
            TypstHighlightTag.RAW to key("TypstRaw", DLHC.STRING),
            TypstHighlightTag.LABEL to key("TypstLabel", DLHC.LABEL),
            TypstHighlightTag.REF to key("TypstRef", DLHC.LABEL),
            TypstHighlightTag.HEADING to
                key(
                    "TypstHeading",
                ),
            TypstHighlightTag.LIST_MARKER to key("TypstListMarker", DLHC.KEYWORD),
            TypstHighlightTag.LIST_TERM to key("TypstListTerm", DLHC.KEYWORD),
            TypstHighlightTag.MATH_DELIMITER to key("TypstMathDelimiter", DLHC.BRACKETS),
            TypstHighlightTag.MATH_OPERATOR to key("TypstOperator", DLHC.OPERATION_SIGN),
            TypstHighlightTag.MATH_GROUPING_PARENS to key("TypstMathGroupingParens", DLHC.PARAMETER),
            TypstHighlightTag.KEYWORD to key("TypstKeyWord", DLHC.KEYWORD),
            TypstHighlightTag.OPERATOR to key("TypstOperator", DLHC.OPERATION_SIGN),
            TypstHighlightTag.NUMBER to key("TypstNumber", DLHC.NUMBER),
            TypstHighlightTag.STRING to key("TypstString", DLHC.STRING),
            TypstHighlightTag.FUNCTION to key("TypstFunction", DLHC.FUNCTION_CALL),
            TypstHighlightTag.INTERPOLATED to key("TypstInterpolated", DLHC.IDENTIFIER),
            TypstHighlightTag.ERROR to key("TypstError", DLHC.INVALID_STRING_ESCAPE),
        ))

private fun key(name: String, fallback: TextAttributesKey) = TextAttributesKey.createTextAttributesKey(name, fallback)

private fun key(name: String) = TextAttributesKey.createTextAttributesKey(name)
