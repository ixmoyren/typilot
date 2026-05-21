package com.github.ixmoyren.typilot.actions

import com.github.ixmoyren.typilot.version
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

class TypstHelpAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val message = version()
        Messages.showInfoMessage(event.project, message, "Typst Help")
    }
}
