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

class TypstHighlightAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is TypstPsiElement) return
        if (element.children.isNotEmpty()) return
        val colorize =
            when (element) {
                is TypstHashPsiElement -> element.nextSibling?.firstLeaf()?.parent ?: return
                is TypstSemicolonPsiElement -> {
                    element.prevSibling.takeIf { it is TypstEmbeddedCodePsiElement }?.lastLeaf()?.parent ?: element
                }
                else -> element
            }
        val textKey = computeTextKey(colorize) ?: return
        val textAttr = mergeAttributes(*textKey.map { it.resolve() }.toTypedArray()) ?: return
        holder.newSilentAnnotation(HighlightInfoType.SYMBOL_TYPE_SEVERITY).range(element.textRange).enforcedTextAttributes(textAttr).create()
    }

    private fun mergeAttributes(vararg attrs: TextAttributes): TextAttributes? {
        if (attrs.isEmpty()) return null
        if (attrs.size == 1) return attrs[0]
        val result = attrs[0].clone()
        result.fontType = attrs.map { it.fontType }.reduce { a, b -> ((a or b) and Font.BOLD) or ((a xor b) and Font.ITALIC) }
        result.foregroundColor = mixColors(attrs.mapNotNull { it.foregroundColor }, attrs.size, defaultScheme.defaultForeground)
        result.backgroundColor = mixColors(attrs.mapNotNull { it.backgroundColor }, attrs.size, defaultScheme.defaultBackground)
        val builder = TextAttributesEffectsBuilder.create(attrs[0])
        attrs.drop(1).fold(builder) { it, next -> it.coverWith(next) }.applyTo(result)
        return result
    }

    @Suppress("UseJBColor")
    private fun mixColors(components: List<Color>, n: Int, default: Color): Color {
        if (components.isEmpty()) return default
        val (sumRed, sumGreen, sumBlue) = components.fold(Triple(0, 0, 0)) { (r, g, b), color -> Triple(r + color.red, g + color.green, b + color.blue) }
        val m = components.size
        val totalWeight = n + m
        val newRed = (default.red.toLong() * n + sumRed) / totalWeight
        val newGreen = (default.green.toLong() * n + sumGreen) / totalWeight
        val newBlue = (default.blue.toLong() * n + sumBlue) / totalWeight
        return Color(newRed.toInt().coerceIn(0, 255), newGreen.toInt().coerceIn(0, 255), newBlue.toInt().coerceIn(0, 255))
    }

    private fun computeTextKey(element: PsiElement): List<TextAttributesKey>? {
        val result = mutableListOf<TextAttributesKey>()
        var prev: PsiElement? = null
        var current: PsiElement = element
        while (current is ATypstPsiElement) {
            when (current) {
                is TypstKeyword,
                is TypstUnOp,
                is TypstBinOp,
                is TypstAssignOp,
                is TypstIgnored,
                is TypstNumericLiteral -> if (current !is TypstStarPsiElement) return null
                is TypstEquationPsiElement -> result.add(TypstHighlightingColors.MATHS.key)
                is TypstRawPsiElement -> result.add(TypstHighlightingColors.RAWS.key)
                is TypstRawBlockPsiElement -> {
                    val lang = current.langTag()
                    if (lang == null) result.add(TypstHighlightingColors.RAWS.key) else return emptyList()
                }
                is TypstContentBlockPsiElement -> {
                    if (current.parent is TypstRefPsiElement) {
                        result.add(TypstHighlightingColors.REFERENCES.key)
                    } else return result
                }
                is TypstCodePart -> return result
                is TypstParam -> if (current !is TypstUnderscorePsiElement) return result
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
            prev = current
            current = current.parent
        }

        return result
    }
}
