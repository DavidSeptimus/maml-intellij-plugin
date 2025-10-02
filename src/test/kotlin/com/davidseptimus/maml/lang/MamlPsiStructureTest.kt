package com.davidseptimus.maml.lang

import com.davidseptimus.maml.lang.psi.*
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.ParsingTestCase
import org.junit.Test

class MamlPsiStructureTest : ParsingTestCase("", "maml", MamlParserDefinition()) {

    override fun getTestDataPath(): String = "src/test/testData"

    override fun skipSpaces(): Boolean = true

    override fun includeRanges(): Boolean = true

    private fun parse(text: String): MamlFile {
        myFile = createPsiFile("test", text)
        return myFile as MamlFile
    }

    // MamlFile Tests

    @Test
    fun testMamlFileContainsTopLevelValue() {
        val file = parse("{ key: \"value\" }")

        val value = PsiTreeUtil.findChildOfType(file, MamlValue::class.java)
        assertNotNull("MamlFile should contain a top-level value", value)
    }

    @Test
    fun testEmptyFileHasNoValue() {
        val file = parse("")

        val value = PsiTreeUtil.findChildOfType(file, MamlValue::class.java)
        assertNull("Empty file should have no value", value)
    }

    @Test
    fun testFileToString() {
        val file = parse("{}")

        assertEquals("File toString should return 'Maml file'", "Maml file", file.toString())
    }

    // Object Element Tests

    @Test
    fun testObjectElementType() {
        val file = parse("{}")

        val obj = PsiTreeUtil.findChildOfType(file, MamlObject::class.java)
        assertNotNull("Should find object element", obj)
        assertTrue("Object should be PsiElement", obj is PsiElement)
    }

    @Test
    fun testEmptyObjectHasNoMembers() {
        val file = parse("{}")

        val obj = PsiTreeUtil.findChildOfType(file, MamlObject::class.java)
        val members = obj?.members
        assertNull("Empty object should have no members", members)
    }

    @Test
    fun testObjectWithMembersHasMembersElement() {
        val file = parse("{ key: \"value\" }")

        val obj = PsiTreeUtil.findChildOfType(file, MamlObject::class.java)
        val members = obj?.members
        assertNotNull("Object with properties should have members element", members)
        assertTrue("Members should be PsiElement", members is PsiElement)
    }

    @Test
    fun testObjectContainsKeyValueChildren() {
        val file = parse("{ a: 1, b: 2 }")

        val obj = PsiTreeUtil.findChildOfType(file, MamlObject::class.java)
        val keyValues = PsiTreeUtil.findChildrenOfType(obj, MamlKeyValue::class.java)

        assertEquals("Object should contain 2 key-value pairs", 2, keyValues.size)
    }

    @Test
    fun testNestedObjectStructure() {
        val file = parse("{ outer: { inner: \"value\" } }")

        val outerObj = PsiTreeUtil.findChildOfType(file, MamlObject::class.java)
        assertNotNull("Should find outer object", outerObj)

        val innerObj = PsiTreeUtil.findChildOfType(outerObj, MamlObject::class.java)
        assertNotNull("Should find inner object as child of outer", innerObj)
    }

    // Array Element Tests

    @Test
    fun testArrayElementType() {
        val file = parse("[]")

        val array = PsiTreeUtil.findChildOfType(file, MamlArray::class.java)
        assertNotNull("Should find array element", array)
        assertTrue("Array should be PsiElement", array is PsiElement)
    }

    @Test
    fun testEmptyArrayHasNoItems() {
        val file = parse("[]")

        val array = PsiTreeUtil.findChildOfType(file, MamlArray::class.java)
        val items = array?.items
        assertNull("Empty array should have no items", items)
    }

    @Test
    fun testArrayWithItemsHasItemsElement() {
        val file = parse("[1, 2, 3]")

        val array = PsiTreeUtil.findChildOfType(file, MamlArray::class.java)
        val items = array?.items
        assertNotNull("Array with elements should have items element", items)
        assertTrue("Items should be PsiElement", items is PsiElement)
    }

    @Test
    fun testArrayContainsValueChildren() {
        val file = parse("[1, 2, 3]")

        val array = PsiTreeUtil.findChildOfType(file, MamlArray::class.java)
        val values = PsiTreeUtil.findChildrenOfType(array, MamlValue::class.java)

        assertEquals("Array should contain 3 value elements", 3, values.size)
    }

