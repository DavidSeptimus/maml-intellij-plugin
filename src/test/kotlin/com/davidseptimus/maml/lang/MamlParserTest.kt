package com.davidseptimus.maml.lang

import com.davidseptimus.maml.lang.psi.*
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.ParsingTestCase
import org.junit.Test

class MamlParserTest : ParsingTestCase("", "maml", MamlParserDefinition()) {

    override fun getTestDataPath(): String = "src/test/testData"

    override fun skipSpaces(): Boolean = true

    override fun includeRanges(): Boolean = true

    // Helper methods

    private fun parse(text: String): MamlFile {
        myFile = createPsiFile("test", text)
        return myFile as MamlFile
    }

    private fun assertNoErrors(file: MamlFile) {
        val errors = PsiTreeUtil.findChildrenOfType(file, PsiErrorElement::class.java)
        if (errors.isNotEmpty()) {
            val errorMessages = errors.joinToString("\n") {
                "Error at ${it.textRange}: ${it.errorDescription}"
            }
            throw AssertionError("Expected no parse errors, but found:\n$errorMessages")
        }
    }

    private fun assertHasErrors(file: MamlFile) {
        val errors = PsiTreeUtil.findChildrenOfType(file, PsiErrorElement::class.java)
        if (errors.isEmpty()) {
            throw AssertionError("Expected parse errors, but found none")
        }
    }

    // Object Parsing Tests

    @Test
    fun testEmptyObject() {
        val file = parse("{}")
        assertNoErrors(file)

        val obj = PsiTreeUtil.findChildOfType(file, MamlObject::class.java)
        assertNotNull("Expected an object", obj)
        assertNull("Expected no members", obj?.members)
    }

    @Test
    fun testObjectWithSingleProperty() {
        val file = parse("{ key: \"value\" }")
        assertNoErrors(file)

        val obj = PsiTreeUtil.findChildOfType(file, MamlObject::class.java)
        assertNotNull("Expected an object", obj)

        val members = obj?.members
        assertNotNull("Expected members", members)

        val keyValues = PsiTreeUtil.findChildrenOfType(members, MamlKeyValue::class.java)
        assertEquals("Expected 1 key-value pair", 1, keyValues.size)
    }

    @Test
    fun testObjectWithMultipleProperties() {
        val file = parse("{ a: 1, b: 2, c: 3 }")
        assertNoErrors(file)

        val obj = PsiTreeUtil.findChildOfType(file, MamlObject::class.java)
        val members = obj?.members
        val keyValues = PsiTreeUtil.findChildrenOfType(members, MamlKeyValue::class.java)
        assertEquals("Expected 3 key-value pairs", 3, keyValues.size)
    }

    @Test
    fun testNestedObjects() {
        val file = parse("{ outer: { inner: \"value\" } }")
        assertNoErrors(file)

        val objects = PsiTreeUtil.findChildrenOfType(file, MamlObject::class.java)
        assertEquals("Expected 2 objects (outer and inner)", 2, objects.size)
    }

    @Test
    fun testObjectWithTrailingComma() {
        val file = parse("{ key: \"value\", }")
        assertNoErrors(file)

        val obj = PsiTreeUtil.findChildOfType(file, MamlObject::class.java)
        assertNotNull("Expected an object", obj)
    }

    @Test
    fun testObjectWithNewlineSeparators() {
        val file = parse("{\n  a: 1\n  b: 2\n}")
        assertNoErrors(file)

        val obj = PsiTreeUtil.findChildOfType(file, MamlObject::class.java)
        val members = obj?.members
        val keyValues = PsiTreeUtil.findChildrenOfType(members, MamlKeyValue::class.java)
        assertEquals("Expected 2 key-value pairs", 2, keyValues.size)
    }

    @Test
    fun testObjectWithMixedSeparators() {
        val file = parse("{ a: 1, b: 2\n  c: 3 }")
        assertNoErrors(file)

        val obj = PsiTreeUtil.findChildOfType(file, MamlObject::class.java)
        val members = obj?.members
        val keyValues = PsiTreeUtil.findChildrenOfType(members, MamlKeyValue::class.java)
        assertEquals("Expected 3 key-value pairs", 3, keyValues.size)
    }

    @Test
    fun testObjectWithQuotedKeys() {
        val file = parse("{ \"quoted-key\": \"value\" }")
        assertNoErrors(file)

        val obj = PsiTreeUtil.findChildOfType(file, MamlObject::class.java)
        assertNotNull("Expected an object", obj)
    }

    @Test
    fun testObjectWithIdentifierKeys() {
        val file = parse("{ unquoted-key: \"value\" }")
        assertNoErrors(file)

        val obj = PsiTreeUtil.findChildOfType(file, MamlObject::class.java)
        assertNotNull("Expected an object", obj)
    }

