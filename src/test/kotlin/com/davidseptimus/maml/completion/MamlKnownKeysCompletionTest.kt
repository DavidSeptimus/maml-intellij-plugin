package com.davidseptimus.maml.completion

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MamlKnownKeysCompletionTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String = "src/test/testData/completion/knownKeys"

    fun testKnownKeysCompletion() {
        myFixture.testCompletionVariants("knownKeysCompletion.maml", "neat")
    }

    fun testKnownKeysFromDifferentObjects() {
        myFixture.testCompletionVariants("knownKeysFromDifferentObjects.maml", "email", "name", "settings", "user")
    }

    fun testNoDuplicateKeyCompletion() {
        myFixture.configureByFile("noDuplicateKeyCompletion.maml")
        val completions = myFixture.completeBasic()

        // 'name' and 'age' should not be suggested again since they already exist in current object
        val lookupStrings = completions?.map { it.lookupString } ?: emptyList()
        assertFalse("'name' should not appear in completions", lookupStrings.contains("name"))
        assertFalse("'age' should not appear in completions", lookupStrings.contains("age"))
    }

    fun testExcludeUniqueCurrentKey() {
        myFixture.testCompletionVariants("excludeUniqueCurrentKey.maml", "uniqueKeyWithSuffix", "uniqueKeyWithSuffix2")
    }

    fun testIncludeRepeatedCurrentKey() {
        myFixture.testCompletionVariants("includeRepeatedCurrentKey.maml", "repeated", "repeatedWithSuffix")
    }

    fun testNoKeyCompletionInValuePosition() {
        myFixture.testCompletionVariants("noKeyCompletionInValuePosition.maml", "true", "false", "null")
    }
}