    @Test
    fun testNestedArrayStructure() {
        val file = parse("[[1, 2], [3, 4]]")

        val outerArray = PsiTreeUtil.findChildOfType(file, MamlArray::class.java)
        assertNotNull("Should find outer array", outerArray)

        val innerArrays = PsiTreeUtil.findChildrenOfType(outerArray, MamlArray::class.java)
        assertEquals("Should find 2 inner arrays as children", 2, innerArrays.size)
    }

    // KeyValue Element Tests

    @Test
    fun testKeyValueContainsKeyAndValue() {
        val file = parse("{ key: \"value\" }")

        val keyValue = PsiTreeUtil.findChildOfType(file, MamlKeyValue::class.java)
        assertNotNull("Should find key-value element", keyValue)

        val key = keyValue?.key
        val value = keyValue?.value

        assertNotNull("KeyValue should have key", key)
        assertNotNull("KeyValue should have value", value)
    }

    @Test
    fun testKeyValueKeyIsNotNull() {
        val file = parse("{ key: \"value\" }")

        val keyValue = PsiTreeUtil.findChildOfType(file, MamlKeyValue::class.java)
        val key = keyValue?.key

        // Based on the @NotNull annotation in MamlKeyValue.java
        assertNotNull("Key should never be null in valid KeyValue", key)
    }

    @Test
    fun testKeyValueWithQuotedKey() {
        val file = parse("{ \"quoted-key\": \"value\" }")

        val keyValue = PsiTreeUtil.findChildOfType(file, MamlKeyValue::class.java)
        val key = keyValue?.key

        assertNotNull("Should have key for quoted string", key)
    }

    @Test
    fun testKeyValueWithIdentifierKey() {
        val file = parse("{ unquoted: \"value\" }")

        val keyValue = PsiTreeUtil.findChildOfType(file, MamlKeyValue::class.java)
        val key = keyValue?.key

        assertNotNull("Should have key for identifier", key)
    }

    // Key Element Tests

    @Test
    fun testKeyElementType() {
        val file = parse("{ key: \"value\" }")

        val key = PsiTreeUtil.findChildOfType(file, MamlKey::class.java)
        assertNotNull("Should find key element", key)
        assertTrue("Key should be PsiElement", key is PsiElement)
    }

    @Test
    fun testKeyIsNamedElement() {
        val file = parse("{ key: \"value\" }")

        val key = PsiTreeUtil.findChildOfType(file, MamlKey::class.java)
        assertTrue("Key should implement MamlNamedElement", key is MamlNamedElement)
    }

    // Value Element Tests

    @Test
    fun testValueElementType() {
        val file = parse("\"hello\"")

        val value = PsiTreeUtil.findChildOfType(file, MamlValue::class.java)
        assertNotNull("Should find value element", value)
        assertTrue("Value should be PsiElement", value is PsiElement)
    }

    @Test
    fun testValueIsValueElement() {
        val file = parse("123")

        val value = PsiTreeUtil.findChildOfType(file, MamlValue::class.java)
        assertTrue("Value should implement MamlValueElement", value is MamlValueElement)
    }

    @Test
    fun testStringValue() {
        val file = parse("\"hello\"")

        val value = PsiTreeUtil.findChildOfType(file, MamlValue::class.java)
        assertNotNull("Should find string value", value)
        assertNotNull("String value should have text", value?.text)
    }

    @Test
    fun testNumberValue() {
        val file = parse("123")

        val value = PsiTreeUtil.findChildOfType(file, MamlValue::class.java)
        assertNotNull("Should find number value", value)
        assertEquals("Number value text should be '123'", "123", value?.text)
    }

    @Test
    fun testBooleanValue() {
        val file = parse("true")

        val value = PsiTreeUtil.findChildOfType(file, MamlValue::class.java)
        assertNotNull("Should find boolean value", value)
        assertEquals("Boolean value text should be 'true'", "true", value?.text)
    }

    @Test
    fun testNullValue() {
        val file = parse("null")

        val value = PsiTreeUtil.findChildOfType(file, MamlValue::class.java)
        assertNotNull("Should find null value", value)
        assertEquals("Null value text should be 'null'", "null", value?.text)
    }

