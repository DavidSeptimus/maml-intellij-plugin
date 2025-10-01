package com.davidseptimus.maml.annotators

import com.davidseptimus.maml.MamlBundle
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement

/**
 * Annotator that provides improved, user-friendly error messages for MAML syntax errors.
 * Works in conjunction with MamlHighlightErrorFilter which disables default error highlighting.
 */
class MamlSyntaxErrorAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is PsiErrorElement) return

        val errorDescription = element.errorDescription
        val improvedMessage = getImprovedErrorMessage(errorDescription)

        holder.newAnnotation(HighlightSeverity.ERROR, improvedMessage)
            .range(element.textRange)
            .create()
    }

    private fun getImprovedErrorMessage(errorDescription: String): String {
        return when {
            // Missing colon after key
            errorDescription.contains("':' expected") ||
            errorDescription.contains("COLON expected") ->
                MamlBundle.message("syntax.error.expected.colon")

            // Missing closing braces
            errorDescription.contains("'}' expected") ||
            errorDescription.contains("RBRACE expected") ->
                MamlBundle.message("syntax.error.expected.rbrace")

            // Missing closing brackets
            errorDescription.contains("']' expected") ||
            errorDescription.contains("RBRACKET expected") ->
                MamlBundle.message("syntax.error.expected.rbracket")

            // Missing comma or separator
            errorDescription.contains("',' expected") ||
            errorDescription.contains("COMMA expected") ->
                MamlBundle.message("syntax.error.expected.comma")

            // Missing value
            errorDescription.contains("value expected") ||
            errorDescription.contains("VALUE expected") ->
                MamlBundle.message("syntax.error.expected.value")

            // Missing key
            errorDescription.contains("key expected") ||
            errorDescription.contains("KEY expected") ->
                MamlBundle.message("syntax.error.expected.key")

            // + character outside of string
            errorDescription.contains(", got '+'") ->
                MamlBundle.message("syntax.error.unexpected.plus")

            // Unexpected token messages
            errorDescription.contains("unexpected") ->
                MamlBundle.message("syntax.error.unexpected.token", extractToken(errorDescription))

            // Default: return original message
            else -> errorDescription
        }
    }

    private fun extractToken(errorDescription: String): String {
        // Try to extract the token from error message
        val match = Regex("'([^']+)'").find(errorDescription)
        return match?.groupValues?.get(1) ?: "unknown"
    }
}