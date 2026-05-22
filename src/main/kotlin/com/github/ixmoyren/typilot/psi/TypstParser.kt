package com.github.ixmoyren.typilot.psi

import com.github.ixmoyren.typilot.Event
import com.github.ixmoyren.typilot.TypstParser
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType

class TypstParser : PsiParser {
    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        val text = builder.originalText.toString()
        val events = parser.parseMarkupEvents(text)

        val rootMarker = builder.mark()
        val stack = ArrayDeque<PsiBuilder.Marker>()

        for (event in events) {
            when (event) {
                is Event.Enter -> {
                    stack.addLast(builder.mark())
                }
                is Event.Leaf -> {
                    if (!builder.eof()) {
                        builder.advanceLexer()
                    }
                }
                is Event.Exit -> {
                    val marker = stack.removeLastOrNull() ?: continue
                    val elementType = event.kind.elementType
                    marker.done(elementType)
                }
            }
        }

        while (stack.isNotEmpty()) {
            stack.removeLast().drop()
        }

        rootMarker.done(root)
        return builder.treeBuilt
    }

    companion object {
        val parser: TypstParser by lazy { TypstParser() }
    }
}