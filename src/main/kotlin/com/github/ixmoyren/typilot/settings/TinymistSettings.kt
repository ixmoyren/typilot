package com.github.ixmoyren.typilot.settings

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.XmlSerializerUtil.copyBean

@Service(Service.Level.APP)
@State(name = "com/github/ixmoyren/typilot/settings/TinymistSettings", storages = [Storage("TinymistSettings.xml")])
class TinymistSettings : PersistentStateComponent<TinymistSettings.State> {
    data class State(
        var tinymistPath: String = "",
        var autoCompileOnSave: Boolean = false,
    )

    private var state = State()

    var tinymistPath: String
        get() = state.tinymistPath
        set(value) {
            state.tinymistPath = value
        }

    var autoCompileOnSave: Boolean
        get() = state.autoCompileOnSave
        set(value) {
            state.autoCompileOnSave = value
        }


    override fun getState(): State = state

    override fun loadState(state: State) {
        copyBean(state, this.state)
    }

    companion object {
        fun getInstance(): TinymistSettings = service()
    }
}