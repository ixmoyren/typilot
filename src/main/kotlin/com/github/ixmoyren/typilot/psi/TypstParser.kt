package com.github.ixmoyren.typilot.psi

import com.github.ixmoyren.typilot.typalizer
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType
import com.github.ixmoyren.typalize.ASTNode as TypalizeASTNode

class TypstParser : PsiParser {
    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        val rootMark = builder.mark()
        try {
            val text = builder.originalText
            val parseResult =
                typalizer.parse(text) ?: throw IllegalStateException("The typst parser returned null")
            if (parseResult.failure()) {
                throw IllegalStateException("The typst parser couldn't work.", parseResult.error)
            }
            val astNodes = parseResult.result ?: throw IllegalStateException("The typst parse result is null")
            builder.replayTree(astNodes.value)
        } catch (e: Exception) {
            val errorMark = builder.mark()
            while (!builder.eof()) builder.advanceLexer()
            errorMark.error("Typst parsing failed: ${e.message}")
        }
        rootMark.done(root)
        return builder.treeBuilt
    }

    private fun PsiBuilder.replayTree(nodes: List<TypalizeASTNode>) {
        val stack = ArrayDeque<Triple<PsiBuilder.Marker, TypalizeASTNode, Int>>()

        for (node in nodes) {
            if (node.isLeaf) {
                advanceToOffset(node.end)
                stack.decrementAndClose()
            } else if (node.isError) {
                val marker = this.mark()
                advanceToOffset(node.end)
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

    private fun PsiBuilder.advanceToOffset(endOffset: Int) {
        while (!this.eof() && this.currentOffset < endOffset) {
            this.advanceLexer()
        }
    }

    private fun ArrayDeque<Triple<PsiBuilder.Marker, TypalizeASTNode, Int>>.decrementAndClose() {
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
