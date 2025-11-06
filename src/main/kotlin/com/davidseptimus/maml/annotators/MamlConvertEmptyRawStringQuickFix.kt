package com.davidseptimus.maml.annotators

import com.davidseptimus.maml.MamlBundle
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

/**
 * Quick fix that converts an empty raw string ("""...""") to an empty double-quoted string ("").
 */
class MamlConvertEmptyRawStringQuickFix(private val element: PsiElement) : IntentionAction, DumbAware {

    override fun getText(): String =
        MamlBundle.message("annotator.empty.raw.string.quickfix")

    override fun getFamilyName(): String =
        MamlBundle.message("annotator.empty.raw.string.quickfix.family")

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean =
        element.isValid

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (!element.isValid || editor == null) return

        val startOffset = element.textRange.startOffset
        val endOffset = element.textRange.endOffset

        // Replace the entire raw string with an empty double-quoted string
        editor.document.replaceString(startOffset, endOffset, "\"\"")

        // Place the caret after the closing quote
        editor.caretModel.moveToOffset(startOffset + 2)
    }

    override fun startInWriteAction(): Boolean = true
}