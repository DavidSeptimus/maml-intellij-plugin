package com.davidseptimus.maml.annotators

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MamlMissingCommaAnnotatorTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String = "src/test/testData"

    // Array tests
    fun testArraySameLineWithoutComma() {
        myFixture.configureByText(
            "test.maml",
            "[1<error descr=\"Missing comma between items on same line\"> </error>2]"
        )
        myFixture.checkHighlighting()
    }

    fun testArraySameLineMultipleItemsWithoutCommas() {
        myFixture.configureByText(
            "test.maml",
            "[1<error descr=\"Missing comma between items on same line\"> </error>2<error descr=\"Missing comma between items on same line\"> </error>3]"
        )
        myFixture.checkHighlighting()
    }

    fun testArraySameLineWithCommaNoError() {
        myFixture.configureByText("test.maml", "[1, 2, 3]")
        val highlights = myFixture.doHighlighting(HighlightSeverity.ERROR)
        val commaErrors = highlights.filter { it.description.contains("Missing comma") }
        assertTrue("Should not have missing comma errors when commas present", commaErrors.isEmpty())
    }

    fun testArrayDifferentLinesWithoutCommaNoError() {
        myFixture.configureByText(
            "test.maml", """
            [
              1
              2
              3
            ]
        """.trimIndent()
        )
        val highlights = myFixture.doHighlighting(HighlightSeverity.ERROR)
        val commaErrors = highlights.filter { it.description.contains("Missing comma") }
        assertTrue("Should not have missing comma errors when items on different lines", commaErrors.isEmpty())
    }

    fun testArrayDifferentLinesWithCommaNoError() {
        myFixture.configureByText(
            "test.maml", """
            [
              1,
              2,
              3
            ]
        """.trimIndent()
        )
        val highlights = myFixture.doHighlighting(HighlightSeverity.ERROR)
        val commaErrors = highlights.filter { it.description.contains("Missing comma") }
        assertTrue("Should not have missing comma errors", commaErrors.isEmpty())
    }

    // Object tests
    fun testObjectSameLineWithoutComma() {
        myFixture.configureByText(
            "test.maml",
            "{a: 1<error descr=\"Missing comma between items on same line\"> </error>b: 2}"
        )
        myFixture.checkHighlighting()
    }

    fun testObjectSameLineMultipleItemsWithoutCommas() {
        myFixture.configureByText(
            "test.maml",
            "{a: 1<error descr=\"Missing comma between items on same line\"> </error>b: 2<error descr=\"Missing comma between items on same line\"> </error>c: 3}"
        )
        myFixture.checkHighlighting()
    }

    fun testObjectSameLineWithCommaNoError() {
        myFixture.configureByText("test.maml", "{a: 1, b: 2, c: 3}")
        val highlights = myFixture.doHighlighting(HighlightSeverity.ERROR)
        val commaErrors = highlights.filter { it.description.contains("Missing comma") }
        assertTrue("Should not have missing comma errors when commas present", commaErrors.isEmpty())
    }

    fun testObjectDifferentLinesWithoutCommaNoError() {
        myFixture.configureByText(
            "test.maml", """
            {
              a: 1
              b: 2
              c: 3
            }
        """.trimIndent()
        )
        val highlights = myFixture.doHighlighting(HighlightSeverity.ERROR)
        val commaErrors = highlights.filter { it.description.contains("Missing comma") }
        assertTrue("Should not have missing comma errors when items on different lines", commaErrors.isEmpty())
    }

    fun testObjectDifferentLinesWithCommaNoError() {
        myFixture.configureByText(
            "test.maml", """
            {
              a: 1,
              b: 2,
              c: 3
            }
        """.trimIndent()
        )
        val highlights = myFixture.doHighlighting(HighlightSeverity.ERROR)
        val commaErrors = highlights.filter { it.description.contains("Missing comma") }
        assertTrue("Should not have missing comma errors", commaErrors.isEmpty())
    }

    // Nested structures
    fun testNestedArraysWithMissingCommas() {
        myFixture.configureByText(
            "test.maml",
            "[[1<error descr=\"Missing comma between items on same line\"> </error>2]<error descr=\"Missing comma between items on same line\"> </error>[3<error descr=\"Missing comma between items on same line\"> </error>4]]"
        )
        myFixture.checkHighlighting()
    }

    fun testNestedObjectsWithMissingCommas() {
        myFixture.configureByText(
            "test.maml",
            "{outer: {a: 1<error descr=\"Missing comma between items on same line\"> </error>b: 2}}"
        )
        myFixture.checkHighlighting()
    }

    // Quick fix tests
    fun testInsertCommaQuickFix() {
        myFixture.configureByText("test.maml", "[1<caret> 2]")
        val intentions = myFixture.availableIntentions
        val insertCommaFix = intentions.find { it.text == "Insert comma" }
        assertNotNull("Insert comma quick fix should be available", insertCommaFix)

        myFixture.launchAction(insertCommaFix!!)
        myFixture.checkResult("[1, 2]")
    }

    fun testBreakItemsOntoLinesQuickFix() {
        myFixture.configureByText("test.maml", "[1<caret> 2 3]")
        val intentions = myFixture.availableIntentions
        val breakLinesFix = intentions.find { it.text == "Break items onto separate lines" }
        assertNotNull("Break items onto separate lines quick fix should be available", breakLinesFix)

        myFixture.launchAction(breakLinesFix!!)
        myFixture.checkResult(
            """
            [1
            2
            3
            ]
        """.trimIndent()
        )
    }

    fun testBreakObjectItemsOntoLinesQuickFix() {
        myFixture.configureByText("test.maml", "{a: 1<caret> b: 2}")
        val intentions = myFixture.availableIntentions
        val breakLinesFix = intentions.find { it.text == "Break items onto separate lines" }
        assertNotNull("Break items onto separate lines quick fix should be available", breakLinesFix)

        myFixture.launchAction(breakLinesFix!!)
        myFixture.checkResult(
            """
            {a: 1
            b: 2}
        """.trimIndent()
        )
    }
}