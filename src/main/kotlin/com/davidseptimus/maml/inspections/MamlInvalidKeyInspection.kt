package com.davidseptimus.maml.inspections

import com.davidseptimus.maml.MamlBundle
import com.davidseptimus.maml.psi.MamlKey
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor

/**
 * Inspection that detects invalid identifier keys in MAML.
 *
 * Identifier keys must:
 * - Contain only A-Z, a-z, 0-9, _, and - characters
 * - Be non-empty
 *
 * Quoted string keys are always valid (including empty strings).
 */
class MamlInvalidKeyInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element !is MamlKey) return

                val text = element.text

                // Check if it's an identifier key (not a quoted string)
                if (!text.startsWith("\"")) {
                    // Identifier key - must be non-empty and match pattern
                    if (text.isEmpty()) {
                        holder.registerProblem(
                            element,
                            MamlBundle.message("inspection.invalid.key.empty"),
                            ConvertToQuotedKeyQuickFix()
                        )
                    } else if (!VALID_IDENTIFIER_PATTERN.matches(text)) {
                        holder.registerProblem(
                            element,
                            MamlBundle.message("inspection.invalid.key.invalid.chars", text),
                            ConvertToQuotedKeyQuickFix()
                        )
                    }
                }
                // Quoted string keys are always valid (including empty strings)
            }
        }
    }

    /**
     * Quick fix that converts an invalid identifier key to a quoted string key.
     */
    private class ConvertToQuotedKeyQuickFix : LocalQuickFix {
        override fun getFamilyName(): String =
            MamlBundle.message("inspection.invalid.key.quickfix")

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val element = descriptor.psiElement as? MamlKey ?: return
            val text = element.text

            // Escape special characters and wrap in quotes
            val escaped = text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")

            val quotedKey = "\"$escaped\""

            // Create new key element
            val tempFile = com.davidseptimus.maml.psi.MamlElementFactory.createKey(project, quotedKey)
            element.replace(tempFile)
        }
    }
}

// Valid characters for identifier keys: A-Z a-z 0-9 _ -
private val VALID_IDENTIFIER_PATTERN = Regex("^[A-Za-z0-9_-]+$")