    // Array Parsing Tests

    @Test
    fun testEmptyArray() {
        val file = parse("[]")
        assertNoErrors(file)

        val array = PsiTreeUtil.findChildOfType(file, MamlArray::class.java)
        assertNotNull("Expected an array", array)
        assertNull("Expected no items", array?.items)
    }

    @Test
    fun testArrayWithSingleElement() {
        val file = parse("[1]")
        assertNoErrors(file)

        val array = PsiTreeUtil.findChildOfType(file, MamlArray::class.java)
        assertNotNull("Expected an array", array)

        val items = array?.items
        assertNotNull("Expected items", items)

        val values = PsiTreeUtil.findChildrenOfType(items, MamlValue::class.java)
        assertEquals("Expected 1 value", 1, values.size)
    }

    @Test
    fun testArrayWithMultipleElements() {
        val file = parse("[1, 2, 3]")
        assertNoErrors(file)

        val array = PsiTreeUtil.findChildOfType(file, MamlArray::class.java)
        val items = array?.items
        val values = PsiTreeUtil.findChildrenOfType(items, MamlValue::class.java)
        assertEquals("Expected 3 values", 3, values.size)
    }

    @Test
    fun testNestedArrays() {
        val file = parse("[[1, 2], [3, 4]]")
        assertNoErrors(file)

        val arrays = PsiTreeUtil.findChildrenOfType(file, MamlArray::class.java)
        assertEquals("Expected 3 arrays (outer and 2 inner)", 3, arrays.size)
    }

    @Test
    fun testArrayWithTrailingComma() {
        val file = parse("[1, 2, 3,]")
        assertNoErrors(file)

        val array = PsiTreeUtil.findChildOfType(file, MamlArray::class.java)
        assertNotNull("Expected an array", array)
    }

    @Test
    fun testArrayWithNewlineSeparators() {
        val file = parse("[\n  1\n  2\n  3\n]")
        assertNoErrors(file)

        val array = PsiTreeUtil.findChildOfType(file, MamlArray::class.java)
        val items = array?.items
        val values = PsiTreeUtil.findChildrenOfType(items, MamlValue::class.java)
        assertEquals("Expected 3 values", 3, values.size)
    }

    @Test
    fun testArrayWithMixedTypes() {
        val file = parse("[1, \"string\", true, false, null]")
        assertNoErrors(file)

        val array = PsiTreeUtil.findChildOfType(file, MamlArray::class.java)
        val items = array?.items
        val values = PsiTreeUtil.findChildrenOfType(items, MamlValue::class.java)
        assertEquals("Expected 5 values", 5, values.size)
    }

    // Value Type Tests

    @Test
    fun testStringValue() {
        val file = parse("\"hello\"")
        assertNoErrors(file)

        val value = PsiTreeUtil.findChildOfType(file, MamlValue::class.java)
        assertNotNull("Expected a value", value)
    }

    @Test
    fun testMultilineStringValue() {
        val file = parse("\"\"\"multiline\nstring\"\"\"")
        assertNoErrors(file)

        val value = PsiTreeUtil.findChildOfType(file, MamlValue::class.java)
        assertNotNull("Expected a value", value)
    }

    @Test
    fun testNumberValue() {
        val file = parse("123")
        assertNoErrors(file)

        val value = PsiTreeUtil.findChildOfType(file, MamlValue::class.java)
        assertNotNull("Expected a value", value)
    }

    @Test
    fun testDecimalNumberValue() {
        val file = parse("123.456")
        assertNoErrors(file)

        val value = PsiTreeUtil.findChildOfType(file, MamlValue::class.java)
        assertNotNull("Expected a value", value)
    }

    @Test
    fun testScientificNotationValue() {
        val file = parse("1.5e-3")
        assertNoErrors(file)

        val value = PsiTreeUtil.findChildOfType(file, MamlValue::class.java)
        assertNotNull("Expected a value", value)
    }

    @Test
    fun testTrueValue() {
        val file = parse("true")
        assertNoErrors(file)

        val value = PsiTreeUtil.findChildOfType(file, MamlValue::class.java)
        assertNotNull("Expected a value", value)
    }

    @Test
    fun testFalseValue() {
        val file = parse("false")
        assertNoErrors(file)

        val value = PsiTreeUtil.findChildOfType(file, MamlValue::class.java)
        assertNotNull("Expected a value", value)
    }

    @Test
    fun testNullValue() {
        val file = parse("null")
        assertNoErrors(file)

        val value = PsiTreeUtil.findChildOfType(file, MamlValue::class.java)
        assertNotNull("Expected a value", value)
    }

