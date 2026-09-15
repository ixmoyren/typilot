package com.github.ixmoyren.typilot.settings

import com.github.ixmoyren.typilot.lsp.config.TinymistServerConfiguration
import com.intellij.openapi.components.*
import com.intellij.util.xmlb.XmlSerializerUtil.copyBean

@Service(Service.Level.APP)
@State(name = "com/github/ixmoyren/typilot/settings/TinymistSettings", storages = [Storage("TinymistSettings.xml")])
class TinymistSettings : PersistentStateComponent<TinymistSettings.State> {
    data class State(
        var tinymistPath: String = "",
        var serverConfiguration: String = TinymistServerConfiguration.DEFAULT_CONFIGURATION,
    )

    private var state = State()

    var tinymistPath: String
        get() = state.tinymistPath
        set(value) {
            state.tinymistPath = value
        }

    var serverConfiguration: String
        get() = state.serverConfiguration.ifBlank { TinymistServerConfiguration.DEFAULT_CONFIGURATION }
        set(value) {
            state.serverConfiguration = value
        }

    override fun getState(): State = state

    override fun loadState(state: State) {
        copyBean(state, this.state)
    }

    companion object {
        fun getInstance(): TinymistSettings = service()
    }
}
