package com.davidseptimus.maml.lang

import com.davidseptimus.maml.lang.psi.MamlTypes
import com.intellij.lexer.Lexer
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import org.junit.Assert.assertEquals
import org.junit.Test

class MamlLexerTest {

    private fun createLexer(): Lexer = MamlLexerAdapter()

    private fun doTest(text: String, vararg expectedTokens: Pair<IElementType, String>) {
        val lexer = createLexer()
        lexer.start(text)

        val actualTokens = mutableListOf<Pair<IElementType, String>>()
        while (lexer.tokenType != null) {
            // Skip whitespace tokens for test simplicity
            if (lexer.tokenType != TokenType.WHITE_SPACE) {
                actualTokens.add(lexer.tokenType!! to lexer.tokenText)
            }
            lexer.advance()
        }

        assertEquals(
            "Token mismatch.\nExpected: ${expectedTokens.toList()}\nActual: $actualTokens",
            expectedTokens.toList(),
            actualTokens
        )
    }

    // Basic Tokens Tests

    @Test
    fun `test left brace`() {
        doTest("{", MamlTypes.LBRACE to "{")
    }

    @Test
    fun `test right brace`() {
        doTest("}", MamlTypes.RBRACE to "}")
    }

    @Test
    fun `test left bracket`() {
        doTest("[", MamlTypes.LBRACKET to "[")
    }

    @Test
    fun `test right bracket`() {
        doTest("]", MamlTypes.RBRACKET to "]")
    }

    @Test
    fun `test colon`() {
        doTest(":", MamlTypes.COLON to ":")
    }

    @Test
    fun `test comma`() {
        doTest(",", MamlTypes.COMMA to ",")
    }

    @Test
    fun `test comment`() {
        doTest("# this is a comment", MamlTypes.COMMENT to "# this is a comment")
    }

    @Test
    fun `test comment with special characters`() {
        doTest("# comment with symbols: {}[],:\"",
            MamlTypes.COMMENT to "# comment with symbols: {}[],:\"")
    }

    // String Literal Tests

    @Test
    fun `test simple quoted string`() {
        doTest("\"hello\"", MamlTypes.STRING to "\"hello\"")
    }

    @Test
    fun `test empty string`() {
        doTest("\"\"", MamlTypes.STRING to "\"\"")
    }

    @Test
    fun `test string with spaces`() {
        doTest("\"hello world\"", MamlTypes.STRING to "\"hello world\"")
    }

    @Test
    fun `test string with escape sequences`() {
        doTest("\"hello\\nworld\"", MamlTypes.STRING to "\"hello\\nworld\"")
    }

    @Test
    fun `test string with tab escape`() {
        doTest("\"tab\\there\"", MamlTypes.STRING to "\"tab\\there\"")
    }

    @Test
    fun `test string with backslash escape`() {
        doTest("\"back\\\\slash\"", MamlTypes.STRING to "\"back\\\\slash\"")
    }

    @Test
    fun `test string with quote escape`() {
        doTest("\"say \\\"hello\\\"\"", MamlTypes.STRING to "\"say \\\"hello\\\"\"")
    }

    @Test
    fun `test multiline string`() {
        doTest("\"\"\"multiline\"\"\"", MamlTypes.MULTILINE_STRING to "\"\"\"multiline\"\"\"")
    }

    @Test
    fun `test multiline string with newlines`() {
        doTest("\"\"\"line1\nline2\"\"\"",
            MamlTypes.MULTILINE_STRING to "\"\"\"line1\nline2\"\"\"")
    }

    @Test
    fun `test multiline string empty`() {
        doTest("\"\"\"\"\"\"", MamlTypes.MULTILINE_STRING to "\"\"\"\"\"\"")
    }

    // Identifier Tests

    @Test
    fun `test simple identifier`() {
        doTest("key", MamlTypes.IDENTIFIER to "key")
    }

    @Test
    fun `test identifier with underscore`() {
        doTest("my_key", MamlTypes.IDENTIFIER to "my_key")
    }

