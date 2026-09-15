package com.github.ixmoyren.typilot.settings

import com.github.ixmoyren.typilot.TypilotBundle
import com.github.ixmoyren.typilot.lsp.config.TinymistServerConfiguration
import com.github.ixmoyren.typilot.lsp.services.TinymistDownloadService
import com.github.ixmoyren.typilot.lsp.services.TinymistFindService
import com.github.ixmoyren.typilot.lsp.services.TinymistLocateService
import com.github.ixmoyren.typilot.settings.jsonSchema.TinymistConfigurationJsonTextField
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.setEmptyState
import com.intellij.openapi.util.Disposer
import com.intellij.ui.dsl.builder.*
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JLabel
import javax.swing.JPanel

class TinymistSettingsForm : JPanel(), Disposable {
    private val settings
        get() = TinymistSettings.getInstance()

    private val properties = PropertyGraph()

    val tinymistPath = properties.property(settings.tinymistPath)
    val serverConfiguration = properties.property(settings.serverConfiguration)
    var tinymistTextFieldBrowseButton: TextFieldWithBrowseButton = TextFieldWithBrowseButton()
    lateinit var tinymistVersionHint: Cell<JLabel>

    /** JSON editor associated with the tinymist settings JSON schema. */
    val serverConfigurationEditor =
        TinymistConfigurationJsonTextField(projectForEditor()).apply {
            text = settings.serverConfiguration
            preferredSize = Dimension(600, 300)
        }

    private val generalSettingsGroup = panel {
        group(TypilotBundle["settings.tinymist.panel.title"]) {
            panel {
                row {
                    label(TypilotBundle["settings.tinymist.panel.tinymistPath.label"]).align(AlignY.TOP)
                    panel {
                        row {
                            val tinymistResolvePath = TinymistLocateService.getInstance().firstValidLocator?.locate()
                            tinymistTextFieldBrowseButton.setEmptyState(getEmptyState(tinymistResolvePath))
                            tinymistTextFieldBrowseButton.addBrowseFolderListener(
                                null, FileChooserDescriptorFactory.singleFile().withTitle(TypilotBundle["settings.tinymist.panel.tinymistPath.fileChooserDescriptor.title"]))
                            var cell =
                                cell(tinymistTextFieldBrowseButton).applyToComponent {
                                    isOpaque = false
                                    textField.isOpaque = false
                                }
                            cell.columns(COLUMNS_MEDIUM).bindText(tinymistPath)

                            button(TypilotBundle["settings.tinymist.panel.testButton"]) {
                                var tinymistPath = tinymistTextFieldBrowseButton.text
                                val version =
                                    runCatching {
                                            ApplicationManager.getApplication().runReadAction<String?> {
                                                if (tinymistPath.isNotBlank()) {
                                                    TinymistFindService.getInstance().version(tinymistPath)
                                                } else {
                                                    TinymistLocateService.getInstance().version
                                                }
                                            }
                                        }
                                        .getOrNull()

                                ApplicationManager.getApplication().invokeLater {
                                    tinymistVersionHint.applyToComponent {
                                        isVisible = true
                                        text = version ?: TypilotBundle["settings.tinymist.panel.versionHint.notVersion"]
                                    }
                                }
                            }
                        }
                        row {
                            tinymistVersionHint = label(TypilotBundle["settings.tinymist.panel.versionHint"]).visible(false)
                        }
                    }
                }
            }
            row {
                button(TypilotBundle["settings.tinymist.panel.tinymistDownload.buttonText"]) {
                        TinymistDownloadService.getInstance().downloadInBackground(null) { success ->
                            run {
                                if (success) {
                                    tinymistTextFieldBrowseButton.text = getTinymistStatusText()
                                } else {
                                    tinymistTextFieldBrowseButton.text = ""
                                    tinymistTextFieldBrowseButton.setEmptyState(TypilotBundle["settings.tinymist.panel.tinymistDownload.downloadFailedMessage"])
                                }
                            }
                        }
                    }
                    .comment(TypilotBundle["settings.tinymist.panel.tinymistDownload.comment"])
            }
        }
        group(TypilotBundle["settings.serverConfiguration.panel.title"]) {
            row {
                link(TypilotBundle["settings.serverConfiguration.restoreDefault"]) {
                        restoreDefaultServerConfiguration()
                    }
                    .align(AlignX.RIGHT)
            }
            row {
                    cell(serverConfigurationEditor).align(Align.FILL).comment(TypilotBundle["settings.serverConfiguration.panel.comment"])
                }
                .resizableRow()
        }
    }

    init {
        serverConfigurationEditor.addDocumentListener(
            object : DocumentListener {
                override fun documentChanged(event: DocumentEvent) {
                    serverConfiguration.set(serverConfigurationEditor.text)
                }
            })

        layout = BorderLayout()
        add(
            panel {
                row { cell(generalSettingsGroup).align(AlignX.FILL) }
            })
    }

    fun reset() {
        tinymistPath.set(settings.tinymistPath)
        serverConfiguration.set(settings.serverConfiguration)
        serverConfigurationEditor.text = settings.serverConfiguration
        tinymistTextFieldBrowseButton.setEmptyState(getEmptyState(TinymistLocateService.getInstance().firstValidLocator?.locate()))
        tinymistVersionHint.applyToComponent {
            isVisible = false
            text = TypilotBundle["settings.tinymist.panel.versionHint"]
        }
    }

    override fun dispose() {
        Disposer.dispose(serverConfigurationEditor)
    }

    /** Restores the bundled default tinymist configuration in the editor. */
    internal fun restoreDefaultServerConfiguration() {
        val defaultConfiguration = TinymistServerConfiguration.DEFAULT_CONFIGURATION
        serverConfigurationEditor.text = defaultConfiguration
        serverConfiguration.set(defaultConfiguration)
    }

    /**
     * The settings page is application-level and has no project of its own. The JSON editor still needs one for the JSON plugin to resolve the schema, so use the first open
     * project or the default project.
     */
    private fun projectForEditor(): Project = ProjectManager.getInstance().openProjects.firstOrNull { !it.isDefault } ?: ProjectManager.getInstance().defaultProject

    private fun getEmptyState(resolvedPath: String?): String {
        return if (resolvedPath != null) {
            TypilotBundle["settings.panel.emptyStatus.found"] + " " + resolvedPath
        } else {
            TypilotBundle["settings.panel.emptyStatus.notFound"]
        }
    }

    private fun getTinymistStatusText(): String {
        val resolvedPath = TinymistLocateService.getInstance().firstValidLocator?.locate()
        return resolvedPath ?: TypilotBundle["settings.tinymist.panel.tinymistPath.notFound"]
    }
}
