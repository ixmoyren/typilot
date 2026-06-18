package com.github.ixmoyren.typilot.settings

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

class TinymistConfigurable : Configurable {
    private var settingsForm: TinymistSettingsForm? = null

    private val settings = TinymistSettings.getInstance()

    override fun getDisplayName(): String = "Typst"

    override fun createComponent(): JComponent? {
        settingsForm = settingsForm ?: TinymistSettingsForm()
        return settingsForm
    }

    override fun isModified(): Boolean {
        return settingsForm?.run {
            settings.tinymistPath != tinymistPath.get() ||
                    settings.autoCompileOnSave != autoCompileOnSave.get()
        } ?: false
    }

    override fun apply() {
        settings.run {
            tinymistPath = settingsForm?.tinymistPath?.get() ?: tinymistPath
            autoCompileOnSave = settingsForm?.autoCompileOnSave?.get() ?: autoCompileOnSave
        }
    }

    override fun reset() {
        settingsForm?.reset()
    }

    override fun disposeUIResources() {
        settingsForm = null
    }
}
