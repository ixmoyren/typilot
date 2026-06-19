package com.github.ixmoyren.typilot.settings

import com.github.ixmoyren.typilot.TinymistManager
import com.github.ixmoyren.typilot.TypilotBundle
import com.github.ixmoyren.typilot.services.TinymistDownloadService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.setEmptyState
import com.intellij.ui.dsl.builder.*
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel

class TinymistSettingsForm : JPanel() {
    private val settings
        get() = TinymistSettings.getInstance()

    private val properties = PropertyGraph()

    val tinymistPath = properties.property(settings.tinymistPath)
    val autoCompileOnSave = properties.property(settings.autoCompileOnSave)
    var tinymistTextFieldBrowseButton: TextFieldWithBrowseButton = TextFieldWithBrowseButton()
    lateinit var tinymistVersionHint: Cell<JLabel>

    private val generalSettingsGroup = panel {
        group(TypilotBundle["settings.tinymist.panel.title"]) {
            panel {
                row {
                    label(TypilotBundle["settings.tinymist.panel.tinymistPath.label"]).align(AlignY.TOP)
                    panel {
                        row {
                            val tinymistResolvePath = TinymistManager.getInstance().resolveTinymistPath()
                            tinymistTextFieldBrowseButton.setEmptyState(getEmptyState(tinymistResolvePath))
                            tinymistTextFieldBrowseButton.addBrowseFolderListener(
                                null,
                                FileChooserDescriptorFactory.singleFile()
                                    .withTitle(TypilotBundle["settings.tinymist.panel.tinymistPath.fileChooserDescriptor.title"])
                            )
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
                                                TinymistManager.getInstance().tinymistVersion(tinymistPath)
                                            } else {
                                                TinymistManager.getInstance().tinymistVersion()
                                            }
                                        }
                                    }
                                        .getOrNull()

                                ApplicationManager.getApplication().invokeLater {
                                    tinymistVersionHint.applyToComponent {
                                        isVisible = true
                                        text =
                                            version ?: TypilotBundle["settings.tinymist.panel.versionHint.notVersion"]
                                    }
                                }
                            }
                        }
                        row {
                            tinymistVersionHint =
                                label(TypilotBundle["settings.typst.panel.versionHint"]).visible(false)
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
    }

    init {
        layout = BorderLayout()
        add(
            panel {
                row { cell(generalSettingsGroup).align(AlignX.FILL) }
            })
    }

    fun reset() {
        tinymistPath.set(settings.tinymistPath)
        autoCompileOnSave.set(settings.autoCompileOnSave)
        tinymistTextFieldBrowseButton.setEmptyState(getEmptyState(TinymistManager.getInstance().resolveTinymistPath()))
        tinymistVersionHint.applyToComponent {
            isVisible = false
            text = TypilotBundle["settings.tinymist.panel.versionHint"]
        }
    }

    private fun getEmptyState(resolvedPath: String?): String {
        return if (resolvedPath != null) {
            TypilotBundle["settings.panel.emptyStatus.found"] + " " + resolvedPath
        } else {
            TypilotBundle["settings.panel.emptyStatus.notFound"]
        }
    }

    private fun getTinymistStatusText(): String {
        val manager = TinymistManager.getInstance()
        val resolvedPath = manager.resolveTinymistPath()
        return resolvedPath ?: TypilotBundle["settings.tinymist.panel.tinymistPath.notFound"]
    }
}
