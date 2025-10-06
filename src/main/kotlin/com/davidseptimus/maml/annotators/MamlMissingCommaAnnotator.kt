package com.davidseptimus.maml.annotators

import com.davidseptimus.maml.MamlBundle
import com.davidseptimus.maml.lang.psi.MamlArray
import com.davidseptimus.maml.lang.psi.MamlObject
import com.davidseptimus.maml.util.MamlSpacingUtil
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

/**
 * Annotator that detects missing commas between items on the same line.
 *
 * MAML rule: Commas are required when items/key-values are on the same line.
 * When items are on separate lines, commas are optional.
 */
class MamlMissingCommaAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element) {
            is MamlArray -> checkArray(element, holder)
            is MamlObject -> checkObject(element, holder)
        }
    }

    private fun checkArray(array: MamlArray, holder: AnnotationHolder) {
        val pairs = MamlSpacingUtil.getArrayValuePairs(array)

        for ((currentValue, nextValue) in pairs) {
            if (MamlSpacingUtil.areSameLine(currentValue, nextValue) &&
                !MamlSpacingUtil.hasCommaBetween(currentValue, nextValue)
            ) {

                // Create error annotation on the gap between the two values
                val start = currentValue.textRange.endOffset
                val end = nextValue.textRange.startOffset

                holder.newAnnotation(
                    HighlightSeverity.ERROR,
                    MamlBundle.message("annotator.missing.comma")
                )
                    .range(TextRange(start, end))
                    .newFix(MamlInsertMissingCommaQuickFix(currentValue)).registerFix()
                    .newFix(MamlBreakItemsOntoLinesQuickFix(array)).registerFix()
                    .create()
            }
        }
    }

    private fun checkObject(obj: MamlObject, holder: AnnotationHolder) {
        val pairs = MamlSpacingUtil.getObjectKeyValuePairs(obj)

        for ((currentKv, nextKv) in pairs) {
            if (MamlSpacingUtil.areSameLine(currentKv, nextKv) &&
                !MamlSpacingUtil.hasCommaBetween(currentKv, nextKv)
            ) {

                // Create error annotation on the gap between the two key-values
                val start = currentKv.textRange.endOffset
                val end = nextKv.textRange.startOffset

                holder.newAnnotation(
                    HighlightSeverity.ERROR,
                    MamlBundle.message("annotator.missing.comma")
                )
                    .range(TextRange(start, end))
                    .newFix(MamlInsertMissingCommaQuickFix(currentKv)).registerFix()
                    .newFix(MamlBreakItemsOntoLinesQuickFix(obj)).registerFix()
                    .create()
            }
        }
    }
}