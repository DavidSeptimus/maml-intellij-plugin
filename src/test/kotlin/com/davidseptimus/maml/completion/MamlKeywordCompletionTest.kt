package com.davidseptimus.maml.completion

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MamlKeywordCompletionTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String = "src/test/testData/completion/keywords"

    fun testKeywordCompletionInValuePosition() {
        myFixture.testCompletionVariants("keywordCompletionInValuePosition.maml", "true", "false", "null")
    }

    fun testKeywordCompletionForPartialKeyword() {
        myFixture.configureByFile("keywordCompletionForPartialKeyword.maml")
        val completions = myFixture.completeBasic()

        // When there's a single match starting with 'f', it auto-completes
        // So we should check that 'false' was suggested (either in lookup or auto-completed)
        if (completions == null) {
            // Auto-completed - check the resulting text contains 'false'
            assertTrue("Expected 'false' to be auto-completed", myFixture.editor.document.text.contains("false"))
        } else {
            // Multiple matches - check lookup contains 'false'
            val lookupStrings = completions.map { it.lookupString }
            assertTrue("Expected 'false' in completions", lookupStrings.contains("false"))
        }
    }

    fun testKeywordCompletionInArray() {
        myFixture.testCompletionVariants("keywordCompletionInArray.maml", "true", "false", "null")
    }

    fun testNoKeywordCompletionInString() {
        myFixture.configureByFile("noKeywordCompletionInString.maml")
        val completions = myFixture.completeBasic()

        // Inside strings, we shouldn't get keyword completions
        val lookupStrings = completions?.map { it.lookupString } ?: emptyList()
        assertFalse("Should not suggest 'true' inside string", lookupStrings.contains("true"))
        assertFalse("Should not suggest 'false' inside string", lookupStrings.contains("false"))
        assertFalse("Should not suggest 'null' inside string", lookupStrings.contains("null"))
    }

    fun testNoKeywordCompletionInKey() {
        myFixture.configureByFile("noKeywordCompletionInKey.maml")
        val completions = myFixture.completeBasic()

        // In key positions, we shouldn't get keyword completions
        val lookupStrings = completions?.map { it.lookupString } ?: emptyList()
        assertFalse("Should not suggest 'true' in key position", lookupStrings.contains("true"))
        assertFalse("Should not suggest 'false' in key position", lookupStrings.contains("false"))
        assertFalse("Should not suggest 'null' in key position", lookupStrings.contains("null"))
    }
}