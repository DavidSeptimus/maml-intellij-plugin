package com.davidseptimus.maml.formatter

import com.davidseptimus.maml.lang.MamlLanguage
import com.davidseptimus.maml.lang.psi.MamlArray
import com.davidseptimus.maml.lang.psi.MamlElementFactory
import com.davidseptimus.maml.lang.psi.MamlObject
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
 * Pre-format processor that removes commas from MAML code and ensures one item per line.
 *  *
 *  * When removing commas, this processor ensures that items are on separate lines,
 * since commas are required when items are on the same line.
 *
 * For example:
 * ```
 * [1,2,3]  -> [1\n2\n3]
 * [1, 2, 3] -> [1\n2\n3]
 * [1\n2\n3] -> [1\n2\n3]  (already valid)
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

        // Process containers to add newlines where needed
        processContainers(psi)

        // Remove commas from the AST
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

    /**
     * Process all containers (arrays and objects) in the tree.
     */
    private fun processContainers(element: PsiElement) {
        // First recursively process children to handle nested structures
        element.children.forEach { processContainers(it) }

        // Then process the current element
        when (element) {
            is MamlArray -> processArray(element)
            is MamlObject -> processObject(element)
        }
    }

    /**
     * Process an array: ensure one item per line.
     */
    private fun processArray(array: MamlArray) {
        val items = array.items ?: return
        val values = items.valueList

        if (values.size < 2) return

        // Process from end to beginning to avoid index issues
        for (i in values.size - 1 downTo 1) {
            val currentValue = values[i - 1]
            val nextValue = values[i]

            // If they're on the same line, insert a newline
            if (areSameLine(currentValue, nextValue)) {
                val newline = MamlElementFactory.createNewline(array.project)
                items.addAfter(newline, currentValue)
            }
        }
    }

    /**
     * Process an object: ensure one key-value per line.
     */
    private fun processObject(obj: MamlObject) {
        val members = obj.members ?: return
        val keyValues = members.keyValueList

        if (keyValues.size < 2) return

        // Process from end to beginning to avoid index issues
        for (i in keyValues.size - 1 downTo 1) {
            val currentKv = keyValues[i - 1]
            val nextKv = keyValues[i]

            // If they're on the same line, insert a newline
            if (areSameLine(currentKv, nextKv)) {
                val newline = MamlElementFactory.createNewline(obj.project)
                members.addAfter(newline, currentKv)
            }
        }
    }

    /**
     * Check if two elements are on the same line.
     */
    private fun areSameLine(first: PsiElement, second: PsiElement): Boolean {
        var current: PsiElement? = first.nextSibling
        while (current != null && current != second) {
            if (current is PsiWhiteSpace && current.text.contains('\n')) {
                return false
            }
            current = current.nextSibling
        }
        return true
    }


    /**
     * Remove a comma, potentially replacing it with whitespace if needed for separation.
     */
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