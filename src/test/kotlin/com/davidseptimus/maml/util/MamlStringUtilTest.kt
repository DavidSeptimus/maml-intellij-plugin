package com.davidseptimus.maml.util

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Test suite for MamlStringUtil validating string conversions between
 * quoted and raw (multiline) string formats.
 */
class MamlStringUtilTest {

    // ========== quotedToMultilineContent Tests ==========

    @Test
    fun testQuotedToMultilineContent_EmptyString() {
        val result = MamlStringUtil.quotedToMultilineContent("\"\"")
        assertEquals("", result)
    }

    @Test
    fun testQuotedToMultilineContent_SimpleText() {
        val result = MamlStringUtil.quotedToMultilineContent("\"hello\"")
        assertEquals("hello", result)
    }

    @Test
    fun testQuotedToMultilineContent_WithNewline() {
        val result = MamlStringUtil.quotedToMultilineContent("\"line1\\nline2\"")
        assertEquals("line1\nline2", result)
    }

    @Test
    fun testQuotedToMultilineContent_WithTab() {
        val result = MamlStringUtil.quotedToMultilineContent("\"hello\\tworld\"")
        assertEquals("hello\tworld", result)
    }

    @Test
    fun testQuotedToMultilineContent_WithEscapedQuote() {
        val result = MamlStringUtil.quotedToMultilineContent("\"say \\\"hello\\\"\"")
        assertEquals("say \"hello\"", result)
    }

    @Test
    fun testQuotedToMultilineContent_WithEscapedBackslash() {
        val result = MamlStringUtil.quotedToMultilineContent("\"path\\\\to\\\\file\"")
        assertEquals("path\\to\\file", result)
    }

    // ========== multilineToQuotedContent Tests ==========

    @Test
    fun testMultilineToQuotedContent_EmptyWithNewline() {
        // Per spec: """\n""" evaluates to empty string (leading newline is stripped)
        val result = MamlStringUtil.multilineToQuotedContent("\"\"\"\n\"\"\"")
        assertEquals("", result)
    }

    @Test
    fun testMultilineToQuotedContent_SimpleText() {
        val result = MamlStringUtil.multilineToQuotedContent("\"\"\"hello\"\"\"")
        assertEquals("hello", result)
    }

    @Test
    fun testMultilineToQuotedContent_WithNewline() {
        val result = MamlStringUtil.multilineToQuotedContent("\"\"\"line1\nline2\"\"\"")
        assertEquals("line1\\nline2", result)
    }

    @Test
    fun testMultilineToQuotedContent_WithTab() {
        val result = MamlStringUtil.multilineToQuotedContent("\"\"\"hello\tworld\"\"\"")
        assertEquals("hello\\tworld", result)
    }

    @Test
    fun testMultilineToQuotedContent_WithQuote() {
        val result = MamlStringUtil.multilineToQuotedContent("\"\"\"say \"hello\"\"\"\"")
        assertEquals("say \\\"hello\\\"", result)
    }

    @Test
    fun testMultilineToQuotedContent_WithBackslash() {
        val result = MamlStringUtil.multilineToQuotedContent("\"\"\"path\\to\\file\"\"\"")
        assertEquals("path\\\\to\\\\file", result)
    }

    @Test
    fun testMultilineToQuotedContent_WithLeadingNewlineStripped() {
        // Per spec: """\n<content>""" -> leading newline is stripped
        val result = MamlStringUtil.multilineToQuotedContent("\"\"\"\nHello World\"\"\"")
        assertEquals("Hello World", result)
    }

    @Test
    fun testMultilineToQuotedContent_TwoNewlines() {
        // Per spec: """\n\n""" -> leading newline stripped, one newline remains
        val result = MamlStringUtil.multilineToQuotedContent("\"\"\"\n\n\"\"\"")
        assertEquals("\\n", result)
    }

    @Test
    fun testMultilineToQuotedContent_NoLeadingNewline() {
        // If there's no leading newline, nothing is stripped
        val result = MamlStringUtil.multilineToQuotedContent("\"\"\"Hello World\"\"\"")
        assertEquals("Hello World", result)
    }

