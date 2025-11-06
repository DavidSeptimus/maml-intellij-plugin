package com.davidseptimus.maml.annotators

import com.davidseptimus.maml.MamlBundle
import com.davidseptimus.maml.lang.psi.MamlTypes
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement

/**
 * Annotator that flags completely empty raw strings (multiline strings).
 * Raw strings that are completely empty (no content between the triple quotes) are invalid
 * and should use double-quoted strings instead.
 *
 * Examples that trigger this annotator:
 * - `""""""` (completely empty raw string - INVALID)
 *
 * Valid raw strings (no annotation):
 * - `"""text"""` (contains text)
 * - `"""  """` (contains spaces)
 * - `"""\ntext"""` (contains newline)
 * - `"""
 *    text
 *    """` (contains actual newline characters)
 */
class MamlEmptyRawStringAnnotator : Annotator, DumbAware {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        // Only process multiline strings
        if (element.node.elementType != MamlTypes.MULTILINE_STRING) return

        val text = element.text
        if (!text.startsWith("\"\"\"") || text.length < 6) return

        // Extract content between opening and closing triple quotes
        val content = text.removePrefix("\"\"\"").removeSuffix("\"\"\"")

        // Only flag if the raw string is completely empty (no content at all)
        if (content.isEmpty()) {
            holder.newAnnotation(HighlightSeverity.ERROR, MamlBundle.message("annotator.empty.raw.string"))
                .range(element.textRange)
                .withFix(MamlConvertEmptyRawStringQuickFix(element))
                .create()
        }
    }
}