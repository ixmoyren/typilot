package com.github.ixmoyren.typilot.psi

import com.github.ixmoyren.typilot.typalizer
import com.github.ixmoyren.typalize.ASTNode as TypalizeASTNode
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType

class TypstParser : PsiParser {
    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        val text = builder.originalText
        val nodes = typalizer.parse(text).run {
            if (this == null || failure()) {
                throw Exception("The typst parser couldn't work.", this?.error)
            }
            result ?: throw Exception("The typst parse result is null")
        }.value
        val rootMark = builder.mark()
        builder.replayTree(nodes)
        rootMark.done(root)
        return builder.treeBuilt
    }

    fun PsiBuilder.replayTree(nodes: List<TypalizeASTNode>) {
        val stack = ArrayDeque<Triple<PsiBuilder.Marker, TypalizeASTNode, Int>>()

        for (node in nodes) {
            if (node.isLeaf) {
                if (!this.eof()) this.advanceLexer()
                stack.decrementAndClose()
            } else if (node.isError) {
                val marker = this.mark()
                if (!this.eof()) this.advanceLexer()
                marker.error(node.errorMessage ?: "syntax error")
                stack.decrementAndClose()
            } else {
                val marker = this.mark()
                val childCount = node.childrenCount
                if (childCount == 0) {
                    marker.done(node.type)
                    stack.decrementAndClose()
                } else {
                    stack.addLast(Triple(marker, node, childCount))
                }
            }
        }
    }

    fun ArrayDeque<Triple<PsiBuilder.Marker, TypalizeASTNode, Int>>.decrementAndClose() {
        while (this.isNotEmpty()) {
            val (marker, node, remaining) = this.removeLast()
            if (remaining == 1) {
                marker.done(node.type)
            } else {
                this.addLast(Triple(marker, node, remaining - 1))
                break
            }
        }
    }
}
