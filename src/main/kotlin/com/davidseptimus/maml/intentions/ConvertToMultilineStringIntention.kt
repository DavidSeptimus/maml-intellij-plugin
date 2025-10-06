package com.davidseptimus.maml.intentions

import com.davidseptimus.maml.MamlBundle
import com.davidseptimus.maml.lang.psi.MamlElementFactory
import com.davidseptimus.maml.lang.psi.MamlTypes
import com.davidseptimus.maml.util.MamlStringUtil
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType

/**
 * Intention action to convert a single-line string to a multiline string.
 */
class ConvertToMultilineStringIntention : PsiElementBaseIntentionAction(), IntentionAction {

    override fun getFamilyName(): String =
        MamlBundle.message("intention.convert.to.multiline.string.family")

    override fun getText(): String =
        MamlBundle.message("intention.convert.to.multiline.string.text")

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        // Available only on STRING tokens (not MULTILINE_STRING)
        return element.elementType == MamlTypes.STRING
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        if (element.elementType != MamlTypes.STRING) return

        val content = MamlStringUtil.quotedToMultilineContent(element.text)
        val multilineText = MamlStringUtil.wrapInMultilineQuotes(content)

        // Create a temporary file with a value to extract the multiline string from
        val tempFile = MamlElementFactory.createFile(project, multilineText)
        val newStringElement = tempFile.firstChild?.firstChild

        if (newStringElement != null) {
            element.replace(newStringElement)
        }
    }
}