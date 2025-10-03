package com.davidseptimus.maml.annotators

import com.davidseptimus.maml.MamlBundle
import com.davidseptimus.maml.lang.psi.MamlIncompleteKeyValue
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement

/**
 * Annotator that highlights incomplete key-value pairs (keys without colons).
 * This provides helpful feedback when the user has typed a key but hasn't yet added the colon.
 */
class MamlIncompleteKeyValueAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is MamlIncompleteKeyValue) return

        val key = element.key

        holder.newAnnotation(HighlightSeverity.ERROR, MamlBundle.message("annotator.incomplete.key.value", key.text))
            .range(element.textRange)
            .afterEndOfLine()
            .create()
    }
}