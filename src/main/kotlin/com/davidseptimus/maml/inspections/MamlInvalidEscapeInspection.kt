package com.davidseptimus.maml.inspections

import com.davidseptimus.maml.MamlBundle
import com.davidseptimus.maml.psi.MamlTypes
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor

/**
 * Inspection that detects invalid escape sequences in MAML strings.
 */
class MamlInvalidEscapeInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element.node.elementType != MamlTypes.STRING) return

                val text = element.text
                if (!text.startsWith("\"") || text.length < 2) return

                // Process the string content (excluding quotes)
                val content = text.substring(1, if (text.endsWith("\"")) text.length - 1 else text.length)
                var i = 0

                while (i < content.length) {
                    if (content[i] == '\\' && i + 1 < content.length) {
                        val nextChar = content[i + 1]

                        if (nextChar !in VALID_ESCAPES) {
                            // Invalid escape sequence found
                            val startOffset = 1 + i // +1 for opening quote (relative to element)
                            val endOffset = startOffset + 2

                            holder.registerProblem(
                                element,
                                TextRange(startOffset, endOffset),
                                MamlBundle.message("inspection.invalid.escape.message", "\\$nextChar"),
                                EscapeBackslashQuickFix()
                            )

                            i += 2 // Skip the invalid escape
                        } else {
                            // Valid escape - calculate its length
                            val length = if (nextChar == 'u' && i + 5 < content.length) {
                                // Unicode escape: \uXXXX
                                6
                            } else {
                                // Regular escape: \n, \\, etc.
                                2
                            }
                            // Skip the entire valid escape sequence
                            i += length
                        }
                    } else {
                        i++
                    }
                }
            }
        }
    }

    /**
     * Quick fix that escapes the backslash in an invalid escape sequence.
     */
    private class EscapeBackslashQuickFix : LocalQuickFix {
        override fun getFamilyName(): String =
            MamlBundle.message("inspection.invalid.escape.quickfix")

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val element = descriptor.psiElement
            val text = element.text
            val range = descriptor.textRangeInElement

            // Extract the invalid escape sequence
            val invalidEscape = text.substring(range.startOffset, range.endOffset)

            // Escape the backslash: \x becomes \\x
            val fixed = invalidEscape.replace("\\", "\\\\")

            // Replace in the full text
            val newText = text.substring(0, range.startOffset) +
                    fixed +
                    text.substring(range.endOffset)

            // Use element factory to create replacement
            val tempFile = com.davidseptimus.maml.psi.MamlElementFactory.createFile(project, newText)
            val newElement = tempFile.firstChild?.firstChild

            if (newElement != null) {
                element.replace(newElement)
            }
        }
    }
}

private val VALID_ESCAPES = setOf('\\', '"', 'n', 'r', 't', 'b', 'f', '/', 'u')