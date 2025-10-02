package com.davidseptimus.maml.inspections

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test

class MamlDuplicateKeysInspectionTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(MamlDuplicateKeysInspection())
    }

    @Test
    fun testNoDuplicateKeys() {
        myFixture.configureByText("test.maml", """
            {
              name: "John"
              age: 30
              city: "NYC"
            }
        """.trimIndent())

        val highlights = myFixture.doHighlighting()
        val duplicateWarnings = highlights.filter { it.description?.contains("Duplicate key") == true }

        assertEquals("Should have no duplicate key warnings", 0, duplicateWarnings.size)
    }

    @Test
    fun testDuplicateUnquotedKeys() {
        myFixture.configureByText("test.maml", """
            {
              name: "John"
              age: 30
              name: "Jane"
            }
        """.trimIndent())

        val highlights = myFixture.doHighlighting()
        val duplicateWarnings = highlights.filter { it.description?.contains("Duplicate key") == true }

        assertTrue("Should have duplicate key warning", duplicateWarnings.size >= 1)
    }

    @Test
    fun testDuplicatesInDifferentObjects() {
        myFixture.configureByText("test.maml", """
            {
              outer: {
                name: "John"
              }
              inner: {
                name: "Jane"
              }
            }
        """.trimIndent())

        val highlights = myFixture.doHighlighting()
        val duplicateWarnings = highlights.filter { it.description?.contains("Duplicate key") == true }

        assertEquals("Same keys in different objects should NOT trigger warnings", 0, duplicateWarnings.size)
    }

    @Test
    fun testEmptyObject() {
        myFixture.configureByText("test.maml", "{}")

        val highlights = myFixture.doHighlighting()
        val duplicateWarnings = highlights.filter { it.description?.contains("Duplicate key") == true }

        assertEquals("Empty object should have no warnings", 0, duplicateWarnings.size)
    }

    @Test
    fun testSingleKey() {
        myFixture.configureByText("test.maml", """{ name: "John" }""")

        val highlights = myFixture.doHighlighting()
        val duplicateWarnings = highlights.filter { it.description?.contains("Duplicate key") == true }

        assertEquals("Single key should have no warnings", 0, duplicateWarnings.size)
    }
}