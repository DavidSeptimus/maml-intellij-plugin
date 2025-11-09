package com.davidseptimus.maml.formatter

import com.davidseptimus.maml.lang.MamlLanguage
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CodeStyleSettingsManager
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Comprehensive test suite for MAML formatter, organized by capability.
 *
 * Tests are grouped into the following categories:
 * 1. Spacing - colon spacing, comma spacing, brace/bracket spacing
 * 2. Indentation - object indentation, array indentation, nested structures
 * 3. Alignment - property alignment on colons and values
 * 4. Key Quoting - adding/removing quotes from keys
 * 5. Comma Handling - comma insertion/removal, trailing commas
 * 6. Comment Formatting - hash spacing, comment indentation
 * 7. Wrapping - object wrapping, array wrapping
 * 8. Edge Cases - empty structures, multiline strings, deep nesting
 */
class MamlFormatterTest : BasePlatformTestCase() {
    private lateinit var mamlSettings: MamlCodeStyleSettings
    private lateinit var commonSettings: CommonCodeStyleSettings
    private lateinit var settings: CodeStyleSettings
    override fun setUp() {
        super.setUp()
        settings = CodeStyleSettingsManager.getInstance(project).cloneSettings(
            CodeStyleSettingsManager.getInstance(project).currentSettings
        )
        commonSettings = settings.getCommonSettings(MamlLanguage)
        mamlSettings = settings.getCustomSettings(MamlCodeStyleSettings::class.java)
        // Set defaults explicitly to match MamlLanguageCodeStyleSettingsProvider
        commonSettings.indentOptions?.INDENT_SIZE = 2
        commonSettings.KEEP_BLANK_LINES_IN_CODE = 0
        CodeStyleSettingsManager.getInstance(project).setTemporarySettings(settings)
    }

    override fun tearDown() {
        try {
            CodeStyleSettingsManager.getInstance(project).dropTemporarySettings()
        } finally {
            super.tearDown()
        }
    }

    private fun doTest(input: String, expected: String) {
        val file = myFixture.configureByText("test.maml", input)
        WriteCommandAction.runWriteCommandAction(project) {
            CodeStyleManager.getInstance(project).reformat(file)
        }
        myFixture.checkResult(expected)
    }
    // ===========================================
    // SPACING TESTS
    // ===========================================

    fun testSpaceAfterColon() {
        mamlSettings.SPACE_AFTER_COLON = true
        doTest(
            "{ key:value }",
            """
            {
              key: value
            }
            """.trimIndent()
        )
    }

    fun testNoSpaceAfterColon() {
        mamlSettings.SPACE_AFTER_COLON = false
        doTest(
            "{ key: value }",
            """
            {
              key:value
            }
            """.trimIndent()
        )
    }

    fun testSpaceBeforeColon() {
        mamlSettings.SPACE_BEFORE_COLON = true
        mamlSettings.SPACE_AFTER_COLON = true
        doTest(
            "{ key:value }",
            """
            {
              key : value
            }
            """.trimIndent()
        )
    }

    fun testNoSpaceBeforeColon() {
        mamlSettings.SPACE_BEFORE_COLON = false
        mamlSettings.SPACE_AFTER_COLON = true
        doTest(
            "{ key : value }",
            """
            {
              key: value
            }
            """.trimIndent()
        )
    }

    fun testSpaceAfterComma() {
        mamlSettings.ARRAY_WRAPPING = CommonCodeStyleSettings.DO_NOT_WRAP
        commonSettings.SPACE_AFTER_COMMA = true
        doTest(
            "[1,2,3]",
            "[1, 2, 3]",
        )
    }

    fun testNoSpaceAfterComma() {
        mamlSettings.ARRAY_WRAPPING = CommonCodeStyleSettings.DO_NOT_WRAP
        commonSettings.SPACE_AFTER_COMMA = false
        doTest(
            "[1, 2, 3]",
            "[1,2,3]",
        )
    }

    fun testSpaceBeforeComma() {
        mamlSettings.ARRAY_WRAPPING = CommonCodeStyleSettings.DO_NOT_WRAP
        commonSettings.SPACE_BEFORE_COMMA = true
        doTest(
            "[1,2,3]",
            "[1 , 2 , 3]",
        )
    }

