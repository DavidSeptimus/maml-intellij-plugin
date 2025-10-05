package com.davidseptimus.maml.formatter

import com.davidseptimus.maml.MamlBundle
import com.davidseptimus.maml.lang.MamlLanguage
import com.intellij.application.options.IndentOptionsEditor
import com.intellij.application.options.SmartIndentOptionsEditor
import com.intellij.application.options.codeStyle.properties.CodeStyleFieldAccessor
import com.intellij.application.options.codeStyle.properties.MagicIntegerConstAccessor
import com.intellij.lang.Language
import com.intellij.psi.codeStyle.*
import java.lang.reflect.Field

class MamlLanguageCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {

    override fun customizeSettings(
        consumer: CodeStyleSettingsCustomizable,
        settingsType: SettingsType
    ) {
        val instance = CodeStyleSettingsCustomizableOptions.getInstance()

        when (settingsType) {
            SettingsType.SPACING_SETTINGS -> {
                consumer.showStandardOptions(
                    "SPACE_WITHIN_BRACKETS",
                    "SPACE_WITHIN_BRACES",
                    "SPACE_AFTER_COMMA",
                    "SPACE_BEFORE_COMMA"
                )
                consumer.renameStandardOption("SPACE_WITHIN_BRACES", MamlBundle.message("formatter.space_within_braces.label"))
                consumer.showCustomOption(
                    MamlCodeStyleSettings::class.java,
                    "SPACE_BEFORE_COLON",
                    MamlBundle.message("formatter.space_before_colon.label"),
                    instance.SPACES_OTHER
                )
                consumer.showCustomOption(
                    MamlCodeStyleSettings::class.java,
                    "SPACE_AFTER_COLON",
                    MamlBundle.message("formatter.space_after_colon.label"),
                    instance.SPACES_OTHER
                )
            }
            SettingsType.BLANK_LINES_SETTINGS -> {
                consumer.showStandardOptions("KEEP_BLANK_LINES_IN_CODE")
            }
            SettingsType.WRAPPING_AND_BRACES_SETTINGS -> {
                consumer.showStandardOptions(
                    "RIGHT_MARGIN",
                    "WRAP_ON_TYPING",
                    "KEEP_LINE_BREAKS",
                    "WRAP_LONG_LINES"
                )

                consumer.showCustomOption(
                    MamlCodeStyleSettings::class.java,
                    "KEEP_TRAILING_COMMA",
                    MamlBundle.message("formatter.trailing_comma.label"),
                    instance.WRAPPING_KEEP
                )

                consumer.showCustomOption(
                    MamlCodeStyleSettings::class.java,
                    "ARRAY_WRAPPING",
                    MamlBundle.message("formatter.wrapping_arrays.label"),
                    null,
                    instance.WRAP_OPTIONS,
                    CodeStyleSettingsCustomizable.WRAP_VALUES
                )

                consumer.showCustomOption(
                    MamlCodeStyleSettings::class.java,
                    "OBJECT_WRAPPING",
                    MamlBundle.message("formatter.objects.label"),
                    null,
                    instance.WRAP_OPTIONS,
                    CodeStyleSettingsCustomizable.WRAP_VALUES
                )

                consumer.showCustomOption(
                    MamlCodeStyleSettings::class.java,
                    "PROPERTY_ALIGNMENT",
                    MamlBundle.message("formatter.align.properties.caption"),
                    MamlBundle.message("formatter.objects.label"),
                    ALIGN_OPTIONS,
                    ALIGN_VALUES
                )

                consumer.showCustomOption(
                    MamlCodeStyleSettings::class.java,
                    "SPACE_AFTER_COMMENT_HASH",
                    MamlBundle.message("formatter.space_after_comment_hash.label"),
                    instance.WRAPPING_COMMENTS,
                        arrayOf(
                            MamlBundle.message("formatter.space_options.any"),
                            MamlBundle.message("formatter.space_options.at_least_one"),
                            MamlBundle.message("formatter.space_options.exactly_one")
                        ),
                    intArrayOf(0, 1, 2)
                )

                consumer.showCustomOption(
                    MamlCodeStyleSettings::class.java,
                    "REMOVE_COMMAS",
                    MamlBundle.message("formatter.wrapping_remove_commas.label"),
                    MamlBundle.message("formatter.wrapping.other_group.label")
                )

                consumer.showCustomOption(
                    MamlCodeStyleSettings::class.java,
                    "KEY_QUOTING_STYLE",
                    MamlBundle.message("formatter.key_quoting.label"),
                    MamlBundle.message("formatter.wrapping.other_group.label"),
                    KEY_QUOTING_OPTIONS,
                    KEY_QUOTING_VALUES
                )
            }
            else -> {}
        }
    }

