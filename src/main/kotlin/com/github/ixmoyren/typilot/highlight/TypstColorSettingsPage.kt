package com.github.ixmoyren.typilot.highlight

import com.github.ixmoyren.typilot.TypstHighlightTag
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

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey>? = null

    override fun getDemoText(): String =
        """
        // Line comment  
        /* Block comment */  

        = Heading 1  
        == Heading 2  

        Plain *strong* _emph_ text with \#escape.  
        - List item  
        + Enum item  
        / Term: Description  

        https://typst.org  
        <my-label>  
        @my-label  

        `raw text`  
        `` `raw with lang` ``  

        ${'$'} x^2 + y_1 = z/2 ${'$'}  

        #let f(x) = x + 1  
        #f(42)  
        #let name = "Alice"  
        #let count = 3.14  
        #name  
        """
            .trimIndent()

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> =
        arrayOf(
            AttributesDescriptor("Comment", TypstHighlightTag.COMMENT.Color),
            AttributesDescriptor("Punctuation", TypstHighlightTag.PUNCTUATION.Color),
            AttributesDescriptor("Escape sequence", TypstHighlightTag.ESCAPE.Color),
            AttributesDescriptor("Markup//Strong", TypstHighlightTag.STRONG.Color),
            AttributesDescriptor("Markup//Emphasized", TypstHighlightTag.EMPH.Color),
            AttributesDescriptor("Markup//Hyperlink", TypstHighlightTag.LINK.Color),
            AttributesDescriptor("Markup//Raw text", TypstHighlightTag.RAW.Color),
            AttributesDescriptor("Markup//Label", TypstHighlightTag.LABEL.Color),
            AttributesDescriptor("Markup//Reference", TypstHighlightTag.REF.Color),
            AttributesDescriptor("Markup//Heading", TypstHighlightTag.HEADING.Color),
            AttributesDescriptor("Markup//List marker", TypstHighlightTag.LIST_MARKER.Color),
            AttributesDescriptor("Markup//List term", TypstHighlightTag.LIST_TERM.Color),
            AttributesDescriptor("Math//Delimiter ($)", TypstHighlightTag.MATH_DELIMITER.Color),
            AttributesDescriptor("Math//Operator", TypstHighlightTag.MATH_OPERATOR.Color),
            AttributesDescriptor("Math//Grouping parentheses", TypstHighlightTag.MATH_GROUPING_PARENS.Color),
            AttributesDescriptor("Code//Keyword", TypstHighlightTag.KEYWORD.Color),
            AttributesDescriptor("Code//Operator", TypstHighlightTag.OPERATOR.Color),
            AttributesDescriptor("Code//Number", TypstHighlightTag.NUMBER.Color),
            AttributesDescriptor("Code//String", TypstHighlightTag.STRING.Color),
            AttributesDescriptor("Code//Function name", TypstHighlightTag.FUNCTION.Color),
            AttributesDescriptor("Code//Interpolated variable", TypstHighlightTag.INTERPOLATED.Color),
            AttributesDescriptor("Error", TypstHighlightTag.ERROR.Color),
        )
}