    fun testNoSpaceBeforeComma() {
        mamlSettings.ARRAY_WRAPPING = CommonCodeStyleSettings.DO_NOT_WRAP
        commonSettings.SPACE_BEFORE_COMMA = false
        doTest(
            "[1 , 2 , 3]",
            "[1, 2, 3]",
        )
    }

    fun testSpaceWithinBraces() {
        commonSettings.SPACE_WITHIN_BRACES = true
        doTest(
            "{key: value}",
            """
            {
              key: value
            }
            """.trimIndent()
        )
    }

    fun testNoSpaceWithinBraces() {
        commonSettings.SPACE_WITHIN_BRACES = false
        doTest(
            "{ key: value }",
            """
            {
              key: value
            }
            """.trimIndent()
        )
    }

    fun testSpaceWithinBrackets() {
        commonSettings.SPACE_WITHIN_BRACKETS = true
        mamlSettings.ARRAY_WRAPPING = CommonCodeStyleSettings.DO_NOT_WRAP
        doTest(
            "[1, 2, 3]",
            "[ 1, 2, 3 ]",
        )
    }

    fun testNoSpaceWithinBrackets() {
        commonSettings.SPACE_WITHIN_BRACKETS = false
        mamlSettings.ARRAY_WRAPPING = CommonCodeStyleSettings.DO_NOT_WRAP
        doTest(
            "[ 1, 2, 3 ]",
            "[1, 2, 3]",
        )
    }

    fun testComplexSpacingCombination() {
        mamlSettings.SPACE_BEFORE_COLON = false
        mamlSettings.SPACE_AFTER_COLON = true
        commonSettings.SPACE_AFTER_COMMA = true
        commonSettings.SPACE_BEFORE_COMMA = false
        commonSettings.SPACE_WITHIN_BRACES = true
        commonSettings.SPACE_WITHIN_BRACKETS = true
        mamlSettings.ARRAY_WRAPPING = CommonCodeStyleSettings.DO_NOT_WRAP
        mamlSettings.OBJECT_WRAPPING = CommonCodeStyleSettings.DO_NOT_WRAP

        doTest(
            "{name:\"John\",age:30,tags:[\"dev\",\"kotlin\"]}",
            "{ name: \"John\", age: 30, tags: [ \"dev\", \"kotlin\" ] }",
        )
    }
    // ===========================================
    // INDENTATION TESTS
    // ===========================================

    fun testObjectIndentation() {
        doTest(
            """
            {
            name: "test"
            value: 123
            }
            """.trimIndent(),
            """
            {
              name: "test"
              value: 123
            }
            """.trimIndent()
        )
    }

    fun testArrayIndentation() {
        doTest(
            """
            [
            1
            2
            3
            ]
            """.trimIndent(),
            """
            [
              1
              2
              3
            ]
            """.trimIndent()
        )
    }

    fun testNestedObjectIndentation() {
        doTest(
            """
            {
            outer: {
            inner: {
            deep: "value"
            }
            }
            }
            """.trimIndent(),
            """
            {
              outer: {
                inner: {
                  deep: "value"
                }
              }
            }
            """.trimIndent()
        )
    }

    fun testNestedArrayIndentation() {
        doTest(
            """
            [
            [
            1
            2
            ]
            [
            3
            4
            ]
            ]
            """.trimIndent(),
            """
            [
              [
                1
                2
              ]
              [
                3
                4
              ]
            ]
            """.trimIndent()
        )
    }

    fun testMixedNestedIndentation() {
        doTest(
            """
            {
            users: [
            {
            name: "Alice"
            age: 30
            }
            {
            name: "Bob"
            age: 25
            }
            ]
            }
            """.trimIndent(),
            """
            {
              users: [
                {
                  name: "Alice"
                  age: 30
                }
                {
                  name: "Bob"
                  age: 25
                }
              ]
            }
            """.trimIndent()
        )
    }

    fun testDeepNesting() {
        doTest(
            """
            {
            a: {
            b: {
            c: {
            d: {
            e: "deep"
            }
            }
            }
            }
            }
            """.trimIndent(),
            """
            {
              a: {
                b: {
                  c: {
                    d: {
                      e: "deep"
                    }
                  }
                }
              }
            }
            """.trimIndent()
        )
    }
    // ===========================================
    // ALIGNMENT TESTS
    // ===========================================