    @Test
    fun testStandaloneComment() {
        val file = parse("# this is a comment")
        // Standalone comments at the file level are valid and should not cause errors
        assertNoErrors(file)
    }

    // Complex Structure Tests

    @Test
    fun testObjectInArray() {
        val file = parse("[{ key: \"value\" }]")
        assertNoErrors(file)

        val array = PsiTreeUtil.findChildOfType(file, MamlArray::class.java)
        val obj = PsiTreeUtil.findChildOfType(file, MamlObject::class.java)
        assertNotNull("Expected an array", array)
        assertNotNull("Expected an object", obj)
    }

    @Test
    fun testArrayInObject() {
        val file = parse("{ key: [1, 2, 3] }")
        assertNoErrors(file)

        val obj = PsiTreeUtil.findChildOfType(file, MamlObject::class.java)
        val array = PsiTreeUtil.findChildOfType(file, MamlArray::class.java)
        assertNotNull("Expected an object", obj)
        assertNotNull("Expected an array", array)
    }

    @Test
    fun testDeeplyNestedStructure() {
        val file = parse("{ a: { b: { c: [1, 2, { d: \"value\" }] } } }")
        assertNoErrors(file)

        val objects = PsiTreeUtil.findChildrenOfType(file, MamlObject::class.java)
        val arrays = PsiTreeUtil.findChildrenOfType(file, MamlArray::class.java)
        assertEquals("Expected 4 objects", 4, objects.size)
        assertEquals("Expected 1 array", 1, arrays.size)
    }

    @Test
    fun testComplexDocument() {
        val file = parse("""
            {
              name: "MAML"
              version: 1.0
              features: [
                "unquoted keys"
                "multiline strings"
                "comments"
              ]
              metadata: {
                author: "Dave"
                year: 2024
              }
            }
        """.trimIndent())
        assertNoErrors(file)

        val obj = PsiTreeUtil.findChildOfType(file, MamlObject::class.java)
        assertNotNull("Expected an object", obj)
    }

    // Comment Tests

    @Test
    fun testCommentInObject() {
        val file = parse("{ # comment\n  key: \"value\" }")
        assertNoErrors(file)

        val obj = PsiTreeUtil.findChildOfType(file, MamlObject::class.java)
        assertNotNull("Expected an object", obj)
    }

    @Test
    fun testCommentInArray() {
        val file = parse("[ # comment\n  1, 2, 3 ]")
        assertNoErrors(file)

        val array = PsiTreeUtil.findChildOfType(file, MamlArray::class.java)
        assertNotNull("Expected an array", array)
    }

    @Test
    fun testMultipleComments() {
        val file = parse("# comment 1\n# comment 2\n{ key: \"value\" }")
        assertNoErrors(file)

        val obj = PsiTreeUtil.findChildOfType(file, MamlObject::class.java)
        assertNotNull("Expected an object", obj)
    }

    // Edge Cases

    @Test
    fun testEmptyFile() {
        val file = parse("")
        assertNoErrors(file)
    }

    @Test
    fun testWhitespaceOnly() {
        val file = parse("   \n\t\n   ")
        assertNoErrors(file)
    }

    @Test
    fun testOnlyComment() {
        val file = parse("# just a comment")
        assertNoErrors(file)
    }

    // Error Recovery Tests

    @Test
    fun testUnclosedObject() {
        val file = parse("{ key: \"value\"")
        assertHasErrors(file)

        // Should still parse the object and key-value
        val obj = PsiTreeUtil.findChildOfType(file, MamlObject::class.java)
        assertNotNull("Expected an object despite error", obj)
    }

    @Test
    fun testUnclosedArray() {
        val file = parse("[1, 2, 3")
        assertHasErrors(file)

        // Should still parse the array and values
        val array = PsiTreeUtil.findChildOfType(file, MamlArray::class.java)
        assertNotNull("Expected an array despite error", array)
    }

    @Test
    fun testMissingColon() {
        val file = parse("{ key \"value\" }")
        assertHasErrors(file)
    }

    @Test
    fun testMissingValue() {
        val file = parse("{ key: }")
        assertHasErrors(file)
    }

    @Test
    fun testInvalidKeyValueInObject() {
        val file = parse("{ key: value, 123: \"error\", good: \"value\" }")
        // Should recover and parse the good key-value
        val obj = PsiTreeUtil.findChildOfType(file, MamlObject::class.java)
        assertNotNull("Expected an object", obj)
    }

    @Test
    fun testInvalidValueInArray() {
        val file = parse("[1, :, 3]")
        // Should recover and parse valid values
        val array = PsiTreeUtil.findChildOfType(file, MamlArray::class.java)
        assertNotNull("Expected an array", array)
    }
}