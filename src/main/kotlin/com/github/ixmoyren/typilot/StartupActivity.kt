package com.github.ixmoyren.typilot

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.redhat.devtools.lsp4ij.LanguageServerManager

class StartupActivity : ProjectActivity, DumbAware {
    override suspend fun execute(project: Project) {
        LanguageServerManager.getInstance(project).start(TYPST_LANGUAGE_SERVER_ID)
    }
}