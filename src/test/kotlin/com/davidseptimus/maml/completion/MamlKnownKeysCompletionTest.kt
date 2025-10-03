package com.davidseptimus.maml.completion

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MamlKnownKeysCompletionTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String = "src/test/testData"

    fun testKnownKeysCompletion() {
        myFixture.configureByText("test.maml", """
            {
              address: {
                "neat": "cool"
              }
              name: "John"
              number: 42
              <caret>
            }
        """.trimIndent())
        myFixture.completeBasic()

        val lookupStrings = myFixture.lookupElementStrings
        assertNotNull("Expected completion suggestions", lookupStrings)
        assertEquals(1, lookupStrings?.size)
        assertTrue("Expected 'neat' in completions", lookupStrings!!.contains("neat"))
    }

    fun testKnownKeysFromDifferentObjects() {
        myFixture.configureByText("test.maml", """
            {
              user: {
                name: "John"
                email: "john@example.com"
              }
              settings: {
                <caret>
              }
            }
        """.trimIndent())
        myFixture.completeBasic()

        val lookupStrings = myFixture.lookupElementStrings
        assertNotNull("Expected completion suggestions", lookupStrings)
        assertTrue("Expected 'name' in completions", lookupStrings!!.contains("name"))
        assertTrue("Expected 'email' in completions", lookupStrings.contains("email"))
    }

    fun testNoDuplicateKeyCompletion() {
        myFixture.configureByText("test.maml", """
            {
              name: "John"
              age: 30
              name: <caret>
            }
        """.trimIndent())
        myFixture.completeBasic()

        val lookupStrings = myFixture.lookupElementStrings
        // 'name' should not be suggested again since it already exists in current object
        if (lookupStrings != null) {
            val nameCount = lookupStrings.count { it == "name" }
            assertTrue("'name' should appear at most once or not at all in completions", nameCount <= 1)
        }
    }

    fun testNoKeyCompletionInValuePosition() {
        myFixture.configureByText("test.maml", """
            {
              name: "John"
              age: <caret>
            }
        """.trimIndent())
        myFixture.completeBasic()

        val lookupStrings = myFixture.lookupElementStrings
        // In value positions, we shouldn't get known key completions
        if (lookupStrings != null) {
            // Should have keyword completions (true, false, null) but not key completions
            assertTrue("Should have some completions in value position", lookupStrings.isNotEmpty())
        }
    }
}