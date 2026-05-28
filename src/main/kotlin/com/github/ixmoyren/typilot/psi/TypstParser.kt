package com.github.ixmoyren.typilot.psi

import com.github.ixmoyren.typilot.AstNode
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
        val nodes = parser.parse(text)
        val tagMap =
            buildMap(nodes.size) {
                for (node in nodes) {
                    node.tag?.let { put(node.start.toLong() shl 32 or node.end.toLong(), it) }
                }
            }
        val rootMark = builder.mark();
        builder.replayTree(nodes)
        rootMark.done(root)
        val astRoot = builder.treeBuilt
        astRoot.applyTags(tagMap)
        return astRoot
    }

    fun PsiBuilder.replayTree(nodes: List<AstNode>) {
        val stack = ArrayDeque<Triple<PsiBuilder.Marker, AstNode, Int>>()

        for (node in nodes) {
            if (node.isTrivia) {
                stack.decrementAndClose()
                continue
            }
            if (node.isLeaf) {
                if (!this.eof()) this.advanceLexer()
                stack.decrementAndClose()
            } else {
                val marker = this.mark()
                val childCount = node.childrenCount.toInt()
                if (childCount == 0) {
                    marker.marked(node)
                    stack.decrementAndClose()
                } else {
                    stack.addLast(Triple(marker, node, childCount))
                }
            }
        }
    }

    fun PsiBuilder.Marker.marked(node: AstNode) {
        if (node.isError) {
            this.error(node.errorMessage ?: "syntax error")
        } else {
            this.done(node.type)
        }
    }

    fun ArrayDeque<Triple<PsiBuilder.Marker, AstNode, Int>>.decrementAndClose() {
        while (this.isNotEmpty()) {
            val (marker, node, remaining) = this.removeLast()
            if (remaining == 1) {
                marker.marked(node)
            } else {
                this.addLast(Triple(marker, node, remaining - 1))
                break
            }
        }
    }

    fun ASTNode.applyTags(tagMap: Map<Long, TypstHighlightTag>) {
        val stack = ArrayDeque<ASTNode>()
        stack.addFirst(this)
        while (stack.isNotEmpty()) {
            val node = stack.removeFirst()
            val start = node.startOffset
            val end = start + node.textLength
            val key = (start.toLong() shl 32) or end.toLong()
            tagMap[key]?.let { node.putUserData(TypstHighlightTagKeys.TAG, it) }

            var child = node.firstChildNode
            while (child != null) {
                stack.addFirst(child)
                child = child.treeNext
            }
        }
    }

    companion object {
        val parser: TypstParser by lazy { TypstParser() }
    }
}
