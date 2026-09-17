package com.github.ixmoyren.typilot.settings.jsonSchema

import com.intellij.json.JsonLanguage
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.SpellCheckingEditorCustomizationProvider
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.ui.EditorTextField
import com.intellij.ui.EditorTextFieldProvider
import com.intellij.ui.MonospaceEditorCustomization
import com.intellij.ui.SoftWrapsEditorCustomization
import com.intellij.util.PsiErrorElementUtil
import java.awt.BorderLayout
import javax.swing.JPanel

/**
 * A JSON editor for the tinymist configuration. Its underlying in-memory file gets a name from [TinymistJsonSchemaFileProvider.uniqueFileName], so the JSON plugin associates it
 * with the tinymist JSON schema and provides syntax highlighting, validation and completion.
 */
class TinymistConfigurationJsonTextField(private val project: Project) : JPanel(BorderLayout()), Disposable {

    private val editorTextField: EditorTextField = createEditor()

    init {
        add(editorTextField, BorderLayout.CENTER)
        editorTextField.setDisposedWith(this)
        applySchemaFileName()
    }

    var text: String
        get() = editorTextField.text
        set(value) {
            if (editorTextField.text != value) {
                editorTextField.text = value
            }
        }

    /** The in-memory file backing the editor, named so that the tinymist JSON schema applies. */
    internal val underlyingFile: VirtualFile?
        get() = FileDocumentManager.getInstance().getFile(editorTextField.document)

    fun addDocumentListener(listener: DocumentListener) {
        editorTextField.document.addDocumentListener(listener, this)
    }

    fun hasErrors(): Boolean {
        val file = underlyingFile ?: return false
        PsiDocumentManager.getInstance(project).commitAllDocuments()
        return PsiErrorElementUtil.hasErrors(project, file)
    }

    override fun dispose() = Unit

    private fun createEditor(): EditorTextField {
        val features = buildList {
            add(MonospaceEditorCustomization.getInstance())
            add(SoftWrapsEditorCustomization.ENABLED)
            SpellCheckingEditorCustomizationProvider.getInstance().enabledCustomization?.let { add(it) }
        }
        return EditorTextFieldProvider.getInstance().getEditorField(JsonLanguage.INSTANCE, project, features)
    }

    private fun applySchemaFileName() {
        val file = FileDocumentManager.getInstance().getFile(editorTextField.document) ?: return
        if (TinymistJsonSchemaFileProvider.isTinymistSettingsFile(file)) return
        runCatching { file.rename(this, TinymistJsonSchemaFileProvider.uniqueFileName()) }
    }
}
