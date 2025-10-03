package com.davidseptimus.maml.completion

import com.davidseptimus.maml.lang.MamlParserDefinition
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MamlKeywordCompletionTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String = "src/test/testData"

    fun testKeywordCompletionInValuePosition() {
        myFixture.configureByText("test.maml", "{ key: <caret> }")
        myFixture.completeBasic()

        val lookupStrings = myFixture.lookupElementStrings
        assertNotNull("Expected completion suggestions", lookupStrings)
        assertTrue("Expected 'true' in completions", lookupStrings!!.contains("true"))
        assertTrue("Expected 'false' in completions", lookupStrings.contains("false"))
        assertTrue("Expected 'null' in completions", lookupStrings.contains("null"))
    }

    fun testKeywordCompletionForPartialKeyword() {
        myFixture.configureByText("test.maml", "{ key: f<caret> }")
        val results = myFixture.completeBasic()

        // When there's a single match starting with 'f', it auto-completes
        // So we should check that 'false' was suggested (either in lookup or auto-completed)
        if (results == null) {
            // Auto-completed - check the resulting text contains 'false'
            assertTrue("Expected 'false' to be auto-completed", myFixture.editor.document.text.contains("false"))
        } else {
            // Multiple matches - check lookup contains 'false'
            val lookupStrings = myFixture.lookupElementStrings
            assertNotNull("Expected completion suggestions", lookupStrings)
            assertTrue("Expected 'false' in completions", lookupStrings!!.contains("false"))
        }
    }

    fun testKeywordCompletionInArray() {
        myFixture.configureByText("test.maml", "[<caret>]")
        myFixture.completeBasic()

        val lookupStrings = myFixture.lookupElementStrings
        assertNotNull("Expected completion suggestions", lookupStrings)
        assertTrue("Expected 'true' in completions", lookupStrings!!.contains("true"))
        assertTrue("Expected 'false' in completions", lookupStrings.contains("false"))
        assertTrue("Expected 'null' in completions", lookupStrings.contains("null"))
    }

    fun testNoKeywordCompletionInString() {
        myFixture.configureByText("test.maml", "{ key: \"<caret>\" }")
        myFixture.completeBasic()

        val lookupStrings = myFixture.lookupElementStrings
        // Inside strings, we shouldn't get keyword completions
        if (lookupStrings != null) {
            assertFalse("Should not suggest 'true' inside string", lookupStrings.contains("true"))
            assertFalse("Should not suggest 'false' inside string", lookupStrings.contains("false"))
            assertFalse("Should not suggest 'null' inside string", lookupStrings.contains("null"))
        }
    }

    fun testNoKeywordCompletionInKey() {
        myFixture.configureByText("test.maml", "{ <caret>: \"value\" }")
        myFixture.completeBasic()

        val lookupStrings = myFixture.lookupElementStrings
        // In key positions, we shouldn't get keyword completions
        if (lookupStrings != null) {
            assertFalse("Should not suggest 'true' in key position", lookupStrings.contains("true"))
            assertFalse("Should not suggest 'false' in key position", lookupStrings.contains("false"))
            assertFalse("Should not suggest 'null' in key position", lookupStrings.contains("null"))
        }
    }
}