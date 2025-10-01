package com.davidseptimus.maml.intentions

import com.davidseptimus.maml.MamlBundle
import com.davidseptimus.maml.psi.MamlElementFactory
import com.davidseptimus.maml.psi.MamlTypes
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType

/**
 * Intention action to convert a multiline string to a single-line string.
 */
class ConvertToSingleLineStringIntention : PsiElementBaseIntentionAction(), IntentionAction {

    override fun getFamilyName(): String =
        MamlBundle.message("intention.convert.to.single.line.string.family")

    override fun getText(): String =
        MamlBundle.message("intention.convert.to.single.line.string.text")

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        // Available only on MULTILINE_STRING tokens
        return element.elementType == MamlTypes.MULTILINE_STRING
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        if (element.elementType != MamlTypes.MULTILINE_STRING) return

        // Get the multiline string content without the triple quotes
        val text = element.text
        val content = text.substring(3, text.length - 3) // Remove opening and closing triple quotes

        // Escape special characters for single-line string, preserving all whitespace in content
        val result = StringBuilder()
        for (char in content) {
            when (char) {
                '\\' -> result.append("\\\\")
                '"' -> result.append("\\\"")
                '\n' -> result.append("\\n")
                '\r' -> result.append("\\r")
                '\t' -> result.append("\\t")
                '\b' -> result.append("\\b")
                else -> result.append(char)
            }
        }
        val escaped = result.toString()

        // Create single-line string with regular quotes
        val singleLineText = "\"$escaped\""

        // Create a temporary file with a value to extract the string from
        val tempFile = MamlElementFactory.createFile(project, singleLineText)
        val newStringElement = tempFile.firstChild?.firstChild

        if (newStringElement != null) {
            element.replace(newStringElement)
        }
    }
}