    fun testNoAlignment() {
        mamlSettings.PROPERTY_ALIGNMENT = MamlCodeStyleSettings.DO_NOT_ALIGN_PROPERTY
        doTest(
            """
            {
              a: 1
              longProperty: 2
              x: 3
            }
            """.trimIndent(),
            """
            {
              a: 1
              longProperty: 2
              x: 3
            }
            """.trimIndent()
        )
    }

    fun testAlignOnColon() {
        mamlSettings.PROPERTY_ALIGNMENT = MamlCodeStyleSettings.ALIGN_PROPERTY_ON_COLON
        doTest(
            """
            {
              a: 1
              longProperty: 2
              x: 3
            }
            """.trimIndent(),
            """
            {
              a           : 1
              longProperty: 2
              x           : 3
            }
            """.trimIndent()
        )
    }

    fun testAlignOnValue() {
        mamlSettings.PROPERTY_ALIGNMENT = MamlCodeStyleSettings.ALIGN_PROPERTY_ON_VALUE
        doTest(
            """
            {
              a: 1
              longProperty: 2
              x: 3
            }
            """.trimIndent(),
            """
            {
              a:            1
              longProperty: 2
              x:            3
            }
            """.trimIndent()
        )
    }

    fun testAlignOnColonNestedObjects() {
        mamlSettings.PROPERTY_ALIGNMENT = MamlCodeStyleSettings.ALIGN_PROPERTY_ON_COLON
        doTest(
            """
            {
              server: {
                host: "localhost"
                port: 8080
                enabled: true
              }
            }
            """.trimIndent(),
            """
            {
              server: {
                host   : "localhost"
                port   : 8080
                enabled: true
              }
            }
            """.trimIndent()
        )
    }

    fun testAlignOnValueNestedObjects() {
        mamlSettings.PROPERTY_ALIGNMENT = MamlCodeStyleSettings.ALIGN_PROPERTY_ON_VALUE
        doTest(
            """
            {
              server: {
                host: "localhost"
                port: 8080
                enabled: true
              }
            }
            """.trimIndent(),
            """
            {
              server: {
                        host:    "localhost"
                        port:    8080
                        enabled: true
                      }
            }
            """.trimIndent()
        )
    }

    fun testAlignmentWithObjectValues() {
        mamlSettings.PROPERTY_ALIGNMENT = MamlCodeStyleSettings.ALIGN_PROPERTY_ON_VALUE
        doTest(
            """
            {
              obj: { key: "value" }
              array: [1, 2, 3]
              string: "test"
            }
            """.trimIndent(),
            """
            {
              obj:    {
                        key: "value"
                      }
              array:  [
                        1, 2, 3
                      ]
              string: "test"
            }
            """.trimIndent()
        )
    }
    // ===========================================
    // KEY QUOTING TESTS
    // ===========================================

    fun testKeyQuotingDoNotModify() {
        mamlSettings.KEY_QUOTING_STYLE = MamlCodeStyleSettings.KeyQuotingStyle.DO_NOT_MODIFY.id
        doTest(
            "{ \"quoted\": 1, unquoted: 2 }",
            """
            {
              "quoted": 1, unquoted: 2
            }
            """.trimIndent()
        )
    }

    fun testKeyQuotingRemoveQuotes() {
        mamlSettings.KEY_QUOTING_STYLE = MamlCodeStyleSettings.KeyQuotingStyle.REMOVE_QUOTES.id
        doTest(
            "{ \"simple-key\": 1, \"hello_world\": 2 }",
            """
            {
              simple-key: 1, hello_world: 2
            }
            """.trimIndent()
        )
    }

    fun testKeyQuotingRemoveQuotesKeepsReservedWords() {
        mamlSettings.KEY_QUOTING_STYLE = MamlCodeStyleSettings.KeyQuotingStyle.REMOVE_QUOTES.id
        doTest(
            "{ \"true\": 1, \"false\": 2, \"null\": 3 }",
            """
            {
              "true": 1, "false": 2, "null": 3
            }
            """.trimIndent()
        )
    }

