package com.davidseptimus.maml.formatter

import com.davidseptimus.maml.lang.MamlLanguage
import com.davidseptimus.maml.lang.psi.MamlTypes
import com.intellij.application.options.CodeStyle
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.TokenType
import com.intellij.psi.impl.source.codeStyle.PreFormatProcessor
import com.intellij.psi.impl.source.tree.LeafPsiElement

/**
 * Pre-format processor that removes all commas from MAML code.
 * Replaces commas with a space if needed to maintain value separation.
 *
 * For example:
 * ```
 * [1,2,3]  -> [1 2 3]
 * [1, 2, 3] -> [1 2 3]
 * ```
 */
class MamlCommaRemover : PreFormatProcessor {

    override fun process(element: ASTNode, range: TextRange): TextRange {
        val psi = element.psi
        val file = psi.containingFile ?: return range

        if (!file.language.isKindOf(MamlLanguage)) {
            return range
        }

        val customSettings = CodeStyle.getCustomSettings(file, MamlCodeStyleSettings::class.java)
        if (!customSettings.REMOVE_COMMAS) {
            return range
        }

        removeCommas(element)

        return TextRange.create(element.startOffset, element.startOffset + element.textLength)
    }

    private fun removeCommas(node: ASTNode) {
        var child = node.firstChildNode
        while (child != null) {
            val next = child.treeNext

            if (child.elementType == MamlTypes.COMMA) {
                handleCommaRemoval(node, child)
            } else {
                // Recursively process children
                removeCommas(child)
            }

            child = next
        }
    }

    private fun handleCommaRemoval(parent: ASTNode, commaNode: ASTNode) {
        val nextSibling = commaNode.treeNext
        val prevSibling = commaNode.treePrev

        // Check if we need to insert a space to maintain separation
        val needsSpace = when {
            nextSibling == null -> false
            nextSibling.elementType == TokenType.WHITE_SPACE -> false
            prevSibling == null -> false
            prevSibling.elementType == TokenType.WHITE_SPACE -> false
            else -> true
        }

        if (needsSpace) {
            // Replace comma with a space
            val spaceNode = LeafPsiElement(TokenType.WHITE_SPACE, " ")
            parent.replaceChild(commaNode, spaceNode)
        } else {
            // Just remove the comma
            parent.removeChild(commaNode)
        }
    }
}