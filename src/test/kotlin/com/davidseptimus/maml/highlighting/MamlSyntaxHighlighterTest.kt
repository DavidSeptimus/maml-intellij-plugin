package com.davidseptimus.maml.highlighting

import com.davidseptimus.maml.lang.psi.MamlTypes
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class MamlSyntaxHighlighterTest {

    private fun assertHighlighting(tokenType: IElementType, expected: TextAttributesKey) {
        val highlighter = MamlSyntaxHighlighter()
        val highlights = highlighter.getTokenHighlights(tokenType)

        assertEquals(
            "Expected exactly one highlight for token $tokenType",
            1,
            highlights.size
        )

        assertEquals(
            "Token $tokenType should map to $expected",
            expected,
            highlights[0]
        )
    }

    private fun assertNoHighlighting(tokenType: IElementType?) {
        val highlighter = MamlSyntaxHighlighter()
        val highlights = highlighter.getTokenHighlights(tokenType)

        assertEquals(
            "Expected no highlighting for token $tokenType",
            0,
            highlights.size
        )
    }

    // Braces and Brackets Tests

    @Test
    fun testLeftBraceHighlighting() {
        assertHighlighting(MamlTypes.LBRACE, MamlTokenAttributes.BRACES)
    }

    @Test
    fun testRightBraceHighlighting() {
        assertHighlighting(MamlTypes.RBRACE, MamlTokenAttributes.BRACES)
    }

    @Test
    fun testLeftBracketHighlighting() {
        assertHighlighting(MamlTypes.LBRACKET, MamlTokenAttributes.BRACKETS)
    }

    @Test
    fun testRightBracketHighlighting() {
        assertHighlighting(MamlTypes.RBRACKET, MamlTokenAttributes.BRACKETS)
    }

    @Test
    fun testBracesAreDifferentFromBrackets() {
        val highlighter = MamlSyntaxHighlighter()
        val braceHighlights = highlighter.getTokenHighlights(MamlTypes.LBRACE)
        val bracketHighlights = highlighter.getTokenHighlights(MamlTypes.LBRACKET)

        // Both should use their respective attributes
        assertEquals(MamlTokenAttributes.BRACES, braceHighlights[0])
        assertEquals(MamlTokenAttributes.BRACKETS, bracketHighlights[0])
    }

    // Punctuation Tests

    @Test
    fun testCommaHighlighting() {
        assertHighlighting(MamlTypes.COMMA, MamlTokenAttributes.COMMA)
    }

    @Test
    fun testColonHighlighting() {
        assertHighlighting(MamlTypes.COLON, MamlTokenAttributes.COLON)
    }

    // String Tests

    @Test
    fun testStringHighlighting() {
        assertHighlighting(MamlTypes.STRING, MamlTokenAttributes.STRING)
    }

    @Test
    fun testMultilineStringHighlighting() {
        assertHighlighting(MamlTypes.MULTILINE_STRING, MamlTokenAttributes.MULTILINE_STRING)
    }

    @Test
    fun testMultilineStringInheritsFromString() {
        // Multiline string should have its own key that falls back to STRING
        val multilineKey = MamlTokenAttributes.MULTILINE_STRING
        val stringKey = MamlTokenAttributes.STRING

        // Check that multiline string has STRING as its fallback
        assertEquals(
            "MULTILINE_STRING should have STRING as fallback",
            stringKey,
            multilineKey.fallbackAttributeKey
        )
    }

    // Number Tests

    @Test
    fun testNumberHighlighting() {
        assertHighlighting(MamlTypes.NUMBER, MamlTokenAttributes.NUMBER)
    }

    // Keyword Tests

    @Test
    fun testTrueHighlighting() {
        assertHighlighting(MamlTypes.TRUE, MamlTokenAttributes.KEYWORD)
    }

    @Test
    fun testFalseHighlighting() {
        assertHighlighting(MamlTypes.FALSE, MamlTokenAttributes.KEYWORD)
    }

    @Test
    fun testNullHighlighting() {
        assertHighlighting(MamlTypes.NULL, MamlTokenAttributes.KEYWORD)
    }

    @Test
    fun testAllKeywordsUseSameHighlighting() {
        val highlighter = MamlSyntaxHighlighter()
        val trueHighlights = highlighter.getTokenHighlights(MamlTypes.TRUE)
        val falseHighlights = highlighter.getTokenHighlights(MamlTypes.FALSE)
        val nullHighlights = highlighter.getTokenHighlights(MamlTypes.NULL)

        assertArrayEquals("true and false should have same highlighting", trueHighlights, falseHighlights)
        assertArrayEquals("true and null should have same highlighting", trueHighlights, nullHighlights)
    }

    // Identifier Tests

    @Test
    fun testIdentifierHighlighting() {
        assertHighlighting(MamlTypes.IDENTIFIER, MamlTokenAttributes.IDENTIFIER)
    }

    // Comment Tests

    @Test
    fun testCommentHighlighting() {
        assertHighlighting(MamlTypes.COMMENT, MamlTokenAttributes.COMMENT)
    }

    // Bad Character Tests

    @Test
    fun testBadCharacterHighlighting() {
        assertHighlighting(TokenType.BAD_CHARACTER, MamlTokenAttributes.BAD_CHARACTER)
    }

    // Edge Cases

    @Test
    fun testNullTokenTypeReturnsEmptyArray() {
        assertNoHighlighting(null)
    }

    @Test
    fun testWhitespaceHasNoHighlighting() {
        assertNoHighlighting(TokenType.WHITE_SPACE)
    }

    // Attribute Inheritance Tests

    @Test
    fun testBracesInheritFromPunctuation() {
        assertEquals(
            "BRACES should have PUNCTUATION as fallback",
            MamlTokenAttributes.PUNCTUATION,
            MamlTokenAttributes.BRACES.fallbackAttributeKey
        )
    }

    @Test
    fun testBracketsInheritFromPunctuation() {
        assertEquals(
            "BRACKETS should have PUNCTUATION as fallback",
            MamlTokenAttributes.PUNCTUATION,
            MamlTokenAttributes.BRACKETS.fallbackAttributeKey
        )
    }

    @Test
    fun testCommaInheritsFromPunctuation() {
        assertEquals(
            "COMMA should have PUNCTUATION as fallback",
            MamlTokenAttributes.PUNCTUATION,
            MamlTokenAttributes.COMMA.fallbackAttributeKey
        )
    }

    @Test
    fun testColonInheritsFromPunctuation() {
        assertEquals(
            "COLON should have PUNCTUATION as fallback",
            MamlTokenAttributes.PUNCTUATION,
            MamlTokenAttributes.COLON.fallbackAttributeKey
        )
    }

    // Token Attributes Completeness Tests

    @Test
    fun testAllTokenTypesHaveDefinedHighlighting() {
        val highlighter = MamlSyntaxHighlighter()

        // Test all token types that should have highlighting
        val tokensWithHighlighting = listOf(
            MamlTypes.LBRACE,
            MamlTypes.RBRACE,
            MamlTypes.LBRACKET,
            MamlTypes.RBRACKET,
            MamlTypes.COMMA,
            MamlTypes.COLON,
            MamlTypes.STRING,
            MamlTypes.MULTILINE_STRING,
            MamlTypes.NUMBER,
            MamlTypes.TRUE,
            MamlTypes.FALSE,
            MamlTypes.NULL,
            MamlTypes.IDENTIFIER,
            MamlTypes.COMMENT,
            TokenType.BAD_CHARACTER
        )

        for (tokenType in tokensWithHighlighting) {
            val highlights = highlighter.getTokenHighlights(tokenType)
            assert(highlights.isNotEmpty()) {
                "Token $tokenType should have highlighting defined"
            }
        }
    }

    @Test
    fun testNoTokenHasMultipleHighlights() {
        val highlighter = MamlSyntaxHighlighter()

        val allTokens = listOf(
            MamlTypes.LBRACE,
            MamlTypes.RBRACE,
            MamlTypes.LBRACKET,
            MamlTypes.RBRACKET,
            MamlTypes.COMMA,
            MamlTypes.COLON,
            MamlTypes.STRING,
            MamlTypes.MULTILINE_STRING,
            MamlTypes.NUMBER,
            MamlTypes.TRUE,
            MamlTypes.FALSE,
            MamlTypes.NULL,
            MamlTypes.IDENTIFIER,
            MamlTypes.COMMENT,
            TokenType.BAD_CHARACTER
        )

        for (tokenType in allTokens) {
            val highlights = highlighter.getTokenHighlights(tokenType)
            assert(highlights.size <= 1) {
                "Token $tokenType should have at most one highlight, but has ${highlights.size}"
            }
        }
    }

    // Consistency Tests

    @Test
    fun testSymmetricPairsUseSameHighlighting() {
        val highlighter = MamlSyntaxHighlighter()

        // Left and right braces should use same highlighting
        assertArrayEquals(
            "Left and right braces should use same highlighting",
            highlighter.getTokenHighlights(MamlTypes.LBRACE),
            highlighter.getTokenHighlights(MamlTypes.RBRACE)
        )

        // Left and right brackets should use same highlighting
        assertArrayEquals(
            "Left and right brackets should use same highlighting",
            highlighter.getTokenHighlights(MamlTypes.LBRACKET),
            highlighter.getTokenHighlights(MamlTypes.RBRACKET)
        )
    }

    // TextAttributesKey Tests

    @Test
    fun testAttributeKeysHaveValidExternalNames() {
        val attributes = listOf(
            MamlTokenAttributes.PUNCTUATION,
            MamlTokenAttributes.BRACES,
            MamlTokenAttributes.BRACKETS,
            MamlTokenAttributes.COMMA,
            MamlTokenAttributes.COLON,
            MamlTokenAttributes.STRING,
            MamlTokenAttributes.MULTILINE_STRING,
            MamlTokenAttributes.NUMBER,
            MamlTokenAttributes.KEYWORD,
            MamlTokenAttributes.IDENTIFIER,
            MamlTokenAttributes.KEY,
            MamlTokenAttributes.COMMENT,
            MamlTokenAttributes.BAD_CHARACTER
        )

        for (attr in attributes) {
            assert(attr.externalName.startsWith("MAML_")) {
                "Attribute ${attr.externalName} should start with 'MAML_'"
            }
            assert(attr.externalName.isNotBlank()) {
                "Attribute should have non-blank external name"
            }
        }
    }

    @Test
    fun testAllAttributeKeysAreUnique() {
        val allKeys = listOf(
            MamlTokenAttributes.PUNCTUATION,
            MamlTokenAttributes.BRACES,
            MamlTokenAttributes.BRACKETS,
            MamlTokenAttributes.COMMA,
            MamlTokenAttributes.COLON,
            MamlTokenAttributes.STRING,
            MamlTokenAttributes.MULTILINE_STRING,
            MamlTokenAttributes.NUMBER,
            MamlTokenAttributes.KEYWORD,
            MamlTokenAttributes.IDENTIFIER,
            MamlTokenAttributes.KEY,
            MamlTokenAttributes.COMMENT,
            MamlTokenAttributes.BAD_CHARACTER
        )

        val externalNames = allKeys.map { it.externalName }
        val uniqueNames = externalNames.toSet()

        assertEquals(
            "All attribute keys should have unique external names",
            externalNames.size,
            uniqueNames.size
        )
    }
}