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

        // Split into lines and find first non-empty line to preserve its leading whitespace
        val lines = content.lines()
        val firstTextLine = lines.firstOrNull { it.isNotBlank() } ?: ""
        val leadingWhitespace = firstTextLine.takeWhile { it.isWhitespace() }

        // Escape special characters for single-line string
        val escaped = content
            .replace("\\", "\\\\")  // Escape backslashes first
            .replace("\"", "\\\"")  // Escape quotes
            .replace("\n", "\\n")   // Escape newlines
            .replace("\r", "\\r")   // Escape carriage returns
            .replace("\t", "\\t")   // Escape tabs

        // Create single-line string with regular quotes, preserving leading whitespace
        val singleLineText = "\"$escaped\""

        // Create a temporary file with a value to extract the string from
        val tempFile = MamlElementFactory.createFile(project, singleLineText)
        val newStringElement = tempFile.firstChild?.firstChild

        if (newStringElement != null) {
            element.replace(newStringElement)
        }
    }
}