    // ========== wrapInMultilineQuotes Tests ==========

    @Test
    fun testWrapInMultilineQuotes_EmptyString() {
        // Per spec: empty string must become """\n"""
        val result = MamlStringUtil.wrapInMultilineQuotes("")
        assertEquals("\"\"\"\n\"\"\"", result)
    }

    @Test
    fun testWrapInMultilineQuotes_SimpleText() {
        val result = MamlStringUtil.wrapInMultilineQuotes("hello")
        assertEquals("\"\"\"hello\"\"\"", result)
    }

    @Test
    fun testWrapInMultilineQuotes_TextWithSpaces() {
        val result = MamlStringUtil.wrapInMultilineQuotes("hello world")
        assertEquals("\"\"\"hello world\"\"\"", result)
    }

    @Test
    fun testWrapInMultilineQuotes_TextWithNewline() {
        val result = MamlStringUtil.wrapInMultilineQuotes("line1\nline2")
        assertEquals("\"\"\"\nline1\nline2\"\"\"", result)
    }

    @Test
    fun testWrapInMultilineQuotes_SingleNewline() {
        // Per spec: string with one newline becomes """\n\n"""
        val result = MamlStringUtil.wrapInMultilineQuotes("\n")
        assertEquals("\"\"\"\n\n\"\"\"", result)
    }

    // ========== wrapInQuotes Tests ==========

    @Test
    fun testWrapInQuotes_EmptyString() {
        val result = MamlStringUtil.wrapInQuotes("")
        assertEquals("\"\"", result)
    }

    @Test
    fun testWrapInQuotes_SimpleText() {
        val result = MamlStringUtil.wrapInQuotes("hello")
        assertEquals("\"hello\"", result)
    }

    @Test
    fun testWrapInQuotes_EscapedContent() {
        val result = MamlStringUtil.wrapInQuotes("line1\\nline2")
        assertEquals("\"line1\\nline2\"", result)
    }

    // ========== Round-trip Conversion Tests ==========

    @Test
    fun testRoundTrip_EmptyString() {
        // "" -> """\n""" -> "" (leading newline is stripped during conversion back)
        val quotedContent = MamlStringUtil.quotedToMultilineContent("\"\"")
        val wrapped = MamlStringUtil.wrapInMultilineQuotes(quotedContent)
        assertEquals("\"\"\"\n\"\"\"", wrapped)

        val backToQuoted = MamlStringUtil.multilineToQuotedContent(wrapped)
        val finalWrapped = MamlStringUtil.wrapInQuotes(backToQuoted)
        assertEquals("\"\"", finalWrapped)
    }

    @Test
    fun testRoundTrip_SimpleText() {
        // "hello" -> """hello""" -> "hello"
        val quotedContent = MamlStringUtil.quotedToMultilineContent("\"hello\"")
        val wrapped = MamlStringUtil.wrapInMultilineQuotes(quotedContent)
        assertEquals("\"\"\"hello\"\"\"", wrapped)

        val backToQuoted = MamlStringUtil.multilineToQuotedContent(wrapped)
        val finalWrapped = MamlStringUtil.wrapInQuotes(backToQuoted)
        assertEquals("\"hello\"", finalWrapped)
    }

    @Test
    fun testRoundTrip_TextWithNewline() {
        // "line1\nline2" -> """\nline1\nline2""" -> "line1\nline2" (leading newline stripped)
        val quotedContent = MamlStringUtil.quotedToMultilineContent("\"line1\\nline2\"")
        val wrapped = MamlStringUtil.wrapInMultilineQuotes(quotedContent)
        assertEquals("\"\"\"\nline1\nline2\"\"\"", wrapped)

        val backToQuoted = MamlStringUtil.multilineToQuotedContent(wrapped)
        val finalWrapped = MamlStringUtil.wrapInQuotes(backToQuoted)
        assertEquals("\"line1\\nline2\"", finalWrapped)
    }
}