    @Test
    fun `test identifier with dash`() {
        doTest("my-key", MamlTypes.IDENTIFIER to "my-key")
    }

    @Test
    fun `test identifier with numbers`() {
        doTest("key123", MamlTypes.IDENTIFIER to "key123")
    }

    @Test
    fun `test camelCase identifier`() {
        doTest("myKey", MamlTypes.IDENTIFIER to "myKey")
    }

    @Test
    fun `test identifier starting with underscore`() {
        doTest("_private", MamlTypes.IDENTIFIER to "_private")
    }

    // Number Tests

    @Test
    fun `test zero`() {
        doTest("0", MamlTypes.NUMBER to "0")
    }

    @Test
    fun `test positive integer`() {
        doTest("123", MamlTypes.NUMBER to "123")
    }

    @Test
    fun `test negative integer`() {
        doTest("-456", MamlTypes.NUMBER to "-456")
    }

    @Test
    fun `test decimal number`() {
        doTest("1.23", MamlTypes.NUMBER to "1.23")
    }

    @Test
    fun `test negative decimal`() {
        doTest("-4.56", MamlTypes.NUMBER to "-4.56")
    }

    @Test
    fun `test decimal starting with zero`() {
        doTest("0.789", MamlTypes.NUMBER to "0.789")
    }

    @Test
    fun `test scientific notation lowercase e`() {
        doTest("1e10", MamlTypes.NUMBER to "1e10")
    }

    @Test
    fun `test scientific notation uppercase E`() {
        doTest("2E5", MamlTypes.NUMBER to "2E5")
    }

    @Test
    fun `test scientific notation with negative exponent`() {
        doTest("1.5e-3", MamlTypes.NUMBER to "1.5e-3")
    }

    @Test
    fun `test scientific notation with positive exponent`() {
        doTest("2E+5", MamlTypes.NUMBER to "2E+5")
    }

    @Test
    fun `test large number`() {
        doTest("999999999", MamlTypes.NUMBER to "999999999")
    }

    // Boolean and Null Tests

    @Test
    fun `test true`() {
        doTest("true", MamlTypes.TRUE to "true")
    }

    @Test
    fun `test false`() {
        doTest("false", MamlTypes.FALSE to "false")
    }

    @Test
    fun `test null`() {
        doTest("null", MamlTypes.NULL to "null")
    }

    // Complex Token Sequences

    @Test
    fun `test empty object`() {
        doTest("{}",
            MamlTypes.LBRACE to "{",
            MamlTypes.RBRACE to "}")
    }

    @Test
    fun `test empty array`() {
        doTest("[]",
            MamlTypes.LBRACKET to "[",
            MamlTypes.RBRACKET to "]")
    }

    @Test
    fun `test simple key-value pair`() {
        doTest("key: \"value\"",
            MamlTypes.IDENTIFIER to "key",
            MamlTypes.COLON to ":",
            MamlTypes.STRING to "\"value\"")
    }

    @Test
    fun `test key-value with spaces`() {
        doTest("key : \"value\"",
            MamlTypes.IDENTIFIER to "key",
            MamlTypes.COLON to ":",
            MamlTypes.STRING to "\"value\"")
    }

    @Test
    fun `test array with numbers`() {
        doTest("[1, 2, 3]",
            MamlTypes.LBRACKET to "[",
            MamlTypes.NUMBER to "1",
            MamlTypes.COMMA to ",",
            MamlTypes.NUMBER to "2",
            MamlTypes.COMMA to ",",
            MamlTypes.NUMBER to "3",
            MamlTypes.RBRACKET to "]")
    }

    @Test
    fun `test object with single property`() {
        doTest("{ name: \"John\" }",
            MamlTypes.LBRACE to "{",
            MamlTypes.IDENTIFIER to "name",
            MamlTypes.COLON to ":",
            MamlTypes.STRING to "\"John\"",
            MamlTypes.RBRACE to "}")
    }

