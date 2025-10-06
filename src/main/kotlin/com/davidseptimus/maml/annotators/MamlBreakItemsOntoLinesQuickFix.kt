package com.davidseptimus.maml.annotators

import com.davidseptimus.maml.MamlBundle
import com.davidseptimus.maml.lang.psi.MamlArray
import com.davidseptimus.maml.lang.psi.MamlElementFactory
import com.davidseptimus.maml.lang.psi.MamlObject
import com.davidseptimus.maml.util.MamlSpacingUtil
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

/**
 * Quick fix that breaks all items in a container (array or object) onto separate lines.
 * This operates on the entire container.
 */
class MamlBreakItemsOntoLinesQuickFix(private val container: PsiElement) : IntentionAction {

    override fun getText(): String =
        MamlBundle.message("annotator.missing.comma.quickfix.break.lines")

    override fun getFamilyName(): String =
        MamlBundle.message("annotator.missing.comma.quickfix.family")

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean = true

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        when (container) {
            is MamlArray -> breakArrayItems(container)
            is MamlObject -> breakObjectItems(container)
        }
    }

    private fun breakArrayItems(array: MamlArray) {
        val items = array.items ?: return
        val pairs = MamlSpacingUtil.getArrayValuePairs(array)

        // Process from end to beginning to avoid index issues
        for ((currentValue, nextValue) in pairs.reversed()) {
            if (MamlSpacingUtil.areSameLine(currentValue, nextValue)) {
                val newline = MamlElementFactory.createNewline(array.project)
                items.addAfter(newline, currentValue)
            }
        }
    }

    private fun breakObjectItems(obj: MamlObject) {
        val members = obj.members ?: return
        val pairs = MamlSpacingUtil.getObjectKeyValuePairs(obj)

        // Process from end to beginning to avoid index issues
        for ((currentKv, nextKv) in pairs.reversed()) {
            if (MamlSpacingUtil.areSameLine(currentKv, nextKv)) {
                val newline = MamlElementFactory.createNewline(obj.project)
                members.addAfter(newline, currentKv)
            }
        }
    }

    override fun startInWriteAction(): Boolean = true
}