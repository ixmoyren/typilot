package com.github.ixmoyren.typilot.actions

import com.github.ixmoyren.typilot.language.TypstFileType
import com.github.ixmoyren.typilot.lsp.services.TinymistLocateService
import com.github.ixmoyren.typilot.typalizer
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.ui.Messages

class TypstHelpAction : AnAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        val file = event.getData(CommonDataKeys.VIRTUAL_FILE)
        val isTypstFile = file?.fileType is TypstFileType
        event.presentation.isEnabledAndVisible = isTypstFile
    }

    @Suppress("DialogTitleCapitalization")
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return

        object : Task.Backgroundable(project, "Getting Typst Info", false) {
                override fun run(indicator: ProgressIndicator) {
                    val tinymistVersion: String = TinymistLocateService.getInstance().version ?: throw Exception("The tinymist version result is null")

                    val typstSyntaxVersion: String =
                        typalizer.version().run {
                            if (this == null || failure()) {
                                throw Exception("The typst lexer couldn't work.", this?.error)
                            }
                            result ?: throw Exception("The typst version result is null")
                        }

                    val message = "$typstSyntaxVersion\n$tinymistVersion"
                    ApplicationManager.getApplication().invokeLater {
                        Messages.showInfoMessage(project, message, "Typst Help")
                    }
                }
            }
            .queue()
    }
}
