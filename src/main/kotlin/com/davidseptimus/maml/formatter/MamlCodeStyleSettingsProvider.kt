package com.davidseptimus.maml.formatter

import com.davidseptimus.maml.MamlBundle
import com.davidseptimus.maml.lang.MamlLanguage
import com.intellij.application.options.CodeStyleAbstractConfigurable
import com.intellij.application.options.CodeStyleAbstractPanel
import com.intellij.application.options.TabbedLanguageCodeStylePanel
import com.intellij.lang.Language
import com.intellij.psi.codeStyle.CodeStyleConfigurable
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider
import com.intellij.psi.codeStyle.CustomCodeStyleSettings

internal class MamlCodeStyleSettingsProvider : CodeStyleSettingsProvider() {
    override fun createConfigurable(
        settings: CodeStyleSettings,
        originalSettings: CodeStyleSettings
    ): CodeStyleConfigurable {
        return object : CodeStyleAbstractConfigurable(
            settings,
            originalSettings,
            MamlBundle.message("settings.displayName")
        ) {
            override fun createPanel(settings: CodeStyleSettings): CodeStyleAbstractPanel {
                val language: Language = MamlLanguage
                val currentSettings = currentSettings
                return object : TabbedLanguageCodeStylePanel(language, currentSettings, settings) {
                    override fun initTabs(settings: CodeStyleSettings) {
                        addIndentOptionsTab(settings)
                        addSpacesTab(settings)
                        addBlankLinesTab(settings)
                        addWrappingAndBracesTab(settings)
                    }
                }
            }

            override fun getHelpTopic(): String = "reference.settingsdialog.codestyle.maml"
        }
    }

    override fun getConfigurableDisplayName(): String = MamlLanguage.displayName

    override fun createCustomSettings(settings: CodeStyleSettings): CustomCodeStyleSettings =
        MamlCodeStyleSettings(settings)

    override fun getLanguage(): Language = MamlLanguage
}