package com.davidseptimus.maml.annotators

import com.davidseptimus.maml.highlighting.MamlTokenAttributes
import com.davidseptimus.maml.lang.psi.MamlKey
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement

/**
 * Annotator that highlights string keys with the KEY text attribute.
 */
class MamlKeyAnnotator : Annotator, DumbAware {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is MamlKey) return

        // Check if this key is a quoted string (not an identifier)
        val text = element.text
        if (text.startsWith("\"") && text.endsWith("\"")) {
            // Highlight the entire string key with KEY attribute
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(element.textRange)
                .textAttributes(MamlTokenAttributes.KEY)
                .create()
        }
    }
}