    @Test
    fun testObjectAsValue() {
        val file = parse("{ key: \"value\" }")

        val value = PsiTreeUtil.findChildOfType(file, MamlValue::class.java)
        val obj = PsiTreeUtil.findChildOfType(value, MamlObject::class.java)

        assertNotNull("Object should be wrapped in value", value)
        assertNotNull("Value should contain object", obj)
    }

    @Test
    fun testArrayAsValue() {
        val file = parse("[1, 2, 3]")

        val value = PsiTreeUtil.findChildOfType(file, MamlValue::class.java)
        val array = PsiTreeUtil.findChildOfType(value, MamlArray::class.java)

        assertNotNull("Array should be wrapped in value", value)
        assertNotNull("Value should contain array", array)
    }

    // Parent-Child Relationship Tests

    @Test
    fun testValueParentIsFile() {
        val file = parse("123")

        val value = PsiTreeUtil.findChildOfType(file, MamlValue::class.java)
        val parent = value?.parent

        assertTrue("Top-level value parent should be MamlFile", parent is MamlFile)
    }

    @Test
    fun testKeyValueParentIsMembers() {
        val file = parse("{ key: \"value\" }")

        val keyValue = PsiTreeUtil.findChildOfType(file, MamlKeyValue::class.java)
        val parent = keyValue?.parent

        assertTrue("KeyValue parent should be MamlMembers", parent is MamlMembers)
    }

    @Test
    fun testMembersParentIsObject() {
        val file = parse("{ key: \"value\" }")

        val members = PsiTreeUtil.findChildOfType(file, MamlMembers::class.java)
        val parent = members?.parent

        assertTrue("Members parent should be MamlObject", parent is MamlObject)
    }

    @Test
    fun testItemsParentIsArray() {
        val file = parse("[1, 2, 3]")

        val items = PsiTreeUtil.findChildOfType(file, MamlItems::class.java)
        val parent = items?.parent

        assertTrue("Items parent should be MamlArray", parent is MamlArray)
    }

    @Test
    fun testNestedValueParentChain() {
        val file = parse("{ key: [1, 2] }")

        val array = PsiTreeUtil.findChildOfType(file, MamlArray::class.java)

        // Array -> Value -> KeyValue -> Members -> Object -> Value -> File
        val arrayParent = array?.parent
        assertTrue("Array parent should be Value", arrayParent is MamlValue)

        val valueParent = arrayParent?.parent
        assertTrue("Value parent should be KeyValue", valueParent is MamlKeyValue)

        val keyValueParent = valueParent?.parent
        assertTrue("KeyValue parent should be Members", keyValueParent is MamlMembers)

        val membersParent = keyValueParent?.parent
        assertTrue("Members parent should be Object", membersParent is MamlObject)
    }

    // Sibling Navigation Tests

    @Test
    fun testKeyValueSiblings() {
        val file = parse("{ a: 1, b: 2, c: 3 }")

        val keyValues = PsiTreeUtil.findChildrenOfType(file, MamlKeyValue::class.java).toList()
        assertEquals("Should have 3 key-value pairs", 3, keyValues.size)

        // All should have the same parent (members element)
        val firstParent = keyValues[0].parent
        val secondParent = keyValues[1].parent
        val thirdParent = keyValues[2].parent

        assertEquals("All key-values should have same parent", firstParent, secondParent)
        assertEquals("All key-values should have same parent", firstParent, thirdParent)
    }

    @Test
    fun testArrayValueSiblings() {
        val file = parse("[1, 2, 3]")

        val values = PsiTreeUtil.findChildrenOfType(file, MamlValue::class.java).toList()
        // One for the array itself, three for the numbers
        assertTrue("Should have at least 3 value elements", values.size >= 3)

        // Find the items element
        val items = PsiTreeUtil.findChildOfType(file, MamlItems::class.java)
        val itemValues = PsiTreeUtil.findChildrenOfType(items, MamlValue::class.java).toList()

        assertEquals("Items should contain 3 values", 3, itemValues.size)
    }

    // Text Range Tests

    @Test
    fun testObjectTextRange() {
        val file = parse("{ key: \"value\" }")

        val obj = PsiTreeUtil.findChildOfType(file, MamlObject::class.java)
        val textRange = obj?.textRange

        assertNotNull("Object should have text range", textRange)
        assertTrue("Text range should have positive length", textRange!!.length > 0)
    }