    override fun getLanguage(): Language = MamlLanguage

    override fun getIndentOptionsEditor(): IndentOptionsEditor = SmartIndentOptionsEditor()

    override fun getCodeSample(settingsType: SettingsType): String = SAMPLE

    override fun customizeDefaults(
        commonSettings: CommonCodeStyleSettings,
        indentOptions: CommonCodeStyleSettings.IndentOptions
    ) {
        indentOptions.INDENT_SIZE = 2
        // strip all blank lines by default
        commonSettings.KEEP_BLANK_LINES_IN_CODE = 0
    }

    override fun getAccessor(codeStyleObject: Any, field: Field): CodeStyleFieldAccessor<*, *>? {
        if (codeStyleObject is MamlCodeStyleSettings) {
            return when (field.name) {
                "PROPERTY_ALIGNMENT" -> MagicIntegerConstAccessor(
                    codeStyleObject,
                    field,
                    intArrayOf(
                        MamlCodeStyleSettings.PropertyAlignment.DO_NOT_ALIGN.id,
                        MamlCodeStyleSettings.PropertyAlignment.ALIGN_ON_VALUE.id,
                        MamlCodeStyleSettings.PropertyAlignment.ALIGN_ON_COLON.id
                    ),
                    arrayOf(
                        "do_not_align",
                        "align_on_value",
                        "align_on_colon"
                    )
                )

                "KEY_QUOTING_STYLE" -> MagicIntegerConstAccessor(
                    codeStyleObject,
                    field,
                    intArrayOf(
                        MamlCodeStyleSettings.KeyQuotingStyle.DO_NOT_MODIFY.id,
                        MamlCodeStyleSettings.KeyQuotingStyle.REMOVE_QUOTES.id,
                        MamlCodeStyleSettings.KeyQuotingStyle.ADD_QUOTES.id
                    ),
                    arrayOf(
                        "do_not_modify",
                        "remove_quotes",
                        "add_quotes"
                    )
                )

                else -> null
            }
        }
        return null
    }

    override fun createCustomSettings(settings: CodeStyleSettings): CustomCodeStyleSettings =
        MamlCodeStyleSettings(settings)

}

private val ALIGN_OPTIONS = MamlCodeStyleSettings.PropertyAlignment.entries.map { it.description }.toTypedArray()
private val ALIGN_VALUES = MamlCodeStyleSettings.PropertyAlignment.entries.map { it.id }.toIntArray()

private val KEY_QUOTING_OPTIONS = MamlCodeStyleSettings.KeyQuotingStyle.entries.map { it.description }.toTypedArray()
private val KEY_QUOTING_VALUES = MamlCodeStyleSettings.KeyQuotingStyle.entries.map { it.id }.toIntArray()

private val SAMPLE = """
    {
        maml-literals: {
            strings: ["foo", "bar", "\u0062\u0061\u0072"],
            numbers: [42, 6.62606975e-34],
            "boolean values": [true, false,],
            objects: {null-value: null,"another": null,}
        },
        "mixed keys": {
            identifier-key: "value",
            "quoted key": 123,
            multiline: ""${'"'}
                This is a
                multiline string
            ""${'"'}
        }
    }
""".trimIndent()