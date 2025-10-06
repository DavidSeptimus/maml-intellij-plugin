package com.davidseptimus.maml.intentions

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test

class InlineContentsIntentionTest : BasePlatformTestCase() {

    @Test
    fun testIntentionAvailableOnMultilineArray() {
        myFixture.configureByText(
            "test.maml", """
            { items: [
              <caret>1
              2
              3
            ] }
        """.trimIndent()
        )

        val intentions = myFixture.availableIntentions.filter {
            it.text.contains("Inline contents")
        }

        assertTrue("Intention should be available on multiline arrays", intentions.isNotEmpty())
    }

    @Test
    fun testIntentionAvailableOnMultilineObject() {
        myFixture.configureByText(
            "test.maml", """
            { config: {
              <caret>a: 1
              b: 2
            } }
        """.trimIndent()
        )

        val intentions = myFixture.availableIntentions.filter {
            it.text.contains("Inline contents")
        }

        assertTrue("Intention should be available on multiline objects", intentions.isNotEmpty())
    }

    @Test
    fun testInlineSimpleArray() {
        myFixture.configureByText(
            "test.maml", """
            { items: [
              <caret>1
              2
              3
            ] }
        """.trimIndent()
        )

        val intention = myFixture.availableIntentions.firstOrNull {
            it.text.contains("Inline contents")
        }

        assertNotNull("Inline contents intention should be available", intention)
        myFixture.launchAction(intention!!)
        myFixture.checkResult("{ items: [1, 2, 3] }")
    }

    @Test
    fun testInlineSimpleObject() {
        myFixture.configureByText(
            "test.maml", """
            { config: {
              <caret>a: 1
              b: 2
            } }
        """.trimIndent()
        )

        val intention = myFixture.availableIntentions.firstOrNull {
            it.text.contains("Inline contents")
        }

        assertNotNull("Inline contents intention should be available", intention)
        myFixture.launchAction(intention!!)
        myFixture.checkResult("{ config: {a: 1, b: 2} }")
    }

    @Test
    fun testInlineNestedContainers() {
        myFixture.configureByText(
            "test.maml", """
            { config: {
              <caret>server: {
                host: "localhost"
                port: 8080
              }
              debug: true
            } }
        """.trimIndent()
        )

        val intention = myFixture.availableIntentions.firstOrNull {
            it.text.contains("Inline contents")
        }

        assertNotNull("Inline contents intention should be available", intention)
        myFixture.launchAction(intention!!)
        myFixture.checkResult(
            """
            { config: {
              server: {host: "localhost", port: 8080}
              debug: true
            } }
        """.trimIndent()
        )
    }

    @Test
    fun testInlineArrayWithNestedArrays() {
        myFixture.configureByText(
            "test.maml", """
            { matrix: <caret>[
              [1
              2]
              [3
              4]
            ] }
        """.trimIndent()
        )

        val intention = myFixture.availableIntentions.firstOrNull {
            it.text.contains("Inline contents")
        }

        assertNotNull("Inline contents intention should be available", intention)
        myFixture.launchAction(intention!!)
        myFixture.checkResult("{ matrix: [[1, 2], [3, 4]] }")
    }

    @Test
    fun testNotAvailableOnEmptyArray() {
        myFixture.configureByText(
            "test.maml", """
            { items: [<caret>] }
        """.trimIndent()
        )

        val intentions = myFixture.availableIntentions.filter {
            it.text.contains("Inline contents")
        }

        assertTrue("Intention should not be available on empty arrays", intentions.isEmpty())
    }

    @Test
    fun testNotAvailableOnPrimitiveValue() {
        myFixture.configureByText(
            "test.maml", """
            { value: 12<caret>3 }
        """.trimIndent()
        )

        val intentions = myFixture.availableIntentions.filter {
            it.text.contains("Inline contents")
        }

        assertTrue("Intention should not be available on primitive values", intentions.isEmpty())
    }
}