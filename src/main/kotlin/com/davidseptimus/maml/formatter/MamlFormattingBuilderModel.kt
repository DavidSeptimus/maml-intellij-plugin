package com.davidseptimus.maml.formatter

import com.davidseptimus.maml.lang.MamlLanguage
import com.davidseptimus.maml.lang.psi.MamlTypes.*
import com.intellij.formatting.*
import com.intellij.psi.codeStyle.CodeStyleSettings

class MamlFormattingBuilderModel : FormattingModelBuilder {
    override fun createModel(formattingContext: FormattingContext): FormattingModel {
        val settings = formattingContext.codeStyleSettings
        val customSettings = settings.getCustomSettings(MamlCodeStyleSettings::class.java)
        val spacingBuilder = createSpacingBuilder(settings)
        val block = MamlBlock(
            parent = null,
            node = formattingContext.node,
            customSettings = customSettings,
            alignment = null,
            indent = Indent.getSmartIndent(Indent.Type.CONTINUATION),
            wrap = null,
            spacingBuilder = spacingBuilder
        )
        return FormattingModelProvider.createFormattingModelForPsiFile(
            formattingContext.containingFile,
            block,
            settings
        )
    }

    companion object {
        fun createSpacingBuilder(settings: CodeStyleSettings): SpacingBuilder {
            val mamlSettings = settings.getCustomSettings(MamlCodeStyleSettings::class.java)
            val commonSettings = settings.getCommonSettings(MamlLanguage)

            val spacesBeforeComma = if (commonSettings.SPACE_BEFORE_COMMA) 1 else 0
            val spacesBeforeColon = if (mamlSettings.SPACE_BEFORE_COLON) 1 else 0
            val spacesAfterColon = if (mamlSettings.SPACE_AFTER_COLON) 1 else 0

            return SpacingBuilder(settings, MamlLanguage)
                .before(COLON).spacing(spacesBeforeColon, spacesBeforeColon, 0, false, 0)
                .after(COLON).spacing(spacesAfterColon, spacesAfterColon, 0, false, 0)
                .withinPair(LBRACKET, RBRACKET).spaceIf(commonSettings.SPACE_WITHIN_BRACKETS, true)
                .withinPair(LBRACE, RBRACE).spaceIf(commonSettings.SPACE_WITHIN_BRACES, true)
                .before(COMMA).spacing(spacesBeforeComma, spacesBeforeComma, 0, false, 0)
                .after(COMMA).spaceIf(commonSettings.SPACE_AFTER_COMMA)
        }
    }
}