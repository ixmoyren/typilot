package com.github.ixmoyren.typilot.navigation

import com.github.ixmoyren.typilot.language.TypstFileType
import com.github.ixmoyren.typilot.lsp.TypstLspIntegrationProvider
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.platform.lsp.api.LspClient
import com.intellij.platform.lsp.api.LspClientManager
import com.intellij.platform.lsp.api.LspServerState
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import org.eclipse.lsp4j.DefinitionParams
import org.eclipse.lsp4j.Position

/**
 * Resolves `Go To Declaration` for Typst through the built-in LSP client by sending `textDocument/definition` to `tinymist`.
 *
 * The platform's own LSP navigation is driven by an implicit reference provider that only reacts while a `GotoDeclarationAction` is running. That indirection is fragile in a full
 * IDE (other plugins can pre-empt the search, and Ctrl+Click does not always run the action through the action listener), so this handler asks the language server directly.
 * Returning a real target element also keeps the built-in documentation navigation for standard library functions working, because this handler returns `null` when `tinymist` has
 * no definition (for example for `text` or `page`).
 */
class TypstLspGotoDeclarationHandler : GotoDeclarationHandler {

    override fun getGotoDeclarationTargets(element: PsiElement?, offset: Int, editor: Editor?): Array<PsiElement>? {
        val containingFile = element?.containingFile ?: return null
        if (containingFile.fileType != TypstFileType) return null
        val sourceFile = containingFile.virtualFile ?: return null
        if (!sourceFile.isValid) return null
        val document = FileDocumentManager.getInstance().getDocument(sourceFile) ?: return null
        val project = containingFile.project
        val client = findRunningClient(project, sourceFile) ?: return null

        val requestOffset = offset.coerceIn(0, document.textLength)
        val line = document.getLineNumber(requestOffset)
        val position = Position(line, requestOffset - document.getLineStartOffset(line))
        val params = DefinitionParams(client.getDocumentIdentifier(sourceFile), position)
        val result =
            runCatching {
                    client.sendRequestSync(REQUEST_TIMEOUT_MS) { server -> server.textDocumentService.definition(params) }
                }
                .getOrNull() ?: return null

        val targets = mutableListOf<PsiElement>()
        result.left?.forEach { location ->
            val start = location.range?.start
            if (start != null) {
                resolveTarget(project, sourceFile, requestOffset, location.uri, start)?.let { targets += it }
            }
        }
        result.right?.forEach { link ->
            val start = (link.targetSelectionRange ?: link.targetRange)?.start
            if (start != null) {
                resolveTarget(project, sourceFile, requestOffset, link.targetUri, start)?.let { targets += it }
            }
        }
        return targets.takeIf { it.isNotEmpty() }?.toTypedArray()
    }

    private fun findRunningClient(project: Project, file: VirtualFile): LspClient? =
        LspClientManager.getInstance(project).getClients(TypstLspIntegrationProvider::class.java).firstOrNull {
            it.state == LspServerState.Running && it.descriptor.isSupportedFile(file)
        }

    /** Maps an LSP target URI and position back to the target PSI element, or returns `null` when the target cannot be resolved. */
    private fun resolveTarget(project: Project, sourceFile: VirtualFile, sourceOffset: Int, uri: String, position: Position): PsiElement? {
        val targetFile = VirtualFileManager.getInstance().findFileByUrl(uri) ?: return null
        val targetDocument = FileDocumentManager.getInstance().getDocument(targetFile) ?: return null
        if (position.line < 0 || position.line >= targetDocument.lineCount) return null
        val targetOffset = offsetAt(targetDocument, position)
        if (targetFile == sourceFile && targetOffset == sourceOffset) return null
        val targetPsiFile = PsiManager.getInstance(project).findFile(targetFile) ?: return null
        return targetPsiFile.findElementAt(targetOffset) ?: targetPsiFile.findElementAt((targetOffset - 1).coerceAtLeast(0)) ?: targetPsiFile
    }

    private fun offsetAt(document: Document, position: Position): Int = (document.getLineStartOffset(position.line) + position.character).coerceIn(0, document.textLength)

    private companion object {
        /** `tinymist` answers local requests in milliseconds; a short timeout keeps the editor responsive if the server is stuck. */
        const val REQUEST_TIMEOUT_MS: Int = 2_000
    }
}
