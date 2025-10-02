package com.davidseptimus.maml.intentions

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test

class ConvertToSingleLineStringIntentionTest : BasePlatformTestCase() {

    @Test
    fun testNotAvailableOnSingleLineString() {
        myFixture.configureByText("test.maml", """
            { text: "already<caret> single-line" }
        """.trimIndent())

        val intentions = myFixture.availableIntentions.filter {
            it.text.contains("Convert to single-line string")
        }

        assertTrue("Intention should not be available on single-line strings", intentions.isEmpty())
    }

    @Test
    fun testNotAvailableOnNonString() {
        myFixture.configureByText("test.maml", """
            { value: 12<caret>3 }
        """.trimIndent())

        val intentions = myFixture.availableIntentions.filter {
            it.text.contains("Convert to single-line string")
        }

        assertTrue("Intention should not be available on numbers", intentions.isEmpty())
    }

    @Test
    fun testNotAvailableOnIdentifier() {
        myFixture.configureByText("test.maml", """
            { te<caret>xt: "value" }
        """.trimIndent())

        val intentions = myFixture.availableIntentions.filter {
            it.text.contains("Convert to single-line string")
        }

        assertTrue("Intention should not be available on identifiers", intentions.isEmpty())
    }
}