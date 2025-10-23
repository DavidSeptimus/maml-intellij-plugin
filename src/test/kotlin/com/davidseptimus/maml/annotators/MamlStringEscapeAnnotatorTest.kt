package com.davidseptimus.maml.annotators

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Test suite for MamlStringEscapeAnnotator validating escape sequences
 * including traditional escapes and unicode scalar values.
 */
class MamlStringEscapeAnnotatorTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String = "src/test/testData"

    // ========== Valid Regular Escape Sequences ==========

    fun testValidBackslashEscape() {
        myFixture.configureByText("test.maml", """{ key: "backslash: \\\\"}""")
        myFixture.checkHighlighting()
        assertNoErrors()
    }

    fun testValidQuoteEscape() {
        myFixture.configureByText("test.maml", """{ key: "quote: \""}""")
        myFixture.checkHighlighting()
        assertNoErrors()
    }

    fun testValidNewlineEscape() {
        myFixture.configureByText("test.maml", """{ key: "newline: \n"}""")
        myFixture.checkHighlighting()
        assertNoErrors()
    }

    fun testValidCarriageReturnEscape() {
        myFixture.configureByText("test.maml", """{ key: "cr: \r"}""")
        myFixture.checkHighlighting()
        assertNoErrors()
    }

    fun testValidTabEscape() {
        myFixture.configureByText("test.maml", """{ key: "tab: \t"}""")
        myFixture.checkHighlighting()
        assertNoErrors()
    }

    fun testMultipleValidEscapes() {
        myFixture.configureByText("test.maml", """{ key: "line1\nline2\ttabbed\"quoted\\\\"}""")
        myFixture.checkHighlighting()
        assertNoErrors()
    }

    // ========== Invalid Regular Escape Sequences ==========

    fun testInvalidEscapeA() {
        myFixture.configureByText("test.maml", """{ key: "<error descr="Invalid escape sequence: '\a'">\a</error>" }""")
        myFixture.checkHighlighting()
    }

    fun testInvalidEscapeX() {
        myFixture.configureByText(
            "test.maml",
            """{ key: "test<error descr="Invalid escape sequence: '\x'">\x</error>test" }"""
        )
        myFixture.checkHighlighting()
    }

    fun testInvalidEscapeB() {
        myFixture.configureByText("test.maml", """{ key: "<error descr="Invalid escape sequence: '\b'">\b</error>" }""")
        myFixture.checkHighlighting()
    }

    fun testInvalidEscapeF() {
        myFixture.configureByText("test.maml", """{ key: "<error descr="Invalid escape sequence: '\f'">\f</error>" }""")
        myFixture.checkHighlighting()
    }

    fun testInvalidEscapeV() {
        myFixture.configureByText("test.maml", """{ key: "<error descr="Invalid escape sequence: '\v'">\v</error>" }""")
        myFixture.checkHighlighting()
    }

    // ========== Valid Unicode Scalar Escapes ==========

    fun testValidUnicodeSingleDigit() {
        myFixture.configureByText("test.maml", """{ key: "{0}"}""")
        myFixture.checkHighlighting()
        assertNoErrors()
    }

    fun testValidUnicodeTwoDigits() {
        myFixture.configureByText("test.maml", """{ key: "{41}"}""")
        myFixture.checkHighlighting()
        assertNoErrors()
    }

    fun testValidUnicodeFourDigits() {
        myFixture.configureByText("test.maml", """{ key: "{1F600}"}""")
        myFixture.checkHighlighting()
        assertNoErrors()
    }

    fun testValidUnicodeSixDigits() {
        myFixture.configureByText("test.maml", """{ key: "{10FFFF}"}""")
        myFixture.checkHighlighting()
        assertNoErrors()
    }

    fun testValidUnicodeLowercase() {
        myFixture.configureByText("test.maml", """{ key: "{1f4a9}"}""")
        myFixture.checkHighlighting()
        assertNoErrors()
    }

    fun testValidUnicodeUppercase() {
        myFixture.configureByText("test.maml", """{ key: "{1F4A9}"}""")
        myFixture.checkHighlighting()
        assertNoErrors()
    }

    fun testValidUnicodeMixedCase() {
        myFixture.configureByText("test.maml", """{ key: "{1F4a9}"}""")
        myFixture.checkHighlighting()
        assertNoErrors()
    }

    fun testValidUnicodeNull() {
        myFixture.configureByText("test.maml", """{ key: "{0000}"}""")
        myFixture.checkHighlighting()
        assertNoErrors()
    }

    fun testMultipleValidUnicodeEscapes() {
        myFixture.configureByText("test.maml", """{ key: "{48}{65}{6C}{6C}{6F}"}""")
        myFixture.checkHighlighting()
        assertNoErrors()
    }

    fun testMixedValidEscapes() {
        myFixture.configureByText("test.maml", """{ key: "Hello\nWorld{2764}\tTab"}""")
        myFixture.checkHighlighting()
        assertNoErrors()
    }

    // ========== Invalid Unicode Scalar Escapes - Format Errors ==========

    fun testInvalidUnicodeEmpty() {
        myFixture.configureByText(
            "test.maml",
            """{ key: "<error descr="Invalid escape sequence: '\u{}' (empty)">\u{}</error>" }"""
        )
        myFixture.checkHighlighting()
    }

    fun testInvalidUnicodeMissingOpenBrace() {
        myFixture.configureByText(
            "test.maml",
            """{ key: "<error descr="Invalid escape sequence: '\u' (missing '{')">\u</error>123}"}"""
        )
        myFixture.checkHighlighting()
    }

    fun testInvalidUnicodeAtEndOfString() {
        // \u at the end of string with no following characters
        myFixture.configureByText(
            "test.maml",
            """{ key: "test<error descr="Invalid escape sequence: '\u' (missing '{')">\u</error>" }"""
        )
        myFixture.checkHighlighting()
    }

    fun testInvalidUnicodeWithOnlyOneCharAfter() {
        // \u followed by only one character (no room for {)
        myFixture.configureByText(
            "test.maml",
            """{ key: "test<error descr="Invalid escape sequence: '\u' (missing '{')">\u</error>a" }"""
        )
        myFixture.checkHighlighting()
    }

    fun testInvalidUnicodeMissingCloseBraceUnterminatedString() {
        // This test is caught as an "Unterminated string literal" by the lexer before our annotator runs
        val text =
            """{ key: <error descr="Unterminated string literal">"'\u{123'</error><EOLError descr="<incomplete key value>, <key value>, MamlTokenType.COMMA or MamlTokenType.RBRACE expected"></EOLError>"""
        myFixture.configureByText("test.maml", text.replace("\\u", "\\u"))
        myFixture.checkHighlighting()
    }

    fun testInvalidUnicodeMissingCloseBraceInValidString() {
        // String is properly closed, but unicode escape is missing the closing brace
        // The annotator stops at the closing quote (doesn't include it in the error)
        val text = """{ key: "<error descr="Invalid escape sequence: '\u{123' (missing '}')">\u{123</error>" }"""
        myFixture.configureByText("test.maml", text.replace("\\u", "\\u"))
        myFixture.checkHighlighting()
    }

    fun testInvalidUnicodeTooManyDigits() {
        val text =
            """{ key: "<error descr="Invalid escape sequence: '\u{1234567}' (too many digits)">\u{1234567}</error>" }"""
        myFixture.configureByText("test.maml", text.replace("\\u", "\\u"))
        myFixture.checkHighlighting()
    }

    fun testInvalidUnicodeNonHexCharacters() {
        val text = """{ key: "<error descr="Invalid escape sequence: '\u{GHIJ}' (invalid hex)">\u{GHIJ}</error>" }"""
        myFixture.configureByText("test.maml", text.replace("\\u", "\\u"))
        myFixture.checkHighlighting()
    }

    fun testInvalidUnicodeSpaces() {
        // With our improved logic, we stop at the first space (whitespace detection)
        val text = """{ key: "<error descr="Invalid escape sequence: '\u{1' (missing '}')">\u{1</error> 2 3}" }"""
        myFixture.configureByText("test.maml", text.replace("\\u", "\\u"))
        myFixture.checkHighlighting()
    }

    fun testInvalidUnicodeMissingCloseBraceStopsAtWhitespace() {
        // Unicode escape without closing brace followed by space - should stop at the space
        val text = """{ key: "<error descr="Invalid escape sequence: '\u{123' (missing '}')">\u{123</error> abc" }"""
        myFixture.configureByText("test.maml", text.replace("\\u", "\\u"))
        myFixture.checkHighlighting()
    }

    // ========== Invalid Unicode Scalar Escapes - Value Errors ==========

    fun testInvalidUnicodeCodePointTooLarge() {
        val text =
            """{ key: "<error descr="Invalid escape sequence: '\u{110000}' (code point too large)">\u{110000}</error>" }"""
        myFixture.configureByText("test.maml", text.replace("\\u", "\\u"))
        myFixture.checkHighlighting()
    }

    fun testInvalidUnicodeSurrogateLow() {
        val text =
            """{ key: "<error descr="Invalid escape sequence: '\u{D800}' (surrogate code point)">\u{D800}</error>" }"""
        myFixture.configureByText("test.maml", text.replace("\\u", "\\u"))
        myFixture.checkHighlighting()
    }

    fun testInvalidUnicodeSurrogateMid() {
        val text =
            """{ key: "<error descr="Invalid escape sequence: '\u{DB00}' (surrogate code point)">\u{DB00}</error>" }"""
        myFixture.configureByText("test.maml", text.replace("\\u", "\\u"))
        myFixture.checkHighlighting()
    }

    fun testInvalidUnicodeSurrogateHigh() {
        val text =
            """{ key: "<error descr="Invalid escape sequence: '\u{DFFF}' (surrogate code point)">\u{DFFF}</error>" }"""
        myFixture.configureByText("test.maml", text.replace("\\u", "\\u"))
        myFixture.checkHighlighting()
    }

    fun testInvalidUnicodeSurrogateLowBoundary() {
        val text =
            """{ key: "<error descr="Invalid escape sequence: '\u{D800}' (surrogate code point)">\u{D800}</error>" }"""
        myFixture.configureByText("test.maml", text.replace("\\u", "\\u"))
        myFixture.checkHighlighting()
    }

    fun testInvalidUnicodeSurrogateHighBoundary() {
        val text =
            """{ key: "<error descr="Invalid escape sequence: '\u{DFFF}' (surrogate code point)">\u{DFFF}</error>" }"""
        myFixture.configureByText("test.maml", text.replace("\\u", "\\u"))
        myFixture.checkHighlighting()
    }

    // ========== Edge Cases ==========

    fun testValidUnicodeJustBelowSurrogateRange() {
        myFixture.configureByText("test.maml", """{ key: "{D7FF}"}""")
        myFixture.checkHighlighting()
        assertNoErrors()
    }

    fun testValidUnicodeJustAboveSurrogateRange() {
        myFixture.configureByText("test.maml", """{ key: "{E000}"}""")
        myFixture.checkHighlighting()
        assertNoErrors()
    }

    fun testValidUnicodeMaxCodePoint() {
        myFixture.configureByText("test.maml", """{ key: "{10FFFF}"}""")
        myFixture.checkHighlighting()
        assertNoErrors()
    }

    fun testInvalidUnicodeJustAboveMaxCodePoint() {
        val text =
            """{ key: "<error descr="Invalid escape sequence: '\u{110000}' (code point too large)">\u{110000}</error>" }"""
        myFixture.configureByText("test.maml", text.replace("\\u", "\\u"))
        myFixture.checkHighlighting()
    }

    fun testEmptyString() {
        myFixture.configureByText("test.maml", """{ key: "" }""")
        myFixture.checkHighlighting()
        assertNoErrors()
    }

    fun testStringWithOnlyEscapes() {
        myFixture.configureByText("test.maml", """{ key: "\n\t\r"}""")
        myFixture.checkHighlighting()
        assertNoErrors()
    }

    fun testConsecutiveBackslashes() {
        myFixture.configureByText("test.maml", """{ key: "\\\\\\\\"}""")
        myFixture.checkHighlighting()
        assertNoErrors()
    }

    fun testEscapedBackslashBeforeValidEscape() {
        myFixture.configureByText("test.maml", """{ key: "\\\\\\\\n"}""")
        myFixture.checkHighlighting()
        assertNoErrors()
    }

    // ========== Complex Mixed Scenarios ==========

    fun testComplexMixedString() {
        myFixture.configureByText(
            "test.maml", """
            {
              test: "Line 1\nLine 2\u{1F600}\tEmoji: \u{2764}\nBackslash: \\ Quote: \""
            }
        """.trimIndent()
        )
        myFixture.checkHighlighting()
        assertNoErrors()
    }


    // ========== Multiline String Escape Tests ==========

    fun testMultilineStringNoEscapes() {
        myFixture.configureByText("test.maml", "{ key: \"\"\"This is a simple multiline\"\"\" }")
        myFixture.checkHighlighting()
        assertNoErrors()
    }

    fun testMultilineStringEscapedTripleQuoteAtStart() {
        myFixture.configureByText("test.maml", "{ key: \"\"\"\\\"\"\"Starts with quotes\"\"\" }")
        myFixture.checkHighlighting()
        assertNoErrors()
    }

    fun testMultilineStringEscapedTripleQuoteInMiddle() {
        myFixture.configureByText("test.maml", "{ key: \"\"\"Some \\\"\"\" text\"\"\" }")
        myFixture.checkHighlighting()
        assertNoErrors()
    }

    fun testMultilineStringEscapedTripleQuoteAtEnd() {
        myFixture.configureByText("test.maml", "{ key: \"\"\"Text with \\\"\"\"\"\"\" }")
        myFixture.checkHighlighting()
        assertNoErrors()
    }

    fun testMultilineStringMultipleEscapedTripleQuotes() {
        myFixture.configureByText("test.maml", "{ key: \"\"\"First \\\"\"\" middle \\\"\"\" last\"\"\" }")
        myFixture.checkHighlighting()
        assertNoErrors()
    }

    fun testMultilineStringRegularBackslashNotHighlighted() {
        // Regular backslashes in multiline strings are literal, not escape sequences
        myFixture.configureByText("test.maml", "{ key: \"\"\"Path: C:\\Users\\Test\"\"\" }")
        myFixture.checkHighlighting()
        assertNoErrors()
    }

    fun testMultilineStringBackslashNNotHighlighted() {
        // \n in multiline strings is literal, not an escape
        myFixture.configureByText("test.maml", "{ key: \"\"\"Line 1\\nLine 2\"\"\" }")
        myFixture.checkHighlighting()
        assertNoErrors()
    }

    fun testMultilineStringPartialEscapeNotHighlighted() {
        // \", \"\" are not complete escape sequences in multiline strings
        myFixture.configureByText("test.maml", "{ key: \"\"\"Quote \\\" or \\\"\\\" two quotes\"\"\" }")
        myFixture.checkHighlighting()
        assertNoErrors()
    }

    fun testMultilineStringNewlines() {
        myFixture.configureByText(
            "test.maml",
            "{\n  key: \"\"\"\nLine 1\nLine 2\nLine 3\n\"\"\"\n}"
        )
        myFixture.checkHighlighting()
        assertNoErrors()
    }

    fun testMultilineStringWithEscapedQuotesAndNewlines() {
        myFixture.configureByText(
            "test.maml",
            "{\n  key: \"\"\"\nFirst line\nThen \\\"\"\"\nAnd another line\n\"\"\"\n}"
        )
        myFixture.checkHighlighting()
        assertNoErrors()
    }

    // ========== Helper Methods ==========

    private fun assertNoErrors() {
        val highlights = myFixture.doHighlighting(HighlightSeverity.ERROR)
        if (highlights.isNotEmpty()) {
            val errorMessages = highlights.joinToString("\n") {
                "Error at '${it.text}': ${it.description}"
            }
            fail("Expected no errors, but found:\n$errorMessages")
        }
    }
}