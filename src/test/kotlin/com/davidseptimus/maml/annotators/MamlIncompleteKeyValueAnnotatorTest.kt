package com.davidseptimus.maml.annotators

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MamlIncompleteKeyValueAnnotatorTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String = "src/test/testData"

    fun testIncompleteKeyValueAnnotation() {
        myFixture.configureByText("test.maml", "{ <EOLError descr=\"Expected ':' after key 'myKey'\">myKey</EOLError> }")
        myFixture.checkHighlighting()
    }

    fun testMultipleIncompleteKeyValues() {
        myFixture.configureByText("test.maml", """
            {
              complete: "value"
              <EOLError descr="Expected ':' after key 'incomplete1'">incomplete1</EOLError>
              another: 123
              <EOLError descr="Expected ':' after key 'incomplete2'">incomplete2</EOLError>
            }
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    fun testCompleteKeyValueNoError() {
        myFixture.configureByText("test.maml", "{ key: \"value\" }")
        myFixture.checkHighlighting()

        val highlights = myFixture.doHighlighting(HighlightSeverity.ERROR)
        val incompleteErrors = highlights.filter { it.description.contains("Expected ':' after key") }
        assertTrue("Should not have errors on complete key-value pair", incompleteErrors.isEmpty())
    }

    fun testKeyWithColonButNoValue() {
        myFixture.configureByText("test.maml", "{ key:<error descr=\"<value> expected, got '}'\"></error><error descr=\"<value> expected, got '}'\" > </error>}")
        myFixture.checkHighlighting()
    }

    fun testQuotedIncompleteKey() {
        myFixture.configureByText("test.maml", "{ <EOLError descr=\"Expected ':' after key '\\\"quoted-key\\\"'\">\"quoted-key\"</EOLError> }")
        myFixture.checkHighlighting()
    }
}