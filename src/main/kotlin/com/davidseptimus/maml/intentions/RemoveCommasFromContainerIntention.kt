package com.davidseptimus.maml.intentions

import com.davidseptimus.maml.MamlBundle
import com.davidseptimus.maml.lang.psi.MamlArray
import com.davidseptimus.maml.lang.psi.MamlElementFactory
import com.davidseptimus.maml.lang.psi.MamlObject
import com.davidseptimus.maml.util.MamlSpacingUtil
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType

/**
 * Intention action to remove commas from arrays and objects.
 * Ensures items are on separate lines before removing commas.
 */
class RemoveCommasFromContainerIntention : PsiElementBaseIntentionAction(), IntentionAction {

    override fun getText(): String = MamlBundle.message("intention.remove.commas.text")

    override fun getFamilyName(): String = MamlBundle.message("intention.remove.commas.family")

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        val container = element.parentOfType<MamlArray>() ?: element.parentOfType<MamlObject>()
        return container != null && MamlSpacingUtil.hasCommas(container)
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val container = element.parentOfType<MamlArray>() ?: element.parentOfType<MamlObject>() ?: return

        // First, ensure all items are on separate lines
        when (container) {
            is MamlArray -> ensureArrayItemsOnSeparateLines(container)
            is MamlObject -> ensureObjectItemsOnSeparateLines(container)
        }

        // Then remove all commas
        removeCommasFromElement(container)
    }

    private fun ensureArrayItemsOnSeparateLines(array: MamlArray) {
        val items = array.items ?: return
        val pairs = MamlSpacingUtil.getArrayValuePairs(array)

        // Process from end to beginning to avoid index issues
        for ((first, second) in pairs.reversed()) {
            if (MamlSpacingUtil.areSameLine(first, second)) {
                val newline = MamlElementFactory.createNewline(array.project)
                items.addAfter(newline, first)
            }
        }
    }

    private fun ensureObjectItemsOnSeparateLines(obj: MamlObject) {
        val members = obj.members ?: return
        val pairs = MamlSpacingUtil.getObjectKeyValuePairs(obj)

        // Process from end to beginning to avoid index issues
        for ((first, second) in pairs.reversed()) {
            if (MamlSpacingUtil.areSameLine(first, second)) {
                val newline = MamlElementFactory.createNewline(obj.project)
                members.addAfter(newline, first)
            }
        }
    }

    private fun removeCommasFromElement(element: PsiElement) {
        val commas = MamlSpacingUtil.findCommas(element)
        commas.forEach { it.delete() }

        // Recursively remove from children
        element.children.forEach { child ->
            removeCommasFromElement(child)
        }
    }
}