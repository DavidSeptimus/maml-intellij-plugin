package com.davidseptimus.maml.annotators

import com.davidseptimus.maml.MamlBundle
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

/**
 * Quick fix that inserts a comma after an element.
 */
class MamlInsertMissingCommaQuickFix(private val element: PsiElement) : IntentionAction {

    override fun getText(): String =
        MamlBundle.message("annotator.missing.comma.quickfix.insert")

    override fun getFamilyName(): String =
        MamlBundle.message("annotator.missing.comma.quickfix.family")

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean =
        element.isValid

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (!element.isValid || editor == null) return

        val offset = element.textRange.endOffset
        editor.document.insertString(offset, ",")
    }

    override fun startInWriteAction(): Boolean = true
}