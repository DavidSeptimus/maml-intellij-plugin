package com.davidseptimus.maml.lang.psi

import com.davidseptimus.maml.lang.INSTANCE
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory

/**
 * Factory for creating MAML PSI elements.
 * This is used for refactoring operations like rename.
 */
object MamlElementFactory {

    /**
     * Creates a new key element with the given text.
     *
     * @param project the project context
     * @param text the key text (with or without quotes)
     * @return a new MamlKey element
     */
    fun createKey(project: Project, text: String): MamlKey {
        // Create a temporary MAML file with a key-value pair
        val fileText = "{ $text: null }"
        val file = createFile(project, fileText)

        // Extract the key from the created file
        val value = file.firstChild as? MamlValue
        val obj = value?.`object`
        val members = obj?.members
        val keyValue = members?.keyValueList?.firstOrNull()
        return keyValue?.key ?: throw IllegalStateException("Failed to create key element")
    }

    /**
     * Creates a new key-value pair with the given key and value.
     *
     * @param project the project context
     * @param keyText the key text (with or without quotes)
     * @param valueText the value text
     * @return a new MamlKeyValue element
     */
    fun createKeyValue(project: Project, keyText: String, valueText: String): MamlKeyValue {
        val fileText = "{ $keyText: $valueText }"
        val file = createFile(project, fileText)

        val value = file.firstChild as? MamlValue
        val obj = value?.`object`
        val members = obj?.members
        val keyValue = members?.keyValueList?.firstOrNull()
        return keyValue ?: throw IllegalStateException("Failed to create key-value element")
    }

    /**
     * Creates a new members element with a single key-value pair.
     *
     * @param project the project context
     * @param keyText the key text
     * @param valueText the value text
     * @return a new MamlMembers element
     */
    fun createMembers(project: Project, keyText: String, valueText: String): MamlMembers {
        val fileText = "{ $keyText: $valueText }"
        val file = createFile(project, fileText)

        val value = file.firstChild as? MamlValue
        val obj = value?.`object`
        val members = obj?.members
        return members ?: throw IllegalStateException("Failed to create members element")
    }

    /**
     * Creates a newline whitespace element.
     *
     * @param project the project context
     * @return a newline PsiElement
     */
    fun createNewline(project: Project): com.intellij.psi.PsiElement {
        val file = createFile(project, "\n")
        return file.firstChild
    }

    /**
     * Creates a temporary MAML file with the given text.
     *
     * @param project the project context
     * @param text the file content
     * @return a MamlFile
     */
    fun createFile(project: Project, text: String): MamlFile {
        val name = "dummy.maml"
        return PsiFileFactory.getInstance(project)
            .createFileFromText(name, INSTANCE, text) as MamlFile
    }
}