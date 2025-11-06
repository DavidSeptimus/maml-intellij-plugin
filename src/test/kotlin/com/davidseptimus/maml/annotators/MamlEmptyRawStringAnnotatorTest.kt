package com.davidseptimus.maml.annotators

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Test suite for MamlEmptyRawStringAnnotator validating detection of empty raw strings
 * that don't contain at least one newline character.
 */
class MamlEmptyRawStringAnnotatorTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String = "src/test/testData/annotators/emptyRawString"

    // ========== Valid Raw Strings (should not trigger warning) ==========

    fun testRawStringWithNewline() {
        myFixture.configureByFile("validWithNewline.maml")
        myFixture.checkHighlighting()
        assertNoWarnings()
    }

    fun testRawStringWithSingleNewline() {
        myFixture.configureByFile("validWithSingleNewline.maml")
        myFixture.checkHighlighting()
        assertNoWarnings()
    }

    fun testRawStringWithTextAndNewline() {
        myFixture.configureByFile("validWithTextAndNewline.maml")
        myFixture.checkHighlighting()
        assertNoWarnings()
    }

    fun testRawStringWithSpaces() {
        myFixture.configureByFile("validWithSpaces.maml")
        myFixture.checkHighlighting()
        assertNoWarnings()
    }

    fun testRawStringWithSingleWord() {
        myFixture.configureByFile("validWithSingleWord.maml")
        myFixture.checkHighlighting()
        assertNoWarnings()
    }

    fun testRawStringWithTextButNoNewline() {
        myFixture.configureByFile("validWithTextNoNewline.maml")
        myFixture.checkHighlighting()
        assertNoWarnings()
    }

    // ========== Invalid Raw Strings (should trigger warning) ==========

    fun testCompletelyEmptyRawString() {
        myFixture.configureByFile("completelyEmpty.maml")
        myFixture.checkHighlighting()
    }

    fun testMultipleEmptyRawStrings() {
        myFixture.configureByFile("multipleEmptyRawStrings.maml")
        myFixture.checkHighlighting()
    }

    // ========== Quick Fix Tests ==========

    fun testQuickFixCompletelyEmpty() {
        myFixture.configureByFile("quickFixCompletelyEmpty.maml")
        val intention = myFixture.findSingleIntention("Replace with \"\"")
        myFixture.launchAction(intention)
        myFixture.checkResultByFile("quickFixCompletelyEmpty_after.maml")
    }

    // ========== Helper Methods ==========

    private fun assertNoWarnings() {
        val highlights = myFixture.doHighlighting(HighlightSeverity.ERROR)
        // Filter to only empty raw string errors
        val emptyRawStringErrors = highlights.filter {
            it.description.contains("Raw string must contain at least one character")
        }
        if (emptyRawStringErrors.isNotEmpty()) {
            val errorMessages = emptyRawStringErrors.joinToString("\n") {
                "Error at '${it.text}': ${it.description}"
            }
            fail("Expected no empty raw string errors, but found:\n$errorMessages")
        }
    }
}