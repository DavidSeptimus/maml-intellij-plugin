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
 * Intention action to convert a single-line string to a multiline string.
 */
class ConvertToMultilineStringIntention : PsiElementBaseIntentionAction(), IntentionAction {

    override fun getFamilyName(): String =
        MamlBundle.message("intention.convert.to.multiline.string.family")

    override fun getText(): String =
        MamlBundle.message("intention.convert.to.multiline.string.text")

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        // Available only on STRING tokens (not MULTILINE_STRING)
        return element.elementType == MamlTypes.STRING
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        if (element.elementType != MamlTypes.STRING) return

        // Get the string content without the quotes
        val text = element.text
        val content = text.substring(1, text.length - 1) // Remove opening and closing quotes

        // Unescape common escape sequences for multiline
        // In multiline strings, we don't need to escape newlines
        val unescaped = content
            .replace("\\n", "\n")
            .replace("\\t", "\t")
            .replace("\\\"", "\"")  // Quotes don't need escaping in multiline

        // Get the indentation of the current line
        val document = editor?.document
        val lineNumber = document?.getLineNumber(element.textRange.startOffset) ?: 0
        val lineStartOffset = document?.getLineStartOffset(lineNumber) ?: 0
        val lineEndOffset = element.textRange.startOffset
        val linePrefix = document?.getText(com.intellij.openapi.util.TextRange(lineStartOffset, lineEndOffset)) ?: ""
        val indentation = linePrefix.takeWhile { it.isWhitespace() }

        // Start content on new line with preserved indentation
        val multilineText = "\"\"\"\n$indentation$unescaped\n$indentation\"\"\""

        // Create a temporary file with a value to extract the multiline string from
        val tempFile = MamlElementFactory.createFile(project, multilineText)
        val newStringElement = tempFile.firstChild?.firstChild

        if (newStringElement != null) {
            element.replace(newStringElement)
        }
    }
}