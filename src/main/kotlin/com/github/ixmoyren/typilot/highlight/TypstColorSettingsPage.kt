package com.github.ixmoyren.typilot.highlight

import com.github.ixmoyren.typilot.language.TypstFileType
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import javax.swing.Icon

class TypstColorSettingsPage : ColorSettingsPage {
    override fun getIcon(): Icon = TypstFileType.icon

    override fun getHighlighter(): SyntaxHighlighter = TypstLexicalHighlighter()

    override fun getDisplayName(): String = "Typst"

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey> =
        mapOf(
            "strong" to TypstHighlightingColors.STRONG.key,
            "em" to TypstHighlightingColors.EMPH.key,
            "heading" to TypstHighlightingColors.HEADING.key,
            "term" to TypstHighlightingColors.TERM.key,
            "label" to TypstHighlightingColors.LABELS.key,
            "reference" to TypstHighlightingColors.REFERENCES.key,
            "shorthand" to TypstHighlightingColors.SHORTHANDS.key,
            "keyword" to TypstHighlightingColors.KEYWORD.key,
            "math" to TypstHighlightingColors.MATHS.key,
            "num" to TypstHighlightingColors.NUMERIC_LITERAL.key,
            "string" to TypstHighlightingColors.STRINGS.key,
            "escape" to TypstHighlightingColors.ESCAPES.key,
            "link" to TypstHighlightingColors.LINKS.key
        ) + (1..12).associate { "rc$it" to TypstHighlightingColors.RAINBOW[it - 1].key }

    override fun getDemoText(): String =
        """
        """
            .trimIndent()

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = TypstHighlightingColors.descriptors
}
