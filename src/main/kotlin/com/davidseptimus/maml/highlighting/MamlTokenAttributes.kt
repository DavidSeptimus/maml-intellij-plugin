package com.davidseptimus.maml.highlighting

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey

object MamlTokenAttributes {
    val BRACES = TextAttributesKey.createTextAttributesKey("MAML_BRACES", DefaultLanguageHighlighterColors.BRACES)
    val BRACKETS = TextAttributesKey.createTextAttributesKey("MAML_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS)
    val COMMA = TextAttributesKey.createTextAttributesKey("MAML_COMMA", DefaultLanguageHighlighterColors.COMMA)
    val COLON = TextAttributesKey.createTextAttributesKey("MAML_COLON", DefaultLanguageHighlighterColors.OPERATION_SIGN)

    val STRING = TextAttributesKey.createTextAttributesKey("MAML_STRING", DefaultLanguageHighlighterColors.STRING)
    val MULTILINE_STRING =
        TextAttributesKey.createTextAttributesKey("MAML_MULTILINE_STRING", DefaultLanguageHighlighterColors.STRING)
    val NUMBER = TextAttributesKey.createTextAttributesKey("MAML_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
    val KEYWORD = TextAttributesKey.createTextAttributesKey("MAML_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)

    val IDENTIFIER =
        TextAttributesKey.createTextAttributesKey("MAML_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER)
    val KEY = TextAttributesKey.createTextAttributesKey("MAML_KEY", DefaultLanguageHighlighterColors.INSTANCE_FIELD)

    val COMMENT =
        TextAttributesKey.createTextAttributesKey("MAML_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)

    val BAD_CHARACTER = TextAttributesKey.createTextAttributesKey(
        "MAML_BAD_CHARACTER",
        DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE
    )
}