package com.github.ixmoyren.typilot.psi

import com.github.ixmoyren.typilot.TypstHighlightTag
import com.github.ixmoyren.typilot.TypstParser
import com.github.ixmoyren.typilot.highlight.TypstHighlightTagKeys
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType

class TypstParser : PsiParser {
    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        val text = builder.originalText.toString()
        val nodes = parser.parseMarkupEvents(text)

        val rootMarker = builder.mark()
        val stack = ArrayDeque<Pair<PsiBuilder.Marker, Int>>()

        for (node in nodes) {
            while (stack.isNotEmpty() && stack.last().second == 0) {
                val (_, _) = stack.removeLast()
            }

            if (node.isLeaf) {
                if (node.isError) builder.error(node.errorMessage ?: "syntax error")
                builder.advanceLexer()
            } else {
                val marker = builder.mark()
                stack.addLast(Pair(marker, node.childrenCount.toInt()))
            }

            if (stack.isNotEmpty()) {
                val (marker, remaining) = stack.removeLast()
                if (remaining - 1 == 0) {
                    marker.done(node.type)
                } else {
                    stack.addLast(Pair(marker, remaining - 1))
                }
            }
        }
        rootMarker.done(root)
        val astRoot = builder.treeBuilt
        val tagMap =
            buildMap(nodes.size) {
                for (node in nodes) {
                    node.tag?.let { put(node.start.toLong() shl 32 or node.end.toLong(), it) }
                }
            }
        applyTags(astRoot, tagMap)
        return astRoot
    }

    private fun applyTags(root: ASTNode, tagMap: Map<Long, TypstHighlightTag>) {
        val stack = mutableListOf(root)
        while (stack.isNotEmpty()) {
            val node = stack.removeAt(stack.size - 1)
            val start = node.startOffset
            val end = start + node.textLength
            val key = (start.toLong() shl 32) or end.toLong()
            tagMap[key]?.let { node.putUserData(TypstHighlightTagKeys.TAG, it) }

            var child = node.firstChildNode
            while (child != null) {
                stack.add(child)
                child = child.treeNext
            }
        }
    }

    companion object {
        val parser: TypstParser by lazy { TypstParser() }
    }
}