    @Test
    fun `test mixed value types`() {
        doTest("[1, \"string\", true, false, null]",
            MamlTypes.LBRACKET to "[",
            MamlTypes.NUMBER to "1",
            MamlTypes.COMMA to ",",
            MamlTypes.STRING to "\"string\"",
            MamlTypes.COMMA to ",",
            MamlTypes.TRUE to "true",
            MamlTypes.COMMA to ",",
            MamlTypes.FALSE to "false",
            MamlTypes.COMMA to ",",
            MamlTypes.NULL to "null",
            MamlTypes.RBRACKET to "]")
    }

    @Test
    fun `test comment before value`() {
        doTest("# comment\n123",
            MamlTypes.COMMENT to "# comment",
            MamlTypes.NUMBER to "123")
    }

    @Test
    fun `test multiple properties with comma`() {
        doTest("a: 1, b: 2",
            MamlTypes.IDENTIFIER to "a",
            MamlTypes.COLON to ":",
            MamlTypes.NUMBER to "1",
            MamlTypes.COMMA to ",",
            MamlTypes.IDENTIFIER to "b",
            MamlTypes.COLON to ":",
            MamlTypes.NUMBER to "2")
    }

    @Test
    fun `test trailing comma`() {
        doTest("[1, 2,]",
            MamlTypes.LBRACKET to "[",
            MamlTypes.NUMBER to "1",
            MamlTypes.COMMA to ",",
            MamlTypes.NUMBER to "2",
            MamlTypes.COMMA to ",",
            MamlTypes.RBRACKET to "]")
    }

    @Test
    fun `test nested structures`() {
        doTest("{a: [1, 2]}",
            MamlTypes.LBRACE to "{",
            MamlTypes.IDENTIFIER to "a",
            MamlTypes.COLON to ":",
            MamlTypes.LBRACKET to "[",
            MamlTypes.NUMBER to "1",
            MamlTypes.COMMA to ",",
            MamlTypes.NUMBER to "2",
            MamlTypes.RBRACKET to "]",
            MamlTypes.RBRACE to "}")
    }

    @Test
    fun `test quoted key vs identifier`() {
        doTest("\"key\": value",
            MamlTypes.STRING to "\"key\"",
            MamlTypes.COLON to ":",
            MamlTypes.IDENTIFIER to "value")
    }

    // Whitespace Tests

    @Test
    fun `test multiple spaces`() {
        doTest("1    2",
            MamlTypes.NUMBER to "1",
            MamlTypes.NUMBER to "2")
    }

    @Test
    fun `test tabs`() {
        doTest("1\t2",
            MamlTypes.NUMBER to "1",
            MamlTypes.NUMBER to "2")
    }

    @Test
    fun `test newlines`() {
        doTest("1\n2",
            MamlTypes.NUMBER to "1",
            MamlTypes.NUMBER to "2")
    }

    @Test
    fun `test mixed whitespace`() {
        doTest("1 \t\n 2",
            MamlTypes.NUMBER to "1",
            MamlTypes.NUMBER to "2")
    }

    // Edge Cases

    @Test
    fun `test empty input`() {
        doTest("")
    }

    @Test
    fun `test only whitespace`() {
        doTest("   \t\n  ")
    }

    @Test
    fun `test only comment`() {
        doTest("# just a comment", MamlTypes.COMMENT to "# just a comment")
    }

    @Test
    fun `test multiple comments`() {
        doTest("# comment 1\n# comment 2",
            MamlTypes.COMMENT to "# comment 1",
            MamlTypes.COMMENT to "# comment 2")
    }

    @Test
    fun `test comment at end of line`() {
        doTest("123 # comment",
            MamlTypes.NUMBER to "123",
            MamlTypes.COMMENT to "# comment")
    }

    @Test
    fun `test special identifier characters`() {
        doTest("my-special_key123",
            MamlTypes.IDENTIFIER to "my-special_key123")
    }

    @Test
    fun `test consecutive punctuation`() {
        doTest("{}[]:,",
            MamlTypes.LBRACE to "{",
            MamlTypes.RBRACE to "}",
            MamlTypes.LBRACKET to "[",
            MamlTypes.RBRACKET to "]",
            MamlTypes.COLON to ":",
            MamlTypes.COMMA to ",")
    }
}