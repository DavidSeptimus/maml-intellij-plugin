package com.davidseptimus.maml.annotators

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MamlInvalidValueAnnotatorTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String = "src/test/testData"

    fun testPartialKeywordAnnotation() {
        myFixture.configureByText("test.maml", "{ key: <error descr=\"Invalid value 'fa'. Did you mean 'true', 'false', or 'null'?\">fa</error> }")
        myFixture.checkHighlighting()
    }

    fun testUnterminatedStringAnnotation() {
        myFixture.configureByText("test.maml", "{ key: <error descr=\"Unterminated string literal\">\"unterminated }</error><EOLError descr=\"<incomplete key value>, <key value>, MamlTokenType.COMMA or MamlTokenType.RBRACE expected\"></EOLError>")
        myFixture.checkHighlighting()
    }

    fun testInvalidIdentifierInValuePosition() {
        myFixture.configureByText("test.maml", "[<error descr=\"Invalid identifier in value position: 'someIdentifier'\">someIdentifier</error>]")
        myFixture.checkHighlighting()
    }

    fun testValidKeywordNoError() {
        myFixture.configureByText("test.maml", "{ key: true }")
        myFixture.checkHighlighting()

        val highlights = myFixture.doHighlighting(HighlightSeverity.ERROR)
        val invalidValueErrors = highlights.filter { it.text == "true" }
        assertTrue("Should not have errors on valid keyword 'true'", invalidValueErrors.isEmpty())
    }

    fun testValidStringNoError() {
        myFixture.configureByText("test.maml", "{ key: \"valid string\" }")
        myFixture.checkHighlighting()

        val highlights = myFixture.doHighlighting(HighlightSeverity.ERROR)
        val stringErrors = highlights.filter { it.description.contains("Unterminated") }
        assertTrue("Should not have errors on valid string", stringErrors.isEmpty())
    }
}