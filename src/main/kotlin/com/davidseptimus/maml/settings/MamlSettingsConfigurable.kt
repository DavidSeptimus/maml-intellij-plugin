package com.davidseptimus.maml.settings

import com.davidseptimus.maml.MamlBundle
import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class MamlSettingsConfigurable : Configurable {
    private var settingsPanel: JPanel? = null

    private val commentLengthField = JBTextField().apply { columns = 5 }
    private val stringLengthField = JBTextField().apply { columns = 5 }
    private val showArrayCountHintsCheckBox = JBCheckBox(MamlBundle.message("settings.inlayHints.arrayCount"))

    override fun createComponent(): JComponent {
        val settings = MamlSettings.getInstance()

        // Initialize with current settings
        commentLengthField.text = settings.commentPreviewWords.toString()
        stringLengthField.text = settings.multilineStringPreviewWords.toString()
        showArrayCountHintsCheckBox.isSelected = settings.showArrayItemCountHints

        settingsPanel = FormBuilder.createFormBuilder()
            .addComponent(JBLabel("<html><b>${MamlBundle.message("settings.codeFolding")}</b></html>"))
            .addLabeledComponent(MamlBundle.message("settings.commentPreviewLength"), commentLengthField)
            .addLabeledComponent(MamlBundle.message("settings.stringPreviewLength"), stringLengthField)
            .addVerticalGap(15)
            .addComponent(JBLabel("<html><b>${MamlBundle.message("settings.inlayHints")}</b></html>"))
            .addComponent(showArrayCountHintsCheckBox)
            .addVerticalGap(15)
            .addComponent(JBLabel("<html><i>Configure JSON Schema mappings in Settings → Languages & Frameworks → Schemas and DTDs → JSON Schema Mappings</i></html>"))
            .addComponentFillVertically(JPanel(), 0)
            .panel

        return settingsPanel!!
    }

    override fun isModified(): Boolean {
        val settings = MamlSettings.getInstance()
        return commentLengthField.text.toIntOrNull() != settings.commentPreviewWords ||
                stringLengthField.text.toIntOrNull() != settings.multilineStringPreviewWords ||
                showArrayCountHintsCheckBox.isSelected != settings.showArrayItemCountHints
    }

    override fun apply() {
        val settings = MamlSettings.getInstance()
        settings.commentPreviewWords = commentLengthField.text.toIntOrNull() ?: 10
        settings.multilineStringPreviewWords = stringLengthField.text.toIntOrNull() ?: 10
        settings.showArrayItemCountHints = showArrayCountHintsCheckBox.isSelected
    }

    override fun reset() {
        val settings = MamlSettings.getInstance()
        commentLengthField.text = settings.commentPreviewWords.toString()
        stringLengthField.text = settings.multilineStringPreviewWords.toString()
        showArrayCountHintsCheckBox.isSelected = settings.showArrayItemCountHints
    }

    override fun getDisplayName(): String = MamlBundle.message("settings.displayName")
}