    fun testKeyQuotingRemoveQuotesKeepsSpecialChars() {
        mamlSettings.KEY_QUOTING_STYLE = MamlCodeStyleSettings.KeyQuotingStyle.REMOVE_QUOTES.id
        doTest(
            "{ \"with space\": 1, \"with.dot\": 2 }",
            """
            {
              "with space": 1, "with.dot": 2
            }
            """.trimIndent()
        )
    }

    fun testKeyQuotingAddQuotes() {
        mamlSettings.KEY_QUOTING_STYLE = MamlCodeStyleSettings.KeyQuotingStyle.ADD_QUOTES.id
        doTest(
            "{ simple-key: 1, hello_world: 2 }",
            """
            {
              "simple-key": 1, "hello_world": 2
            }
            """.trimIndent()
        )
    }

    fun testKeyQuotingAddQuotesNested() {
        mamlSettings.KEY_QUOTING_STYLE = MamlCodeStyleSettings.KeyQuotingStyle.ADD_QUOTES.id
        doTest(
            """
            {
              outer: {
                inner: {
                  key: "value"
                }
              }
            }
            """.trimIndent(),
            """
            {
              "outer": {
                "inner": {
                  "key": "value"
                }
              }
            }
            """.trimIndent()
        )
    }
    // ===========================================
    // COMMA HANDLING TESTS
    // ===========================================

    fun testCommaRemovalInArray() {
        mamlSettings.REMOVE_COMMAS = true
        doTest(
            "[1, 2, 3]",
            """
            [
              1
              2
              3
            ]
            """.trimIndent()
        )
    }

    fun testCommaRemovalInObject() {
        mamlSettings.REMOVE_COMMAS = true
        doTest(
            "{ a: 1, b: 2, c: 3 }",
            """
            {
              a: 1
              b: 2
              c: 3
            }
            """.trimIndent()
        )
    }

    fun testCommaRemovalNested() {
        mamlSettings.REMOVE_COMMAS = true
        doTest(
            """
            {
              users: [
                { id: 1, name: "Alice" },
                { id: 2, name: "Bob" }
              ]
            }
            """.trimIndent(),
            """
            {
              users: [
                {
                  id: 1
                  name: "Alice"
                }
                {
                  id: 2
                  name: "Bob"
                }
              ]
            }
            """.trimIndent()
        )
    }

    fun testTrailingCommaRemovalArray() {
        mamlSettings.KEEP_TRAILING_COMMA = false
        doTest(
            "[1, 2, 3,]",
            """
            [
              1, 2, 3
            ]
            """.trimIndent()
        )
    }

    fun testTrailingCommaRemovalObject() {
        mamlSettings.KEEP_TRAILING_COMMA = false
        doTest(
            "{ a: 1, b: 2, }",
            """
            {
              a: 1, b: 2
            }
            """.trimIndent()
        )
    }

    fun testTrailingCommaKeep() {
        mamlSettings.KEEP_TRAILING_COMMA = true
        doTest(
            "[1, 2, 3,]",
            """
            [
              1, 2, 3,
            ]
            """.trimIndent()
        )
    }

    fun testTrailingCommaKeepObject() {
        mamlSettings.KEEP_TRAILING_COMMA = true
        doTest(
            "{ a: 1, b: 2, }",
            """
            {
              a: 1, b: 2,
            }
            """.trimIndent()
        )
    }
    // ===========================================
    // COMMENT FORMATTING TESTS
    // ===========================================

    fun testCommentHashSpacingAtLeastOne() {
        mamlSettings.SPACE_AFTER_COMMENT_HASH = CommentHashSpaceOptions.AT_LEAST_ONE.id
        doTest(
            """
            {
              #No space
              #  Two spaces
              # One space
              key: "value"
            }
            """.trimIndent(),
            """
            {
              # No space
              #  Two spaces
              # One space
              key: "value"
            }
            """.trimIndent()
        )
    }

    fun testCommentHashSpacingExactlyOne() {
        mamlSettings.SPACE_AFTER_COMMENT_HASH = CommentHashSpaceOptions.EXACTLY_ONE.id
        doTest(
            """
            {
              #No space
              #  Two spaces
              # One space
              key: "value"
            }
            """.trimIndent(),
            """
            {
              # No space
              # Two spaces
              # One space
              key: "value"
            }
            """.trimIndent()
        )
    }

