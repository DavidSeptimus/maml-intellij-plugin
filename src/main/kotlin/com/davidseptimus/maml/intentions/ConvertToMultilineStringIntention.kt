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

        // Unescape escape sequences for multiline, preserving whitespace in the string content
        var result = StringBuilder()
        var i = 0
        while (i < content.length) {
            if (content[i] == '\\' && i + 1 < content.length) {
                when (content[i + 1]) {
                    'n' -> result.append('\n')
                    't' -> result.append('\t')
                    'r' -> result.append('\r')
                    '"' -> result.append('"')
                    '\\' -> result.append('\\')
                    else -> {
                        // Keep unknown escapes as-is
                        result.append(content[i])
                        result.append(content[i + 1])
                    }
                }
                i += 2
            } else {
                result.append(content[i])
                i++
            }
        }
        val unescaped = result.toString()

        // Create multiline string with triple quotes
        // If original string has no newlines, keep it on same line
        // If it has newlines, start content on a new line after opening """
        val multilineText = if (unescaped.contains('\n')) {
            "\"\"\"\n$unescaped\"\"\""
        } else {
            "\"\"\"$unescaped\"\"\""
        }

        // Create a temporary file with a value to extract the multiline string from
        val tempFile = MamlElementFactory.createFile(project, multilineText)
        val newStringElement = tempFile.firstChild?.firstChild

        if (newStringElement != null) {
            element.replace(newStringElement)
        }
    }
}