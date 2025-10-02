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