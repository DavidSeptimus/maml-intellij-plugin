package com.davidseptimus.maml.editor

import com.intellij.openapi.components.*

/**
 * Settings for MAML code folding options.
 */
@State(
    name = "MamlCodeFoldingSettings",
    storages = [Storage("editor.xml")]
)
@Service(Service.Level.APP)
class MamlCodeFoldingSettings : SimplePersistentStateComponent<MamlCodeFoldingSettings.State>(State()) {

    class State : BaseState() {
        var collapseUnicodeEscapes by property(true)
        var groupUnicodeEscapes by property(false)
    }

    var isCollapseUnicodeEscapes: Boolean
        get() = state.collapseUnicodeEscapes
        set(value) {
            state.collapseUnicodeEscapes = value
        }

    var isGroupUnicodeEscapes: Boolean
        get() = state.groupUnicodeEscapes
        set(value) {
            state.groupUnicodeEscapes = value
        }

    companion object {
        fun getInstance(): MamlCodeFoldingSettings {
            return service()
        }
    }
}