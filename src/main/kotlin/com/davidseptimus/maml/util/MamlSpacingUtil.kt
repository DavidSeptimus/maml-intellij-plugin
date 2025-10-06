package com.davidseptimus.maml.util

import com.davidseptimus.maml.lang.psi.MamlArray
import com.davidseptimus.maml.lang.psi.MamlObject
import com.davidseptimus.maml.lang.psi.MamlTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.elementType

/**
 * Utility methods for working with commas in MAML code.
 */
object MamlSpacingUtil {

    /**
     * Check if two elements are on the same line.
     */
    fun areSameLine(first: PsiElement, second: PsiElement): Boolean {
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
     * Check if there's a comma between two elements.
     */
    fun hasCommaBetween(first: PsiElement, second: PsiElement): Boolean {
        var current: PsiElement? = first.nextSibling
        while (current != null && current != second) {
            if (current.elementType == MamlTypes.COMMA) {
                return true
            }
            current = current.nextSibling
        }
        return false
    }

    /**
     * Get all consecutive element pairs in an array.
     * Returns a list of pairs (current, next) for all adjacent values.
     */
    fun getArrayValuePairs(array: MamlArray): List<Pair<PsiElement, PsiElement>> {
        val items = array.items ?: return emptyList()
        val values = items.valueList
        if (values.size < 2) return emptyList()

        return values.zipWithNext()
    }

    /**
     * Get all consecutive element pairs in an object.
     * Returns a list of pairs (current, next) for all adjacent key-values.
     */
    fun getObjectKeyValuePairs(obj: MamlObject): List<Pair<PsiElement, PsiElement>> {
        val members = obj.members ?: return emptyList()
        val keyValues = members.keyValueList
        if (keyValues.size < 2) return emptyList()

        return keyValues.zipWithNext()
    }

    /**
     * Check if a container (array or object) has items on the same line without commas.
     */
    fun hasMissingCommas(container: PsiElement): Boolean {
        return when (container) {
            is MamlArray -> {
                getArrayValuePairs(container).any { (first, second) ->
                    areSameLine(first, second) && !hasCommaBetween(first, second)
                }
            }

            is MamlObject -> {
                getObjectKeyValuePairs(container).any { (first, second) ->
                    areSameLine(first, second) && !hasCommaBetween(first, second)
                }
            }

            else -> false
        }
    }

    /**
     * Check if a container has any commas.
     */
    fun hasCommas(container: PsiElement): Boolean {
        return when (container) {
            is MamlArray -> {
                val items = container.items ?: return false
                findCommas(items).isNotEmpty()
            }

            is MamlObject -> {
                val members = container.members ?: return false
                findCommas(members).isNotEmpty()
            }

            else -> false
        }
    }

    /**
     * Find all comma elements within a PSI element.
     */
    fun findCommas(element: PsiElement): List<PsiElement> {
        val commas = mutableListOf<PsiElement>()
        for (child in element.children) {
            if (child.elementType == MamlTypes.COMMA) {
                commas.add(child)
            }
        }
        return commas
    }

    /**
     * Check if all items in a container are on separate lines.
     */
    fun allItemsOnSeparateLines(container: PsiElement): Boolean {
        return when (container) {
            is MamlArray -> {
                getArrayValuePairs(container).all { (first, second) ->
                    !areSameLine(first, second)
                }
            }

            is MamlObject -> {
                getObjectKeyValuePairs(container).all { (first, second) ->
                    !areSameLine(first, second)
                }
            }

            else -> false
        }
    }
}