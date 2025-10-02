package com.davidseptimus.maml.annotators

import com.davidseptimus.maml.highlighting.MamlTokenAttributes
import com.davidseptimus.maml.lang.psi.MamlTypes
import com.davidseptimus.maml.lang.psi.MamlValueElement
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement

/**
 * Annotator that highlights URLs and resolved file paths in string values.
 */
class MamlReferenceAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is MamlValueElement) return

        // Check if this is a string value
        val child = element.firstChild
        if (child !is LeafPsiElement) return
        if (child.elementType != MamlTypes.STRING && child.elementType != MamlTypes.MULTILINE_STRING) return

        val text = element.text.trim().removeSurrounding("\"").removeSurrounding("\"\"\"")
        if (text.isEmpty()) return

        // Check if it's a URL
        if (text.startsWith("http://") || text.startsWith("https://") || text.startsWith("file://")) {
            highlightAsUrl(element, holder)
            return
        }

        // Check if the reference resolves to a file
        val reference = element.reference
        if (reference != null && reference.resolve() != null) {
            highlightAsFilePath(element, holder)
        }
    }

    private fun highlightAsUrl(element: PsiElement, holder: AnnotationHolder) {
        val range = getStringContentRange(element)
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(range)
            .textAttributes(MamlTokenAttributes.URL)
            .create()
    }

    private fun highlightAsFilePath(element: PsiElement, holder: AnnotationHolder) {
        val range = getStringContentRange(element)
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(range)
            .textAttributes(MamlTokenAttributes.FILE_PATH)
            .create()
    }

    private fun getStringContentRange(element: PsiElement): TextRange {
        val text = element.text
        // For regular strings, skip the opening quote
        if (text.startsWith("\"") && !text.startsWith("\"\"\"")) {
            return TextRange.create(element.textRange.startOffset + 1, element.textRange.endOffset - 1)
        }
        // For multiline strings, skip the triple quotes
        if (text.startsWith("\"\"\"")) {
            return TextRange.create(element.textRange.startOffset + 3, element.textRange.endOffset - 3)
        }
        return element.textRange
    }
}