package com.github.ixmoyren.typilot.highlight

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.options.colors.AttributesDescriptor

fun TextAttributesKey.resolve() = defaultScheme.getAttributes(this)!!

val defaultScheme
    get() = EditorColorsManager.getInstance().globalScheme

class TextAttributeHelper(val displayName: String, val parent: TextAttributesKey?) {
    val id: String = "TYPST_" + displayName.replace(" ", "_").replace("//", "__").replace("+", "and").uppercase()
    val key: TextAttributesKey = parent?.let { createTextAttributesKey(id, it) } ?: createTextAttributesKey(id)
    val array = arrayOf(key)
    val descriptor = AttributesDescriptor(displayName, key)
}

data object TypstHighlightingColors {
    private val collection = mutableListOf<TextAttributeHelper>()

    private fun attribute(
        displayName: String,
        parent: TextAttributesKey? = null,
    ): TextAttributeHelper {
        val helper = TextAttributeHelper(displayName, parent)
        collection.add(helper)
        return helper
    }

    val descriptors = collection.map { it.descriptor }.toTypedArray()

    val KEYWORD = attribute("Code//Keyword", DefaultLanguageHighlighterColors.KEYWORD)
    val KEYWORD_LITERAL = attribute("Code//Keyword Literal", KEYWORD.key)
    val OPERATOR = attribute("Code//Operator", DefaultLanguageHighlighterColors.OPERATION_SIGN)
    val NUMERIC_LITERAL = attribute("Code//Numeric literal", DefaultLanguageHighlighterColors.NUMBER)
    val STRINGS = attribute("Code//String", DefaultLanguageHighlighterColors.STRING)
    val BLOCK_COMMENT = attribute("BlockComment", DefaultLanguageHighlighterColors.BLOCK_COMMENT)
    val LINE_COMMENT = attribute("LineComment", DefaultLanguageHighlighterColors.LINE_COMMENT)
    val RAWS = attribute("Raw", DefaultLanguageHighlighterColors.IDENTIFIER)
    val LABELS = attribute("Label", DefaultLanguageHighlighterColors.LABEL)
    val ESCAPES = attribute("Markup//Escape", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE)
    val REFERENCES = attribute("Markup//Reference", DefaultLanguageHighlighterColors.LABEL)
    val SHORTHANDS = attribute("Markup//Shorthand", DefaultLanguageHighlighterColors.KEYWORD)
    val LINKS = attribute("Markup//Link", DefaultLanguageHighlighterColors.HIGHLIGHTED_REFERENCE)
    val EMPH = attribute("Markup//Emphasis")
    val STRONG = attribute("Markup//Strong")
    val HEADING = attribute("Markup//Heading")
    val TERM = attribute("Markup//Term")
    val MATHS = attribute("Math//Math", DefaultLanguageHighlighterColors.STRING)
    val RAINBOW = (1..12).map { attribute("Rainbow//Color $it") }

    val RAINBOW_BACK_WEAK = (1..12).map { attribute("Rainbow background weak//Color $it") }

    val RAINBOW_BACK_STRONG = (1..12).map { attribute("Rainbow background strong//Color $it") }

    val TEST_KEY = attribute("Test")
}
