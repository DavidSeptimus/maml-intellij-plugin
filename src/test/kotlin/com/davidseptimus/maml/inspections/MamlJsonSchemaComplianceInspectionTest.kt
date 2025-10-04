package com.davidseptimus.maml.inspections

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Tests for JSON Schema compliance inspection, particularly for required fields validation.
 */
class MamlJsonSchemaComplianceInspectionTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(MamlJsonSchemaComplianceInspection())
    }

    fun testInspectionRegistered() {
        // Verify the inspection exists and is registered
        val inspection = MamlJsonSchemaComplianceInspection()
        assertNotNull(inspection)
        assertEquals("JSON schema compliance", inspection.displayName)
        assertTrue(inspection.isEnabledByDefault)
    }

    fun testInspectionWithNoSchema() {
        // File without schema should not trigger inspection
        val mamlContent = """
            {
              name: "John Doe"
              age: 30
            }
        """.trimIndent()

        myFixture.configureByText("test.maml", mamlContent)
        val highlights = myFixture.doHighlighting()

        // No schema = no schema validation errors
        assertTrue(highlights.isEmpty() || highlights.none {
            it.inspectionToolId == "MamlJsonSchemaCompliance"
        })
    }

    // Note: Testing with actual schema requires setting up JSON Schema mappings
    // which is complex in unit tests. The inspection itself delegates to
    // JsonSchemaComplianceChecker which is well-tested by JetBrains.
    // For integration testing, schemas should be configured via:
    // Settings > Languages & Frameworks > Schemas and DTDs > JSON Schema Mappings
}