package com.davidseptimus.maml.annotators

import com.davidseptimus.maml.MamlBundle
import com.davidseptimus.maml.highlighting.MamlTokenAttributes
import com.davidseptimus.maml.lang.psi.MamlTypes
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

/**
 * Annotator that validates escape sequences in MAML strings and highlights them.
 */
class MamlStringEscapeAnnotator : Annotator {

    companion object {
        // Valid escape sequences in MAML single-line strings
        private val VALID_ESCAPES = setOf('\\', '"', 'n', 'r', 't', 'b', 'f', '/', 'u')
    }

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element.node.elementType != MamlTypes.STRING) return

        val text = element.text
        if (!text.startsWith("\"") || text.length < 2) return

        // Process the string content (excluding quotes)
        val content = text.substring(1, if (text.endsWith("\"")) text.length - 1 else text.length)
        var i = 0

        while (i < content.length) {
            if (content[i] == '\\' && i + 1 < content.length) {
                val nextChar = content[i + 1]

                // Check if it's a valid escape
                if (nextChar !in VALID_ESCAPES) {
                    // Invalid escape sequence
                    val startOffset = element.textRange.startOffset + 1 + i // +1 for opening quote
                    val endOffset = startOffset + 2 // backslash + next char

                    holder.newAnnotation(
                        HighlightSeverity.ERROR,
                        MamlBundle.message("annotator.invalid.escape", "\\$nextChar")
                    )
                        .range(TextRange(startOffset, endOffset))
                        .textAttributes(MamlTokenAttributes.INVALID_ESCAPE)
                        .create()

                    i += 2 // Skip backslash and next char
                } else {
                    // Valid escape - highlight it
                    val startOffset = element.textRange.startOffset + 1 + i
                    val length = if (nextChar == 'u' && i + 5 < content.length) {
                        // Unicode escape: \uXXXX (6 characters total)
                        6
                    } else {
                        // Regular escape: \n, \t, etc. (2 characters)
                        2
                    }
                    val endOffset = startOffset + length

                    holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                        .range(TextRange(startOffset, endOffset))
                        .textAttributes(MamlTokenAttributes.VALID_ESCAPE)
                        .create()

                    // If this was an escaped backslash (\\), the next character after the
                    // escape sequence is not part of an escape
                    i += length
                }
            } else {
                i++
            }
        }
    }
}