    fun testCommentHashSpacingAny() {
        mamlSettings.SPACE_AFTER_COMMENT_HASH = CommentHashSpaceOptions.ANY.id
        doTest(
            """
            {
              #No space
              #  Two spaces
              key: "value"
            }
            """.trimIndent(),
            """
            {
              #No space
              #  Two spaces
              key: "value"
            }
            """.trimIndent()
        )
    }

    fun testCommentIndentation() {
        doTest(
            """
            {
                # Badly indented comment
              key: "value"
              nested: {
                    # Another badly indented comment
                inner: 123
              }
            }
            """.trimIndent(),
            """
            {
              # Badly indented comment
              key: "value"
              nested: {
                # Another badly indented comment
                inner: 123
              }
            }
            """.trimIndent()
        )
    }
    // ===========================================
    // WRAPPING TESTS
    // ===========================================

    fun testObjectWrapAlways() {
        mamlSettings.OBJECT_WRAPPING = CommonCodeStyleSettings.WRAP_ALWAYS
        doTest(
            "{ a: 1, b: 2, c: 3 }",
            """
            {
              a: 1, b: 2, c: 3
            }
            """.trimIndent()
        )
    }

    fun testArrayWrapAlways() {
        mamlSettings.ARRAY_WRAPPING = CommonCodeStyleSettings.WRAP_ALWAYS
        doTest(
            "[1, 2, 3]",
            """
            [
              1, 2, 3
            ]
            """.trimIndent()
        )
    }

    fun testObjectWrapNone() {
        mamlSettings.OBJECT_WRAPPING = CommonCodeStyleSettings.DO_NOT_WRAP
        doTest(
            "{a: 1, b: 2}",
            "{a: 1, b: 2}"
        )
    }

    fun testArrayWrapNone() {
        mamlSettings.ARRAY_WRAPPING = CommonCodeStyleSettings.DO_NOT_WRAP
        doTest(
            "[1, 2]",
            "[1, 2]"
        )
    }

    fun testInlineCommentsNotMovedWithWrapping() {
        mamlSettings.OBJECT_WRAPPING = CommonCodeStyleSettings.WRAP_ALWAYS
        mamlSettings.ARRAY_WRAPPING = CommonCodeStyleSettings.WRAP_ALWAYS
        doTest(
            """
            {
            # Line comment should be indented
              key: "value" # inline after value
              number: 42 # inline after number
              flag: true # inline after boolean

              # Another line comment
              nested: { # inline after opening brace
                inner: "data" # inline after nested value
                count: 10
              } # inline after closing brace

              items: [ # inline after opening bracket
                1, 2, 3 # inline after array items
              ] # inline after closing bracket

              list: [
                # Line comment in array
                "first"
                "second" # inline in array
              ]

              obj: {
                # Line comment in object
                a: 1, # inline after comma
                b: 2 # inline without comma
              }
            }
            """.trimIndent(),
            """
            {
              # Line comment should be indented
              key: "value" # inline after value
              number: 42 # inline after number
              flag: true # inline after boolean

              # Another line comment
              nested: { # inline after opening brace
                inner: "data" # inline after nested value
                count: 10
              } # inline after closing brace

              items: [ # inline after opening bracket
                1, 2, 3 # inline after array items
              ] # inline after closing bracket

              list: [
                # Line comment in array
                "first"
                "second" # inline in array
              ]

              obj: {
                # Line comment in object
                a: 1, # inline after comma
                b: 2 # inline without comma
              }
            }
            """.trimIndent()
        )
    }
    // ===========================================
    // EDGE CASE TESTS
    // ===========================================

    fun testEmptyObject() {
        mamlSettings.OBJECT_WRAPPING = CommonCodeStyleSettings.DO_NOT_WRAP
        doTest("{}", "{}")
        mamlSettings.OBJECT_WRAPPING = CommonCodeStyleSettings.WRAP_ALWAYS
        doTest("{}", "{\n}")
    }

