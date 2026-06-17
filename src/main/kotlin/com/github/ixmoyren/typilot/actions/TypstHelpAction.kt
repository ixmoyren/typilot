package com.github.ixmoyren.typilot.actions

import com.github.ixmoyren.typilot.typalizer
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

class TypstHelpAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val message = typalizer.version().run {
            if (this == null || failure()) {
                throw Exception("The typst lexer couldn't work.", this?.error)
            }
            result ?: throw Exception("The typst version result is null")
        }
        Messages.showInfoMessage(event.project, message, "Typst Help")
    }
}
