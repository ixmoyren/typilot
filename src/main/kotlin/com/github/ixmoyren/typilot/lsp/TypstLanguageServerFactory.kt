package com.github.ixmoyren.typilot.lsp

import com.github.ixmoyren.typilot.lsp.config.TinymistSettingsContributor
import com.github.ixmoyren.typilot.lsp.services.TinymistLocateService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.redhat.devtools.lsp4ij.LanguageServerFactory
import com.redhat.devtools.lsp4ij.client.LanguageClientImpl
import com.redhat.devtools.lsp4ij.client.features.LSPClientFeatures
import com.redhat.devtools.lsp4ij.client.features.LSPSemanticTokensFeature
import com.redhat.devtools.lsp4ij.installation.ServerInstaller
import com.redhat.devtools.lsp4ij.server.StreamConnectionProvider
import com.redhat.devtools.lsp4ij.settings.contributors.LanguageServerSettingsContributor

class TypstLanguageServerFactory : LanguageServerFactory {

    override fun createConnectionProvider(project: Project): StreamConnectionProvider =
        TinymistStreamConnectionProvider(project, TinymistLocateService.getInstance().firstValidLocator)

    override fun createServerInstaller(): ServerInstaller = TinymistLocateService.getInstance().installer

    override fun createLanguageClient(project: Project): LanguageClientImpl = TinymistLanguageClient(project)

    override fun createLanguageServerSettingsContributor(): LanguageServerSettingsContributor = TinymistSettingsContributor()

    @Suppress("UnstableApiUsage")
    override fun createClientFeatures(): LSPClientFeatures =
        LSPClientFeatures()
            .setCodeLensFeature(TinymistCodeLensFeature())
            .setSemanticTokensFeature(
                object : LSPSemanticTokensFeature() {
                    override fun shouldVisitPsiElement(file: PsiFile): Boolean = false
                })
}
