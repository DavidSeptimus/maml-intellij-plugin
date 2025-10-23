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
                    if (content[i] == '\\' && i + 1 < content.length) {
                        // Check if this is an escaped backslash
                        if (content[i + 1] == '\\') {
                            // Skip the escaped backslash
                            i += 2
                            continue
                        }

                        // Check for Unicode escape
                        if (content[i + 1] == 'u') {
                            // Check for opening brace
                            if (i + 2 < content.length && content[i + 2] == '{') {
                                // Find closing brace
                                var closingBraceIndex = i + 3
                                while (closingBraceIndex < content.length && content[closingBraceIndex] != '}') {
                                    closingBraceIndex++
                                }

                                if (closingBraceIndex < content.length) {
                                    // Extract and validate hex digits
                                    val hexDigits = content.substring(i + 3, closingBraceIndex)
                                    if (hexDigits.isNotEmpty() &&
                                        hexDigits.length <= 6 &&
                                        hexDigits.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
                                    ) {

                                        val codePoint = hexDigits.toInt(16)
                                        // Valid Unicode code point range (excluding surrogates)
                                        if (codePoint <= 0x10FFFF && codePoint !in 0xD800..0xDFFF) {
                                            // Skip whitespace characters - they won't be visible anyway
                                            if (!Character.isWhitespace(codePoint)) {
                                                // Only create folding if the font can display this character
                                                if (editorFont?.canDisplay(codePoint) == true) {
                                                    // Calculate absolute offsets in the document
                                                    val startOffset =
                                                        stringElement.textRange.startOffset + 1 + i // +1 for opening quote
                                                    val endOffset =
                                                        stringElement.textRange.startOffset + 1 + closingBraceIndex + 1 // +1 for closing brace

                                                    val unicodeChar = String(intArrayOf(codePoint), 0, 1)
                                                    descriptors.add(
                                                        FoldingDescriptor(
                                                            stringElement.node,
                                                            com.intellij.openapi.util.TextRange(startOffset, endOffset),
                                                            group,
                                                            unicodeChar
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    i = closingBraceIndex + 1
                                } else {
                                    i++
                                }
                            } else {
                                i++
                            }
                        } else {
                            i++
                        }
                    } else {
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