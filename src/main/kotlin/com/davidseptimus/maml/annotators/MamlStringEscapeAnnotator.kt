package com.davidseptimus.maml.annotators

import com.davidseptimus.maml.MamlBundle
import com.davidseptimus.maml.highlighting.MamlTokenAttributes
import com.davidseptimus.maml.lang.psi.MamlTypes
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

// Valid escape sequences in MAML single-line strings
private val VALID_ESCAPES = setOf('\\', '"', 'n', 'r', 't', 'u')

/**
 * Annotator that validates escape sequences in MAML strings and highlights them.
 * spec: https://maml.dev/spec/v0.1#string
 */
class MamlStringEscapeAnnotator : Annotator, DumbAware {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element.node.elementType) {
            MamlTypes.STRING -> annotateRegularString(element, holder)
            MamlTypes.MULTILINE_STRING -> annotateMultilineString(element, holder)
        }
    }

    /**
     * Annotates escape sequences in regular (single-line) strings.
     */
    private fun annotateRegularString(element: PsiElement, holder: AnnotationHolder) {
        val text = element.text
        if (!text.startsWith("\"") || text.length < 2) return

        val content = text.removeSurrounding("\"")
        var i = 0

        while (i < content.length) {
            if (content[i] == '\\' && i + 1 < content.length) {
                // Check if it's a valid escape
                when (val nextChar = content[i + 1]) {
                    !in VALID_ESCAPES -> {
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
                    }

                    'u' -> {
                        // Validate and highlight unicode scalar escape sequence
                        val escapeResult = validateUnicodeScalar(content, i, element.textRange.startOffset + 1, holder)
                        if (escapeResult.isValid) {
                            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                                .range(TextRange(escapeResult.startOffset, escapeResult.endOffset))
                                .textAttributes(MamlTokenAttributes.VALID_ESCAPE)
                                .create()
                        }
                        i = escapeResult.nextIndex
                    }

                    else -> {
                        // Highlight compact escape sequence
                        val startOffset = element.textRange.startOffset + 1 + i
                        val endOffset = startOffset + 2

                        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                            .range(TextRange(startOffset, endOffset))
                            .textAttributes(MamlTokenAttributes.VALID_ESCAPE)
                            .create()

                        i += 2
                    }
                }
            } else {
                i++
            }
        }
    }

    /**
     * Annotates escape sequences in multiline (raw) strings.
     * In multiline strings, the only valid escape is \""" (escaped triple-quote).
     */
    private fun annotateMultilineString(element: PsiElement, holder: AnnotationHolder) {
        val text = element.text
        if (!text.startsWith("\"\"\"") || text.length < 6) return

        // Remove the opening and closing triple quotes
        val content = text.removePrefix("\"\"\"").removeSuffix("\"\"\"")
        var i = 0

        while (i < content.length) {
            if (content[i] == '\\' && i + 3 < content.length) {
                // Check for escaped triple-quote: \"""
                if (content[i + 1] == '"' && content[i + 2] == '"' && content[i + 3] == '"') {
                    // Valid escaped triple-quote
                    val startOffset = element.textRange.startOffset + 3 + i // +3 for opening """
                    val endOffset = startOffset + 4 // \"""

                    holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                        .range(TextRange(startOffset, endOffset))
                        .textAttributes(MamlTokenAttributes.VALID_ESCAPE)
                        .create()

                    i += 4 // Skip \"""
                } else {
                    i++
                }
            } else {
                i++
            }
        }
    }

    private data class EscapeResult(
        val isValid: Boolean,
        val startOffset: Int,
        val endOffset: Int,
        val nextIndex: Int
    )

    /**
     * Validates a unicode scalar escape sequence in the form \u{XXXXXX}
     * where XXXXXX is 1-6 hexadecimal digits representing a valid Unicode code point.
     *
     * @param content The string content being analyzed
     * @param index The current index pointing to the backslash
     * @param contentStartOffset The absolute offset of the string content start (after opening quote)
     * @param holder The annotation holder for reporting errors
     * @return EscapeResult containing validation result and offsets
     */
    private fun validateUnicodeScalar(
        content: String,
        index: Int,
        contentStartOffset: Int,
        holder: AnnotationHolder
    ): EscapeResult {
        val startOffset = contentStartOffset + index

        // Check for opening brace
        if (index + 2 >= content.length || content[index + 2] != '{') {
            val endOffset = startOffset + 2
            val invalidContent = content.substring(index, minOf(index + 2, content.length))
            holder.newAnnotation(
                HighlightSeverity.ERROR,
                MamlBundle.message("annotator.invalid.escape.unicode.missing.open.brace", invalidContent)
            )
                .range(TextRange(startOffset, endOffset))
                .textAttributes(MamlTokenAttributes.INVALID_ESCAPE)
                .create()
            return EscapeResult(false, startOffset, endOffset, index + 2)
        }

        // Find closing brace, but stop at whitespace or quotes (these indicate the escape is malformed)
        var closingBraceIndex = index + 3
        while (closingBraceIndex < content.length) {
            val ch = content[closingBraceIndex]
            if (ch == '}') {
                break
            }
            if (ch.isWhitespace() || ch == '"' || ch == '\'') {
                break
            }
            closingBraceIndex++
        }

        // Check if we found a closing brace
        val foundClosingBrace = closingBraceIndex < content.length && content[closingBraceIndex] == '}'

        if (!foundClosingBrace) {
            // No closing brace found - highlight up to where we stopped (not including whitespace/quotes)
            val errorEndOffset = startOffset + (closingBraceIndex - index)
            val invalidContent = content.substring(index, closingBraceIndex)
            holder.newAnnotation(
                HighlightSeverity.ERROR,
                MamlBundle.message("annotator.invalid.escape.unicode.missing.close.brace", invalidContent)
            )
                .range(TextRange(startOffset, errorEndOffset))
                .textAttributes(MamlTokenAttributes.INVALID_ESCAPE)
                .create()
            return EscapeResult(false, startOffset, errorEndOffset, closingBraceIndex)
        }

        // Extract hex digits
        val hexDigits = content.substring(index + 3, closingBraceIndex)
        val escapeLength = closingBraceIndex - index + 1 // \u{...} total length
        val endOffset = startOffset + escapeLength

        // Validate hex digits
        if (hexDigits.isEmpty()) {
            holder.newAnnotation(
                HighlightSeverity.ERROR,
                MamlBundle.message("annotator.invalid.escape.unicode.empty")
            )
                .range(TextRange(startOffset, endOffset))
                .textAttributes(MamlTokenAttributes.INVALID_ESCAPE)
                .create()
            return EscapeResult(false, startOffset, endOffset, closingBraceIndex + 1)
        }

        if (hexDigits.length > 6) {
            holder.newAnnotation(
                HighlightSeverity.ERROR,
                MamlBundle.message("annotator.invalid.escape.unicode.too.many.digits", hexDigits)
            )
                .range(TextRange(startOffset, endOffset))
                .textAttributes(MamlTokenAttributes.INVALID_ESCAPE)
                .create()
            return EscapeResult(false, startOffset, endOffset, closingBraceIndex + 1)
        }

        if (!hexDigits.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }) {
            holder.newAnnotation(
                HighlightSeverity.ERROR,
                MamlBundle.message("annotator.invalid.escape.unicode.invalid.hex", hexDigits)
            )
                .range(TextRange(startOffset, endOffset))
                .textAttributes(MamlTokenAttributes.INVALID_ESCAPE)
                .create()
            return EscapeResult(false, startOffset, endOffset, closingBraceIndex + 1)
        }

        // Parse code point value
        val codePoint = hexDigits.toInt(16)

        // Validate Unicode code point range (0x0 to 0x10FFFF)
        if (codePoint > 0x10FFFF) {
            holder.newAnnotation(
                HighlightSeverity.ERROR,
                MamlBundle.message("annotator.invalid.escape.unicode.code.point.too.large", hexDigits)
            )
                .range(TextRange(startOffset, endOffset))
                .textAttributes(MamlTokenAttributes.INVALID_ESCAPE)
                .create()
            return EscapeResult(false, startOffset, endOffset, closingBraceIndex + 1)
        }

        // Check for surrogate pair range (0xD800 to 0xDFFF) - invalid in Unicode scalars
        if (codePoint in 0xD800..0xDFFF) {
            holder.newAnnotation(
                HighlightSeverity.ERROR,
                MamlBundle.message("annotator.invalid.escape.unicode.surrogate", hexDigits)
            )
                .range(TextRange(startOffset, endOffset))
                .textAttributes(MamlTokenAttributes.INVALID_ESCAPE)
                .create()
            return EscapeResult(false, startOffset, endOffset, closingBraceIndex + 1)
        }

        // Valid unicode scalar
        return EscapeResult(true, startOffset, endOffset, closingBraceIndex + 1)
    }
}