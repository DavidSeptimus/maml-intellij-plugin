package com.davidseptimus.maml.editor

import com.davidseptimus.maml.lang.psi.MamlTypes
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

/**
 * Folding builder that allows folding Unicode escape sequences (\u{XXXXXX}) to their character representation.
 * Only creates folding regions for characters that can be displayed by the editor's font.
 */
class MamlUnicodeFoldingBuilder : FoldingBuilderEx(), DumbAware {

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()

        // Check if Unicode escapes should be grouped together
        val settings = MamlCodeFoldingSettings.getInstance()
        val group = if (settings.isGroupUnicodeEscapes) {
            FoldingGroup.newGroup("maml-unicode")
        } else {
            null
        }

        // Find all STRING elements
        PsiTreeUtil.findChildrenOfType(root, PsiElement::class.java)
            .filter { it.node.elementType == MamlTypes.STRING }
            .forEach { stringElement ->
                val text = stringElement.text
                if (!text.startsWith("\"") || text.length < 2) return@forEach

                val content = text.removeSurrounding("\"")
                var i = 0

                while (i < content.length) {
                    // Skip escaped backslashes
                    if (i + 1 < content.length && content[i] == '\\' && content[i + 1] == '\\') {
                        i += 2
                        continue
                    }

                    // Check for start of Unicode escape sequence
                    if (!content.startsWith("\\u{", i)) {
                        i++
                        continue
                    }

                    // Found start of sequence - collect consecutive Unicode escapes
                    val sequenceStart = i
                    val allCodePoints = mutableListOf<Int>()
                    var hasRenderableCodePoint = false

                    while (content.startsWith("\\u{", i)) {
                        val closingBraceIndex = content.indexOf('}', i + 3)

                        // Stop sequence if malformed (no closing brace)
                        if (closingBraceIndex == -1) break

                        val hexDigits = content.substring(i + 3, closingBraceIndex)
                        val codePoint = parseUnicodeEscape(hexDigits)

                        if (codePoint != null) {
                            allCodePoints.add(codePoint)
                            if (shouldFoldCodePoint(codePoint)) {
                                hasRenderableCodePoint = true
                            }
                        }

                        i = closingBraceIndex + 1
                    }

                    // Create folding region if we have at least one renderable code point
                    if (hasRenderableCodePoint && allCodePoints.isNotEmpty()) {
                        val startOffset = stringElement.textRange.startOffset + 1 + sequenceStart
                        val endOffset = stringElement.textRange.startOffset + 1 + i
                        val unicodeString = String(allCodePoints.toIntArray(), 0, allCodePoints.size)

                        descriptors.add(
                            FoldingDescriptor(
                                stringElement.node,
                                com.intellij.openapi.util.TextRange(startOffset, endOffset),
                                group,
                                unicodeString
                            )
                        )
                    }

                    // Advance at least one character if we didn't move
                    if (i == sequenceStart) {
                        i++
                    }
                }
            }

        return descriptors.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String? {
        // This is handled by the FoldingDescriptor constructor
        return null
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        // Check the IDE's folding settings
        return MamlCodeFoldingSettings.getInstance().isCollapseUnicodeEscapes
    }

    /**
     * Parse a hex string into a valid Unicode code point.
     * Returns null if the escape is invalid.
     */
    private fun parseUnicodeEscape(hexDigits: String): Int? {
        if (hexDigits.isEmpty() || hexDigits.length > 6) return null
        if (!hexDigits.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }) return null

        val codePoint = hexDigits.toInt(16)
        // Valid Unicode code point range (excluding surrogates)
        return if (codePoint <= 0x10FFFF && codePoint !in 0xD800..0xDFFF) codePoint else null
    }

    /**
     * Check if a code point should be folded.
     * Returns false for whitespace, control characters, and other non-printable categories.
     *
     * Note: We don't use Font.canDisplay() because it has inconsistent cross-platform behavior.
     * The same font and code point can return true on macOS but false on Windows/Linux.
     * Instead, we rely on IntelliJ's font fallback system to handle rendering.
     */
    private fun shouldFoldCodePoint(codePoint: Int): Boolean {
        if (Character.isWhitespace(codePoint)) return false

        val charType = Character.getType(codePoint)

        when {
            charType == Character.CONTROL.toInt() -> return false
            charType == Character.LINE_SEPARATOR.toInt() -> return false
            charType == Character.PARAGRAPH_SEPARATOR.toInt() -> return false
            charType == Character.SPACE_SEPARATOR.toInt() -> return false
            charType == Character.FORMAT.toInt() -> return false
            charType == Character.UNASSIGNED.toInt() -> return false
            charType == Character.PRIVATE_USE.toInt() -> return false
            charType == Character.SURROGATE.toInt() -> return false
            charType == Character.NON_SPACING_MARK.toInt() -> return false
            charType == Character.ENCLOSING_MARK.toInt() -> return false
            charType == Character.COMBINING_SPACING_MARK.toInt() -> return false
        }

        return true
    }
}