package com.github.ixmoyren.typilot.highlight

import com.github.ixmoyren.typilot.psi.*
import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.editor.markup.TextAttributesEffectsBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.util.firstLeaf
import com.intellij.psi.util.lastLeaf
import java.awt.Color
import java.awt.Font
import kotlin.math.max
import kotlin.math.min

class TypstHighlightAnnotator : Annotator {
    private lateinit var holder: AnnotationHolder
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        this.holder = holder
        if (element !is TypstPsiElement) return

        val children = element.children
        if (children.isNotEmpty()) {
            return
        } else {
            val colorize = when (element) {
                is TypstHashPsiElement -> element.nextSibling.firstLeaf().parent
                is TypstSemicolonPsiElement -> {
                    element.prevSibling.takeIf { it is TypstEmbeddedCodePsiElement }?.lastLeaf()?.parent ?: element
                }
                else -> element
            }

            val textKey = computeTextKey(colorize)
            val textAttr = textKey?.let { mergeAttributes(*it.map { it.resolve() }.toTypedArray()) }
            if (textAttr != null)
                holder.newSilentAnnotation(HighlightInfoType.SYMBOL_TYPE_SEVERITY)
                    .range(element.textRange)
                    .enforcedTextAttributes(textAttr)
                    .create()
        }
    }

    private fun mergeAttributes(vararg attrs: TextAttributes): TextAttributes? {
        if (attrs.isEmpty()) return null

        if (attrs.size == 1) return attrs[0]

        val result = attrs[0].clone()

        result.fontType = attrs.map { it.fontType }.reduce(::mergeFontType)

        result.foregroundColor =
            mixColors(attrs.mapNotNull { it.foregroundColor }, attrs.size, defaultScheme.defaultForeground)
        result.backgroundColor =
            mixColors(attrs.mapNotNull { it.backgroundColor }, attrs.size, defaultScheme.defaultBackground)

        val builder = TextAttributesEffectsBuilder.create(attrs[0])
        attrs.drop(1).fold(builder) { it, next -> it.coverWith(next) }.applyTo(result)
        return result
    }

    private fun mergeFontType(a: Int, b: Int): Int {
        val bold = (a and Font.BOLD) or (b and Font.BOLD)
        val italic = (a and Font.ITALIC) xor (b and Font.ITALIC)
        return bold or italic
    }

    fun Int.clip(lower: Int, upper: Int) = max(lower, min(this, upper))

    private fun mixColors(components: List<Color>, n: Int, default: Color): Color {
        val redDiff = components.sumOf { it.red - default.red }
        val greenDiff = components.sumOf { it.green - default.green }
        val blueDiff = components.sumOf { it.blue - default.blue }
        val n = components.size // Experimental change
        val red = default.red + redDiff // * (n) / (n + 1)
        val green = default.green + greenDiff // * (n) / (n + 1)
        val blue = default.blue + blueDiff // * (n) / (n + 1)
        return Color(red.clip(0, 255), green.clip(0, 255), blue.clip(0, 255))
    }

    private fun computeTextKey(element: PsiElement): List<TextAttributesKey>? {
        val result = mutableListOf<TextAttributesKey>()
        var prev: PsiElement? = null
        var element: PsiElement = element
        while (element is ATypstPsiElement) {
            when (element) {
                is TypstKeyword, is TypstUnOp, is TypstBinOp, is TypstAssignOp, is TypstIgnored, is TypstNumericLiteral -> if (element !is TypstStarPsiElement) return null
                is TypstEquationPsiElement -> result.add(TypstHighlightingColors.MATHS.key)
                is TypstRawPsiElement -> result.add(TypstHighlightingColors.RAWS.key)
                is TypstRawBlockPsiElement -> {
                    val lang = element.langTag()
                    if (lang == null) result.add(TypstHighlightingColors.RAWS.key) else return listOf()
                }
                is TypstContentBlockPsiElement -> {
                    if (element.parent is TypstRefPsiElement) {
                        result.add(TypstHighlightingColors.REFERENCES.key)
                    } else return result
                }
                is TypstCodePart -> return result
                is TypstParam -> if (element !is TypstUnderscorePsiElement) return result
                is TypstEmbeddedCodePsiElement -> return result
                is TypstEmphPsiElement -> result.add(TypstHighlightingColors.EMPH.key)
                is TypstHeadingPsiElement -> result.add(TypstHighlightingColors.HEADING.key)
                is TypstStrongPsiElement -> result.add(TypstHighlightingColors.STRONG.key)
                is TypstTermItemPsiElement -> {
                    if (prev?.nextSiblingOf(inclusive = true) { it is TypstColonPsiElement } != null) {
                        result.add(TypstHighlightingColors.TERM.key)
                    }
                }
                else -> Unit
            }
            prev = element
            element = element.parent
        }

        return result
    }
}