    @Test
    fun testKeyValueTextRange() {
        val file = parse("{ key: \"value\" }")

        val keyValue = PsiTreeUtil.findChildOfType(file, MamlKeyValue::class.java)
        val textRange = keyValue?.textRange

        assertNotNull("KeyValue should have text range", textRange)
        assertTrue("Text range should span key and value", textRange!!.length > 0)
    }

    @Test
    fun testValueTextRangeMatchesContent() {
        val file = parse("\"hello\"")

        val value = PsiTreeUtil.findChildOfType(file, MamlValue::class.java)
        val text = value?.text

        assertEquals("Value text should match parsed content", "\"hello\"", text)
    }

    // Element Offset Tests

    @Test
    fun testNestedElementOffsets() {
        val file = parse("{ key: \"value\" }")

        val obj = PsiTreeUtil.findChildOfType(file, MamlObject::class.java)
        val keyValue = PsiTreeUtil.findChildOfType(file, MamlKeyValue::class.java)

        val objOffset = obj?.textRange?.startOffset ?: 0
        val kvOffset = keyValue?.textRange?.startOffset ?: 0

        assertTrue(
            "KeyValue offset should be after Object opening brace",
            kvOffset > objOffset
        )
    }

    // Finding Elements by Type Tests

    @Test
    fun testFindAllObjectsInComplexStructure() {
        val file = parse("{ a: { b: { c: \"value\" } } }")

        val objects = PsiTreeUtil.findChildrenOfType(file, MamlObject::class.java)

        assertEquals("Should find 3 nested objects", 3, objects.size)
    }

    @Test
    fun testFindAllArraysInComplexStructure() {
        val file = parse("{ key: [1, [2, 3], 4] }")

        val arrays = PsiTreeUtil.findChildrenOfType(file, MamlArray::class.java)

        assertEquals("Should find 2 arrays (outer and nested)", 2, arrays.size)
    }

    @Test
    fun testFindAllValuesInMixedStructure() {
        val file = parse("{ a: 1, b: \"string\", c: true }")

        val values = PsiTreeUtil.findChildrenOfType(file, MamlValue::class.java)

        // One for the object, three for the literal values
        assertTrue("Should have at least 4 values", values.size >= 4)
    }

    // Complex Structure Tests

    @Test
    fun testDeeplyNestedPsiStructure() {
        val file = parse("{ a: { b: { c: { d: \"deep\" } } } }")

        // Navigate down the structure
        val obj1 = PsiTreeUtil.findChildOfType(file, MamlObject::class.java)
        assertNotNull("Should find first object", obj1)

        val kv1 = PsiTreeUtil.findChildOfType(obj1, MamlKeyValue::class.java)
        assertNotNull("Should find first key-value", kv1)

        val val1 = kv1?.value
        assertNotNull("Should find first value", val1)

        val obj2 = PsiTreeUtil.findChildOfType(val1, MamlObject::class.java)
        assertNotNull("Should find second nested object", obj2)

        // Verify parent relationships work both ways
        assertNotNull("obj1 should not be null", obj1)
        assertNotNull("obj2 should not be null", obj2)
        assertTrue("Child object's ancestor should be parent object",
            PsiTreeUtil.isAncestor(obj1!!, obj2!!, true))
    }

    @Test
    fun testMixedArrayAndObjectNesting() {
        val file = parse("{ items: [{ name: \"item1\" }, { name: \"item2\" }] }")

        val outerObj = PsiTreeUtil.findChildOfType(file, MamlObject::class.java)
        val array = PsiTreeUtil.findChildOfType(file, MamlArray::class.java)
        val innerObjects = PsiTreeUtil.findChildrenOfType(array, MamlObject::class.java)

        assertNotNull("Should have outer object", outerObj)
        assertNotNull("Should have array", array)
        assertEquals("Array should contain 2 objects", 2, innerObjects.size)

        // Verify containment
        assertNotNull("outerObj should not be null", outerObj)
        assertNotNull("array should not be null", array)
        assertTrue("Outer object should contain array",
            PsiTreeUtil.isAncestor(outerObj!!, array!!, true))
    }
}