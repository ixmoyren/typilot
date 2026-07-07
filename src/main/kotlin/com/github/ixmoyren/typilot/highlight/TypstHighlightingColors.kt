package com.github.ixmoyren.typilot.highlight

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.options.colors.AttributesDescriptor

class TextAttributeHelper(val displayName: String, default: TextAttributesKey?) {
    val id: String = "TYPST_${displayName.replace(" ", "_").replace("//", "__").replace("+", "and").uppercase()}"
    val key: TextAttributesKey = default?.let { createTextAttributesKey(id, it) } ?: createTextAttributesKey(id)
    val descriptor = AttributesDescriptor(displayName, key)
}

object TypstHighlightingColors {
    private val collection = mutableListOf<TextAttributeHelper>()

    private fun attribute(
        displayName: String,
        default: TextAttributesKey? = null,
    ): TextAttributeHelper {
        val helper = TextAttributeHelper(displayName, default)
        collection.add(helper)
        return helper
    }

    val descriptors by lazy { collection.map { it.descriptor }.toTypedArray() }

    val KEYWORD = attribute("Code//Keyword", DefaultLanguageHighlighterColors.KEYWORD)
    val OPERATOR = attribute("Code//Operator", DefaultLanguageHighlighterColors.OPERATION_SIGN)
    val NUMERIC_LITERAL = attribute("Code//Numeric literal", DefaultLanguageHighlighterColors.NUMBER)
    val STRINGS = attribute("Code//String", DefaultLanguageHighlighterColors.STRING)
    val BLOCK_COMMENT = attribute("BlockComment", DefaultLanguageHighlighterColors.BLOCK_COMMENT)
    val LINE_COMMENT = attribute("LineComment", DefaultLanguageHighlighterColors.LINE_COMMENT)
    val LABELS = attribute("Label", DefaultLanguageHighlighterColors.LABEL)
    val ESCAPES = attribute("Markup//Escape", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE)
    val REFERENCES = attribute("Markup//Reference", DefaultLanguageHighlighterColors.LABEL)
    val SHORTHANDS = attribute("Markup//Shorthand", DefaultLanguageHighlighterColors.OPERATION_SIGN)
    val LINKS = attribute("Markup//Link", CodeInsightColors.HYPERLINK_ATTRIBUTES)
    val EMPH = attribute("Markup//Emphasis", createTextAttributesKey("MARKDOWN_ITALIC"))
    val STRONG = attribute("Markup//Strong", createTextAttributesKey("MARKDOWN_BOLD"))
    val HEADING = attribute("Markup//Heading", createTextAttributesKey("MARKDOWN_HEADER_LEVEL_1"))
    val TERM = attribute("Markup//Term", DefaultLanguageHighlighterColors.IDENTIFIER)
    val MATHS = attribute("Math//Math", DefaultLanguageHighlighterColors.STRING)
}
