package com.davidseptimus.maml.editor

import com.davidseptimus.maml.lang.psi.MamlTypes
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import java.awt.Font

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

        // Get the editor font to check glyph support
        val editorFont = getEditorFont()

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
                            if (shouldFoldCodePoint(codePoint, editorFont)) {
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
     * Returns false for whitespace or if the font cannot display it.
     */
    private fun shouldFoldCodePoint(codePoint: Int, editorFont: Font?): Boolean {
        if (Character.isWhitespace(codePoint)) return false
        return editorFont?.canDisplay(codePoint) == true
    }

    /**
     * Get the editor font for checking glyph support.
     * Returns null if the font cannot be determined.
     */
    private fun getEditorFont(): Font? {
        return try {
            val editorColorsManager = com.intellij.openapi.editor.colors.EditorColorsManager.getInstance()
            val scheme = editorColorsManager.globalScheme
            scheme.getFont(EditorFontType.PLAIN)
        } catch (_: Exception) {
            // If we can't get the font, don't create folding regions
            null
        }
    }
}