    fun testEmptyArray() {
        mamlSettings.ARRAY_WRAPPING = CommonCodeStyleSettings.DO_NOT_WRAP
        doTest("[]", "[]")
        mamlSettings.ARRAY_WRAPPING = CommonCodeStyleSettings.WRAP_ALWAYS
        doTest("[]", "[\n]")
    }

    fun testEmptyObjectWithSpacing() {
        mamlSettings.OBJECT_WRAPPING = CommonCodeStyleSettings.DO_NOT_WRAP
        commonSettings.SPACE_WITHIN_BRACES = true
        doTest("{}", "{ }")
    }

    fun testEmptyArrayWithSpacing() {
        mamlSettings.ARRAY_WRAPPING = CommonCodeStyleSettings.DO_NOT_WRAP
        commonSettings.SPACE_WITHIN_BRACKETS = true
        doTest("[]", "[ ]")
    }

    fun testMultilineString() {
        doTest(
            """
            {
              description: ""${'"'}
                This is a
                multiline string
              ""${'"'}
            }
            """.trimIndent(),
            """
            {
              description: ""${'"'}
                This is a
                multiline string
              ""${'"'}
            }
            """.trimIndent()
        )
    }

    fun testMultilineStringWithBadIndentation() {
        // The content inside multiline strings should be preserved exactly,
        // even if it has "bad" indentation
        doTest(
            """
            {
              text: ""${'"'}
            badly indented
              content here
                  more content
              ""${'"'}
            }
            """.trimIndent(),
            """
            {
              text: ""${'"'}
            badly indented
              content here
                  more content
              ""${'"'}
            }
            """.trimIndent()
        )
    }

    fun testMultilineStringWithVariousWhitespace() {
        // Tabs, spaces, and newlines inside multiline strings should be preserved
        doTest(
            """
            {
              code: ""${'"'}
            	if (x) {
            		return true
            	}
              ""${'"'}
            }
            """.trimIndent(),
            """
            {
              code: ""${'"'}
            	if (x) {
            		return true
            	}
              ""${'"'}
            }
            """.trimIndent()
        )
    }

    fun testMultilineStringInArray() {
        // raw strings inside arrays have their opening line indentation removed -- TBD how to indent only the first line, while preserving the rest
        doTest(
            """
            [
              ""${'"'}
              First multiline
                with indent
              ""${'"'}
              ""${'"'}
            Second multiline
              different indent
              ""${'"'}
            ]
            """.trimIndent(),
            """
            [
            ""${'"'}
              First multiline
                with indent
              ""${'"'}
            ""${'"'}
            Second multiline
              different indent
              ""${'"'}
            ]
            """.trimIndent()
        )
    }

    fun testMultilineStringInNestedObject() {
        doTest(
            """
            {
              outer: {
                inner: {
                  doc: ""${'"'}
                This has specific
                  indentation that
                    must be preserved
              ""${'"'}
                }
              }
            }
            """.trimIndent(),
            """
            {
              outer: {
                inner: {
                  doc: ""${'"'}
                This has specific
                  indentation that
                    must be preserved
              ""${'"'}
                }
              }
            }
            """.trimIndent()
        )
    }

    fun testMultipleMultilineStringsWithDifferentIndentation() {
        doTest(
            """
            {
              poem1: ""${'"'}
            Roses are red
              Violets are blue
              ""${'"'}
              poem2: ""${'"'}
                  This one is indented
                    even more
              ""${'"'}
            }
            """.trimIndent(),
            """
            {
              poem1: ""${'"'}
            Roses are red
              Violets are blue
              ""${'"'}
              poem2: ""${'"'}
                  This one is indented
                    even more
              ""${'"'}
            }
            """.trimIndent()
        )
    }

    fun testMultilineStringOpeningLineWithContent() {
        // Only the line with opening """ can be moved, but content on that line stays with it
        doTest(
            """
            {
              greeting: ""${'"'}Hello World
                More text here
              ""${'"'}
            }
            """.trimIndent(),
            """
            {
              greeting: ""${'"'}Hello World
                More text here
              ""${'"'}
            }
            """.trimIndent()
        )
    }

