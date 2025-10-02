package com.davidseptimus.maml.highlighting

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.editor.colors.CodeInsightColors.*
import com.intellij.openapi.editor.colors.TextAttributesKey

object MamlTokenAttributes {
    // Base punctuation - other punctuation inherits from this
    val PUNCTUATION = TextAttributesKey.createTextAttributesKey("MAML_PUNCTUATION", DefaultLanguageHighlighterColors.OPERATION_SIGN)

    val BRACES = TextAttributesKey.createTextAttributesKey("MAML_BRACES", PUNCTUATION)
    val BRACKETS = TextAttributesKey.createTextAttributesKey("MAML_BRACKETS", PUNCTUATION)
    val COMMA = TextAttributesKey.createTextAttributesKey("MAML_COMMA", PUNCTUATION)
    val COLON = TextAttributesKey.createTextAttributesKey("MAML_COLON", PUNCTUATION)

    val STRING = TextAttributesKey.createTextAttributesKey("MAML_STRING", DefaultLanguageHighlighterColors.STRING)
    val MULTILINE_STRING =
        TextAttributesKey.createTextAttributesKey("MAML_MULTILINE_STRING", STRING)
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

    val VALID_ESCAPE = TextAttributesKey.createTextAttributesKey(
        "MAML_VALID_ESCAPE",
        DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE
    )

    val INVALID_ESCAPE = TextAttributesKey.createTextAttributesKey(
        "MAML_INVALID_ESCAPE",
        DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE
    )

    val URL = TextAttributesKey.createTextAttributesKey(
        "MAML_URL",
        HYPERLINK_ATTRIBUTES
    )

    val FILE_PATH = TextAttributesKey.createTextAttributesKey(
        "MAML_FILE_PATH",
        DefaultLanguageHighlighterColors.INSTANCE_FIELD
    )
}