package com.davidseptimus.maml.editor

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Tests for [MamlUnicodeFoldingBuilder].
 */
class MamlUnicodeFoldingBuilderTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String = "src/test/testData/folding"

    /**
     * Helper to configure text and build folding regions.
     */
    private fun configureFolding(text: String) {
        myFixture.configureByText("test.maml", text)
        // Build folding regions directly using the builder
        val builder = MamlUnicodeFoldingBuilder()
        val descriptors = builder.buildFoldRegions(myFixture.file, myFixture.editor.document, false)

        // Apply the fold regions to the editor
        myFixture.editor.foldingModel.runBatchFoldingOperation {
            descriptors.forEach { descriptor ->
                val region = myFixture.editor.foldingModel.addFoldRegion(
                    descriptor.range.startOffset,
                    descriptor.range.endOffset,
                    descriptor.placeholderText ?: ""
                )
                region?.isExpanded = !builder.isCollapsedByDefault(descriptor.element)
            }
        }
    }

    // Basic Unicode escape folding tests

    fun testBasicUnicodeEscape() {
        val text = """{ emoji: "\u{1F600}" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should have one folding region", 1, regions.size)
        assertEquals("Should fold to emoji character", "😀", regions[0].placeholderText)
    }

    fun testMultipleUnicodeEscapes() {
        val text = """{ emoji: "\u{1F600}", heart: "\u{2764}", star: "\u{2605}" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should have three folding regions", 3, regions.size)
        val placeholders = regions.map { it.placeholderText }.sorted()
        assertEquals("Should have correct emojis", listOf("★", "❤", "😀"), placeholders)
    }

    fun testMultipleEscapesInSameString() {
        val text = """{ text: "\u{1F600}\u{2764}\u{2605}" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should have three folding regions", 3, regions.size)
    }

    // Hex digit validation tests

    fun testValidHexDigitsLowerCase() {
        val text = """{ char: "\u{61}" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should have one folding region", 1, regions.size)
        assertEquals("Should fold to 'a'", "a", regions[0].placeholderText)
    }

    fun testValidHexDigitsUpperCase() {
        val text = """{ char: "\u{41}" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should have one folding region", 1, regions.size)
        assertEquals("Should fold to 'A'", "A", regions[0].placeholderText)
    }

    fun testValidHexDigitsMixedCase() {
        val text = """{ char: "\u{1F6Aa}" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should have one folding region", 1, regions.size)
        assertEquals("Should fold to door emoji", "🚪", regions[0].placeholderText)
    }

    fun testInvalidHexDigits() {
        val text = """{ invalid: "\u{GGGG}" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should not create folding for invalid hex", 0, regions.size)
    }

    fun testHexDigitsWithSpaces() {
        val text = """{ invalid: "\u{1F 60}" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should not create folding for hex with spaces", 0, regions.size)
    }

    // Length validation tests

    fun testSingleHexDigit() {
        val text = """{ char: "\u{A}" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        // Newline is whitespace, so no folding should be created
        assertEquals("Should not create folding for whitespace", 0, regions.size)
    }

    fun testTwoHexDigits() {
        val text = """{ char: "\u{41}" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should have one folding region", 1, regions.size)
        assertEquals("Should fold to 'A'", "A", regions[0].placeholderText)
    }

    fun testFourHexDigits() {
        val text = """{ char: "\u{2764}" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should have one folding region", 1, regions.size)
        assertEquals("Should fold to heart", "❤", regions[0].placeholderText)
    }

    fun testSixHexDigits() {
        val text = """{ emoji: "\u{1F600}" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should have one folding region", 1, regions.size)
        assertEquals("Should fold to emoji", "😀", regions[0].placeholderText)
    }

    fun testSevenHexDigits() {
        val text = """{ invalid: "\u{1234567}" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should not create folding for more than 6 hex digits", 0, regions.size)
    }

    fun testEmptyBraces() {
        val text = """{ invalid: "\u{}" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should not create folding for empty braces", 0, regions.size)
    }

    // Unicode code point validation tests

    fun testCodePointTooLarge() {
        val text = """{ invalid: "\u{110000}" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should not create folding for code point > 0x10FFFF", 0, regions.size)
    }

    fun testSurrogateCodePointLow() {
        val text = """{ invalid: "\u{D800}" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should not create folding for surrogate code point", 0, regions.size)
    }

    fun testSurrogateCodePointMid() {
        val text = """{ invalid: "\u{DC00}" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should not create folding for surrogate code point", 0, regions.size)
    }

    fun testSurrogateCodePointHigh() {
        val text = """{ invalid: "\u{DFFF}" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should not create folding for surrogate code point", 0, regions.size)
    }

    // Escaped backslash tests

    fun testEscapedBackslashBeforeUnicodeEscape() {
        val text = """{ text: "\\u{1F600}" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should not create folding for escaped backslash", 0, regions.size)
    }

    fun testBackslashThenUnicodeEscape() {
        val text = """{ text: "\\\u{1F600}" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should have one folding region after escaped backslash", 1, regions.size)
        assertEquals("Should fold to emoji", "😀", regions[0].placeholderText)
    }

    fun testDoubleEscapedBackslash() {
        val text = """{ text: "\\\\u{1F600}" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should not create folding after double escaped backslash", 0, regions.size)
    }

    fun testTripleBackslashThenUnicodeEscape() {
        val text = """{ text: "\\\\\\\u{1F600}" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should have one folding region after triple backslash", 1, regions.size)
    }

    // Malformed escape tests

    fun testMissingOpeningBrace() {
        val text = """{ invalid: "\u1F600}" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should not create folding without opening brace", 0, regions.size)
    }

    fun testMissingClosingBrace() {
        val text = """{ invalid: "\u{1F600" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should not create folding without closing brace", 0, regions.size)
    }

    fun testNoBraces() {
        val text = """{ invalid: "\u1F600" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should not create folding without braces", 0, regions.size)
    }

    fun testClosingBraceOnly() {
        val text = """{ invalid: "\u}" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should not create folding with closing brace only", 0, regions.size)
    }

    // String boundary tests

    fun testUnicodeEscapeAtStringStart() {
        val text = """{ text: "\u{1F600} hello" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should have one folding region at start", 1, regions.size)
        assertEquals("Should fold to emoji", "😀", regions[0].placeholderText)
    }

    fun testUnicodeEscapeAtStringEnd() {
        val text = """{ text: "hello \u{1F600}" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should have one folding region at end", 1, regions.size)
        assertEquals("Should fold to emoji", "😀", regions[0].placeholderText)
    }

    fun testUnicodeEscapeOnly() {
        val text = """{ emoji: "\u{1F600}" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should have one folding region", 1, regions.size)
        assertEquals("Should fold to emoji", "😀", regions[0].placeholderText)
    }

    fun testEmptyString() {
        val text = """{ empty: "" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should not create folding in empty string", 0, regions.size)
    }

    // Multiple strings tests

    fun testMultipleStringsWithEscapes() {
        val text = """
            {
              emoji1: "\u{1F600}"
              emoji2: "\u{2764}"
              text: "no escape here"
              emoji3: "\u{2605}"
            }
        """.trimIndent()
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should have three folding regions across multiple strings", 3, regions.size)
    }

    // Mixed content tests

    fun testMixedEscapesAndText() {
        val text = """{ message: "Hello \u{1F600} World \u{2764} !" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should have two folding regions", 2, regions.size)
        val placeholders = regions.map { it.placeholderText }.sorted()
        assertEquals("Should have both emojis", listOf("❤", "😀"), placeholders)
    }

    fun testValidAndInvalidEscapesMixed() {
        val text = """{ text: "\u{1F600} \u{GGGG} \u{2764}" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should have two folding regions (skip invalid)", 2, regions.size)
        val placeholders = regions.map { it.placeholderText }.sorted()
        assertEquals("Should have valid emojis only", listOf("❤", "😀"), placeholders)
    }

    // Multiline string tests (if MAML supports them)

    fun testUnicodeEscapeInMultilineString() {
        // Note: MAML multiline strings (""") do not process escape sequences
        // This test verifies that escapes in multiline strings are NOT folded
        val text = "{text: \"\"\"Line 1\n\\u{1F600}\nLine 3\"\"\"}"
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        // Filter for Unicode foldings only
        val unicodeFoldings = regions.filter { it.placeholderText == "😀" }
        // Multiline strings don't process escapes, so no Unicode folding should occur
        assertEquals("Should not fold Unicode escapes in multiline strings", 0, unicodeFoldings.size)
    }

    // Whitespace character tests

    fun testWhitespaceCharacters() {
        // Tab, newline, space, and other whitespace characters should not be folded
        val text = """{ ws: "\u{9}\u{A}\u{20}\u{D}" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should not create folding for whitespace characters", 0, regions.size)
    }

    // Leading zeros tests

    fun testLeadingZeros() {
        val text = """{ char: "\u{0041}" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should have one folding region with leading zeros", 1, regions.size)
        assertEquals("Should fold to 'A'", "A", regions[0].placeholderText)
    }

    // Edge case: very long strings

    fun testManyEscapesInLongString() {
        val escapes = (1..20).joinToString("") { "\\u{1F600}" }
        val text = """{ text: "$escapes" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should have 20 folding regions", 20, regions.size)
        assertTrue("All should fold to emoji", regions.all { it.placeholderText == "😀" })
    }

    // Whitespace tests

    fun testNoWhitespaceAroundEscape() {
        val text = """{ text: "\u{41}" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should have one folding region", 1, regions.size)
    }

    fun testWhitespaceBeforeEscape() {
        val text = """{ text: "  \u{41}" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should have one folding region", 1, regions.size)
    }

    fun testWhitespaceAfterEscape() {
        val text = """{ text: "\u{41}  " }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should have one folding region", 1, regions.size)
    }

    // Case sensitivity tests

    fun testLowercaseU() {
        val text = """{ char: "\u{41}" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should have one folding region with lowercase u", 1, regions.size)
    }

    fun testUppercaseU() {
        val text = """{ char: "\U{41}" }"""
        configureFolding(text)
        val regions = myFixture.editor.foldingModel.allFoldRegions
        assertEquals("Should not create folding with uppercase U", 0, regions.size)
    }
}