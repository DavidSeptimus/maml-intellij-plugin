package com.davidseptimus.maml.annotators

import com.davidseptimus.maml.MamlBundle
import com.davidseptimus.maml.lang.psi.MamlInvalidValue
import com.davidseptimus.maml.lang.psi.MamlTypes
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement

/**
 * Annotator that highlights invalid values in MAML files.
 * This includes:
 * - Partial/incomplete keywords (e.g., "fa" instead of "false")
 * - Unterminated strings (e.g., "hello without closing quote)
 * - Invalid identifiers in value positions
 */
class MamlInvalidValueAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is MamlInvalidValue) return

        val child = element.firstChild ?: return

        when (child.node.elementType) {
            MamlTypes.IDENTIFIER -> {
                val text = child.text
                val message = when {
                    text.startsWith("t") || text.startsWith("f") || text.startsWith("n") ->
                        MamlBundle.message("annotator.invalid.value.identifier", text)
                    else ->
                        MamlBundle.message("annotator.invalid.value.identifier.generic", text)
                }

                holder.newAnnotation(HighlightSeverity.ERROR, message)
                    .range(element.textRange)
                    .create()
            }

            MamlTypes.UNTERMINATED_STRING -> {
                holder.newAnnotation(HighlightSeverity.ERROR, MamlBundle.message("annotator.invalid.value.unterminated.string"))
                    .range(element.textRange)
                    .create()
            }
        }
    }
}