package com.davidseptimus.maml.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "com.davidseptimus.maml.settings.MamlSettings",
    storages = [Storage("MamlSettings.xml")]
)
class MamlSettings : PersistentStateComponent<MamlSettings> {

    var commentPreviewWords: Int = 10
    var multilineStringPreviewWords: Int = 10
    var showArrayItemCountHints: Boolean = true
    var enableKeywordCompletion: Boolean = true
    var enableKnownKeysCompletion: Boolean = true

    override fun getState(): MamlSettings = this

    override fun loadState(state: MamlSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(): MamlSettings {
            return ApplicationManager.getApplication().getService(MamlSettings::class.java)
        }
    }
}