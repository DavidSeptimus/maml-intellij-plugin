package com.davidseptimus.maml.inspections

import com.davidseptimus.maml.lang.MamlParserDefinition
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Base class for inspection tests.
 */
abstract class MamlInspectionTestBase : BasePlatformTestCase() {

    protected abstract fun getInspection(): LocalInspectionTool

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(getInspection())
    }

    protected fun doTest(code: String, expectedHighlights: Int) {
        myFixture.configureByText("test.maml", code)
        val highlights = myFixture.doHighlighting()
        val inspectionHighlights = highlights.filter {
            it.description?.contains(getInspection().shortName) == true ||
            it.inspectionToolId == getInspection().shortName
        }
        assertEquals(
            "Expected $expectedHighlights inspection highlights but found ${inspectionHighlights.size}",
            expectedHighlights,
            inspectionHighlights.size
        )
    }

    protected fun doTestWithQuickFix(
        before: String,
        after: String,
        quickFixName: String
    ) {
        myFixture.configureByText("test.maml", before.trimIndent())
        val intention = myFixture.findSingleIntention(quickFixName)
        myFixture.launchAction(intention)
        myFixture.checkResult(after.trimIndent())
    }
}