package com.davidseptimus.maml.settings

import com.davidseptimus.maml.MamlBundle
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.UiDslUnnamedConfigurable
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bindSelected

class MamlCodeCompletionConfigurable : UiDslUnnamedConfigurable.Simple(), Configurable {

    override fun getDisplayName(): String {
        return "MAML"
    }

    override fun Panel.createContent() {
        val settings = MamlSettings.getInstance()

        group("MAML") {
            row {
                checkBox(MamlBundle.message("settings.completion.keywords"))
                    .bindSelected({ settings.enableKeywordCompletion }, { settings.enableKeywordCompletion = it })
            }
            row {
                checkBox(MamlBundle.message("settings.completion.knownKeys"))
                    .bindSelected({ settings.enableKnownKeysCompletion }, { settings.enableKnownKeysCompletion = it })
            }
        }
    }
}