package com.davidseptimus.maml.highlighting

import com.davidseptimus.maml.lang.psi.MamlTypes
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType

object MamlSyntaxHighlighterColors {
    fun getTokenHighlights(tokenType: IElementType?): Array<TextAttributesKey> {
        return when (tokenType) {
            MamlTypes.LBRACE, MamlTypes.RBRACE -> arrayOf(MamlTokenAttributes.BRACES)
            MamlTypes.LBRACKET, MamlTypes.RBRACKET -> arrayOf(MamlTokenAttributes.BRACKETS)
            MamlTypes.COMMA -> arrayOf(MamlTokenAttributes.COMMA)
            MamlTypes.COLON -> arrayOf(MamlTokenAttributes.COLON)

            MamlTypes.STRING -> arrayOf(MamlTokenAttributes.STRING)
            MamlTypes.MULTILINE_STRING -> arrayOf(MamlTokenAttributes.MULTILINE_STRING)
            MamlTypes.NUMBER -> arrayOf(MamlTokenAttributes.NUMBER)

            MamlTypes.TRUE, MamlTypes.FALSE, MamlTypes.NULL -> arrayOf(MamlTokenAttributes.KEYWORD)

            MamlTypes.IDENTIFIER -> arrayOf(MamlTokenAttributes.IDENTIFIER)

            MamlTypes.COMMENT -> arrayOf(MamlTokenAttributes.COMMENT)

            TokenType.BAD_CHARACTER -> arrayOf(MamlTokenAttributes.BAD_CHARACTER)

            else -> emptyArray()
        }
    }
}