    fun testMultilineStringWithEmptyLines() {
        doTest(
            """
            {
              text: ""${'"'}
            Line 1

            Line 3 (line 2 was empty)
              ""${'"'}
            }
            """.trimIndent(),
            """
            {
              text: ""${'"'}
            Line 1

            Line 3 (line 2 was empty)
              ""${'"'}
            }
            """.trimIndent()
        )
    }

    fun testMultilineStringWithTrailingSpaces() {
        // Trailing spaces inside multiline strings should be preserved
        val input = "{\n  text: \"\"\"\nLine with trailing spaces   \n    Another line  \n  \"\"\"\n}"
        val expected = "{\n  text: \"\"\"\nLine with trailing spaces   \n    Another line  \n  \"\"\"\n}"
        doTest(input, expected)
    }

    fun testWrappingWithoutCommasResultsInOneItemPerLine() {
        mamlSettings.SPACE_AFTER_COLON = true
        mamlSettings.SPACE_BEFORE_COLON = false
        commonSettings.SPACE_AFTER_COMMA = true
        mamlSettings.PROPERTY_ALIGNMENT = MamlCodeStyleSettings.DO_NOT_ALIGN_PROPERTY
        mamlSettings.ARRAY_WRAPPING = CommonCodeStyleSettings.WRAP_ALWAYS
        mamlSettings.OBJECT_WRAPPING = CommonCodeStyleSettings.WRAP_ALWAYS
        mamlSettings.REMOVE_COMMAS = true
        doTest(
            """
            {name:"John Doe",age:30,address:{street:"123 Main St",city:"Springfield"},tags:["developer","kotlin"]}
            """.trimIndent(),
            """
            {
              name: "John Doe"
              age: 30
              address: {
                street: "123 Main St"
                city: "Springfield"
              }
              tags: [
                "developer"
                "kotlin"
              ]
            }
            """.trimIndent()
        )
    }

    fun testWrappingWithoutRemovingCommasDoesntResultInOneItemPerLine() {
        mamlSettings.SPACE_AFTER_COLON = true
        mamlSettings.SPACE_BEFORE_COLON = false
        commonSettings.SPACE_AFTER_COMMA = true
        mamlSettings.PROPERTY_ALIGNMENT = MamlCodeStyleSettings.DO_NOT_ALIGN_PROPERTY
        mamlSettings.ARRAY_WRAPPING = CommonCodeStyleSettings.WRAP_ALWAYS
        mamlSettings.OBJECT_WRAPPING = CommonCodeStyleSettings.WRAP_ALWAYS
        mamlSettings.REMOVE_COMMAS = false
        doTest(
            """
            {name:"John Doe",age:30,address:{street:"123 Main St",city:"Springfield"},tags:["developer","kotlin"]}
            """.trimIndent(),
            """
            {
              name: "John Doe", age: 30, address: {
                street: "123 Main St", city: "Springfield"
              }, tags: [
                "developer", "kotlin"
              ]
            }
            """.trimIndent()
        )
    }

    fun testMixedQuotingAndSpacing() {
        mamlSettings.KEY_QUOTING_STYLE = MamlCodeStyleSettings.KeyQuotingStyle.REMOVE_QUOTES.id
        mamlSettings.SPACE_AFTER_COLON = true
        commonSettings.SPACE_WITHIN_BRACES = true
        doTest(
            "{\"simple-key\":\"value\",\"another\":123}",
            """
            {
              simple-key: "value", another: 123
            }
            """.trimIndent()
        )
    }

    fun testArrayWrappingClosingBraceIndentation() {
        mamlSettings.ARRAY_WRAPPING = CommonCodeStyleSettings.WRAP_ALWAYS
        mamlSettings.OBJECT_WRAPPING = CommonCodeStyleSettings.WRAP_ALWAYS
        mamlSettings.KEY_QUOTING_STYLE = MamlCodeStyleSettings.KeyQuotingStyle.REMOVE_QUOTES.id
        mamlSettings.SPACE_AFTER_COLON = true

        doTest(
            "{a: \"b\", b: [1, 2, 3, {a: 1, c: 2}, [1,2,3] ]}",
            """
            {
              a: "b", b: [
                1, 2, 3, {
                  a: 1, c: 2
                }, [
                  1, 2, 3
                ]
              ]
            }
            """.trimIndent()
        )
    }
}