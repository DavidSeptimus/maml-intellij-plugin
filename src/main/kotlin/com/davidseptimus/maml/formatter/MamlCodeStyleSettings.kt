package com.davidseptimus.maml.formatter

import com.davidseptimus.maml.MamlBundle
import com.davidseptimus.maml.lang.MamlLanguage
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.codeStyle.CustomCodeStyleSettings

class MamlCodeStyleSettings(container: CodeStyleSettings) :
    CustomCodeStyleSettings(MamlLanguage.id, container) {

    @JvmField
    var SPACE_AFTER_COMMENT_HASH: Int = CommentHashSpaceOptions.AT_LEAST_ONE.id

    @JvmField
    var REMOVE_COMMAS: Boolean = false

    @JvmField
    var UNQUOTE_SAFE_KEYS: Boolean = false

    @JvmField
    var SPACE_AFTER_COLON: Boolean = true

    @JvmField
    var SPACE_BEFORE_COLON: Boolean = false

    @JvmField
    var KEEP_TRAILING_COMMA: Boolean = false

    @JvmField
    @CommonCodeStyleSettings.WrapConstant
    var PROPERTY_ALIGNMENT: Int = PropertyAlignment.DO_NOT_ALIGN.id

    @JvmField
    @CommonCodeStyleSettings.WrapConstant
    var OBJECT_WRAPPING: Int = CommonCodeStyleSettings.WRAP_ALWAYS

    @JvmField
    @CommonCodeStyleSettings.WrapConstant
    var ARRAY_WRAPPING: Int = CommonCodeStyleSettings.WRAP_ALWAYS

    enum class PropertyAlignment(val id: Int, private val key: String) {
        DO_NOT_ALIGN(0, "formatter.align.properties.none"),
        ALIGN_ON_VALUE(1, "formatter.align.properties.on.value"),
        ALIGN_ON_COLON(2, "formatter.align.properties.on.colon");

        val description: String
            get() = MamlBundle.message(key)
    }

    companion object {
        const val DO_NOT_ALIGN_PROPERTY: Int = 0
        const val ALIGN_PROPERTY_ON_VALUE: Int = 1
        const val ALIGN_PROPERTY_ON_COLON: Int = 2
    }
}

enum class CommentHashSpaceOptions(val id: Int) {
    ANY(0),
    AT_LEAST_ONE(1),
    EXACTLY_ONE(2)
}
