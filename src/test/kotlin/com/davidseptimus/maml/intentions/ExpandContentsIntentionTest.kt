package com.davidseptimus.maml.intentions

import com.davidseptimus.maml.formatter.MamlCodeStyleSettings
import com.intellij.application.options.CodeStyle
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test

class ExpandContentsIntentionTest : BasePlatformTestCase() {

    @Test
    fun testIntentionAvailableOnInlineArray() {
        myFixture.configureByText(
            "test.maml", """
            { items: [<caret>1, 2, 3] }
        """.trimIndent()
        )

        val intentions = myFixture.availableIntentions.filter {
            it.text.contains("Expand contents")
        }

        assertTrue("Intention should be available on inline arrays", intentions.isNotEmpty())
    }

    @Test
    fun testIntentionAvailableOnInlineObject() {
        myFixture.configureByText(
            "test.maml", """
            { config: {<caret>a: 1, b: 2} }
        """.trimIndent()
        )

        val intentions = myFixture.availableIntentions.filter {
            it.text.contains("Expand contents")
        }

        assertTrue("Intention should be available on inline objects", intentions.isNotEmpty())
    }

    @Test
    fun testExpandSimpleArrayWithCommas() {
        myFixture.configureByText("test.maml", "{ items: [<caret>1, 2, 3] }")

        // Ensure REMOVE_COMMAS is false (use commas)
        val settings = CodeStyle.getSettings(myFixture.file)
        val customSettings = settings.getCustomSettings(MamlCodeStyleSettings::class.java)
        customSettings.REMOVE_COMMAS = false

        val intention = myFixture.availableIntentions.firstOrNull {
            it.text.contains("Expand contents")
        }
        assertNotNull("Expand contents intention should be available", intention)

        myFixture.launchAction(intention!!)
        myFixture.checkResult(
            """
            { items: [
              1,
              2,
              3
            ] }
        """.trimIndent()
        )
    }

    @Test
    fun testExpandSimpleArrayWithoutCommas() {
        myFixture.configureByText("test.maml", "{ items: [<caret>1, 2, 3] }")

        // Set REMOVE_COMMAS to true (no commas)
        val settings = CodeStyle.getSettings(myFixture.file)
        val customSettings = settings.getCustomSettings(MamlCodeStyleSettings::class.java)
        customSettings.REMOVE_COMMAS = true

        val intention = myFixture.availableIntentions.firstOrNull {
            it.text.contains("Expand contents")
        }
        assertNotNull("Expand contents intention should be available", intention)

        myFixture.launchAction(intention!!)
        myFixture.checkResult(
            """
            { items: [
              1
              2
              3
            ] }
        """.trimIndent()
        )
    }

    @Test
    fun testExpandSimpleObjectWithCommas() {
        myFixture.configureByText("test.maml", "{ config: {<caret>a: 1, b: 2} }")

        // Ensure REMOVE_COMMAS is false (use commas)
        val settings = CodeStyle.getSettings(myFixture.file)
        val customSettings = settings.getCustomSettings(MamlCodeStyleSettings::class.java)
        customSettings.REMOVE_COMMAS = false

        val intention = myFixture.availableIntentions.firstOrNull {
            it.text.contains("Expand contents")
        }
        assertNotNull("Expand contents intention should be available", intention)

        myFixture.launchAction(intention!!)
        myFixture.checkResult(
            """
            { config: {
              a: 1,
              b: 2
            } }
        """.trimIndent()
        )
    }

    @Test
    fun testExpandNestedContainers() {
        myFixture.configureByText(
            "test.maml",
            """{ config: {<caret>server: {host: localhost, port: 8080}, debug: true} }"""
        )

        // Ensure REMOVE_COMMAS is false (use commas)
        val settings = CodeStyle.getSettings(myFixture.file)
        val customSettings = settings.getCustomSettings(MamlCodeStyleSettings::class.java)
        customSettings.REMOVE_COMMAS = false

        val intention = myFixture.availableIntentions.firstOrNull {
            it.text.contains("Expand contents")
        }
        assertNotNull("Expand contents intention should be available", intention)

        myFixture.launchAction(intention!!)
        myFixture.checkResult(
            """
            { config: {server: {
              host: localhost,
              port: 8080
            }, debug: true} }
        """.trimIndent()
        )
    }

    @Test
    fun testExpandArrayWithNestedArrays() {
        myFixture.configureByText("test.maml", "{ matrix: [<caret>[1, 2], [3, 4]] }")

        // Ensure REMOVE_COMMAS is false (use commas)
        val settings = CodeStyle.getSettings(myFixture.file)
        val customSettings = settings.getCustomSettings(MamlCodeStyleSettings::class.java)
        customSettings.REMOVE_COMMAS = false

        val intention = myFixture.availableIntentions.firstOrNull {
            it.text.contains("Expand contents")
        }
        assertNotNull("Expand contents intention should be available", intention)

        myFixture.launchAction(intention!!)
        myFixture.checkResult(
            """
            { matrix: [
              [
                1,
                2
              ],
              [
                3,
                4
              ]
            ] }
        """.trimIndent()
        )
    }

    @Test
    fun testNotAvailableOnEmptyArray() {
        myFixture.configureByText("test.maml", "{ items: [<caret>] }")

        val intentions = myFixture.availableIntentions.filter {
            it.text.contains("Expand contents")
        }

        assertTrue("Intention should not be available on empty arrays", intentions.isEmpty())
    }

    @Test
    fun testNotAvailableOnPrimitiveValue() {
        myFixture.configureByText("test.maml", "{ value: 12<caret>3 }")

        val intentions = myFixture.availableIntentions.filter {
            it.text.contains("Expand contents")
        }

        assertTrue("Intention should not be available on primitive values", intentions.isEmpty())
    }
}
