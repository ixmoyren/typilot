package com.github.ixmoyren.typilot.actions

import com.github.ixmoyren.typilot.language.TypstFileType
import com.github.ixmoyren.typilot.lsp.TinymistCommands
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys

class TinymistExportPdfAction : AnAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        val file = event.getData(CommonDataKeys.VIRTUAL_FILE)
        event.presentation.isEnabledAndVisible = file != null && file.fileType is TypstFileType
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val file = event.getData(CommonDataKeys.VIRTUAL_FILE)?.takeIf { it.fileType == TypstFileType } ?: return
        TinymistCommands.exportPdf(project, file)
    }
}
