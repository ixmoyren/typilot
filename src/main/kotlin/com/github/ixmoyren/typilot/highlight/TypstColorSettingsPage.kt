package com.github.ixmoyren.typilot.highlight

import com.github.ixmoyren.typilot.language.TypstFileType
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import javax.swing.Icon
import kotlin.to

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
            "link" to TypstHighlightingColors.LINKS.key) + (1..12).associate { "rc$it" to TypstHighlightingColors.RAINBOW[it - 1].key }

    override fun getDemoText(): String =
        """
        This <strong>*is*</strong> just <em>_formatting_</em>. It's <strong>*</strong><emst>_composable_</emst><strong>*</strong>.

        <heading>= Headings</heading>

        <term>/ Term:</term> this is.

        (Unfortunately, color settings page doesn't support custom effects mixing)

        There are <label><labels></label> and <reference>@references</reference>. Shorthands are highlighted <shorthand>---</shorthand> like this.

        <rc8>-</rc8> List
            <rc4>-</rc4> markers,
          <rc4>-</rc4> as well as
            <rc11>+</rc11> enum
          <rc4>+</rc4> markers,
            <rc11>+</rc11> are highlighted
              <rc7>+</rc7> based on their level

        <keyword>#let</keyword> f = <num>1</num>
        <rc7>#[</rc7>The color of the hashes depends on the context<rc7>];</rc7> and so is the color of the semicolons.

        // These are comments.
        /*
            These are as well.
        */

        Rainbowifying can be disabled in plugin's settings.

        (This demonstrates the colors, not the highlighting.)
        <rc1>1</rc1> <rc2>2</rc2> <rc3>3</rc3> <rc4>4</rc4> <rc5>5</rc5> <rc6>6</rc6> <rc7>7</rc7> <rc8>8</rc8> <rc9>9</rc9> <rc10>10</rc10> <rc11>11</rc11> <rc12>12</rc12>

        Кириллица тоже прекрасно работает!
        <keyword>#</keyword>let и-юникод-идентификаторы = [seem to work as well ]

        <keyword>#</keyword>for i in range<rc7>(</rc7><num>5</num><rc7>)</rc7> <rc3>{</rc3>
            for x in range<rc9>(</rc9>i<rc9>)</rc9> <rc5>{</rc5>
                for y in range<rc11>(</rc11>x<rc11>)</rc11> <rc7>{</rc7>
                    <rc2>[</rc2>1<rc2>]</rc2>
                <rc7>}</rc7>
            <rc5>}</rc5>
        <rc3>}</rc3>

        <string>#"string with <escape>\n</escape> escapes"</string>, links: <link>https://typst.app/</link>

        <math>${'$'}{'$'} A_n^d m a t h${'$'}{'$'}</math>
        ""${'"'} 
        """
            .trimIndent()

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = TypstHighlightingColors.descriptors
}
