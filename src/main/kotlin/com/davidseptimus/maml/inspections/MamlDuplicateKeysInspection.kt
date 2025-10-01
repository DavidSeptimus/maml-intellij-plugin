package com.davidseptimus.maml.inspections

import com.davidseptimus.maml.MamlBundle
import com.davidseptimus.maml.psi.MamlKeyValue
import com.davidseptimus.maml.psi.MamlObject
import com.davidseptimus.maml.psi.MamlVisitor
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import com.intellij.refactoring.RefactoringActionHandler
import com.intellij.refactoring.RefactoringActionHandlerFactory
import com.intellij.refactoring.actions.RenameElementAction

/**
 * Inspection that detects and highlights duplicate keys within MAML objects.
 *
 * According to JSON specification (and by extension, most JSON-like formats),
 * duplicate keys in an object are not allowed. While some parsers may accept them,
 * the behavior is typically undefined (usually the last value wins).
 *
 * This inspection helps catch these issues early.
 */
class MamlDuplicateKeysInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : MamlVisitor() {
            override fun visitObject(obj: MamlObject) {
                super.visitObject(obj)
                checkForDuplicateKeys(obj, holder)
            }
        }
    }

    private fun checkForDuplicateKeys(obj: MamlObject, holder: ProblemsHolder) {
        val members = obj.members ?: return
        val keyValues = members.keyValueList

        // Group key-value pairs by their key text
        val keyGroups = keyValues.groupBy { kv ->
            // Remove surrounding quotes from keys to get the actual key name
            kv.key.text.removeSurrounding("\"")
        }

        // Report duplicates
        for ((keyName, duplicates) in keyGroups) {
            if (duplicates.size > 1) {
                // All occurrences after the first are duplicates
                for (duplicate in duplicates.drop(1)) {
                    holder.registerProblem(
                        duplicate.key,
                        MamlBundle.message("inspection.duplicate.keys.message", keyName),
                        RenameKeyQuickFix()
                    )
                }
            }
        }
    }

    override fun getDisplayName(): String =
        MamlBundle.message("inspection.duplicate.keys.display.name")

    override fun getGroupDisplayName(): String =
        MamlBundle.message("inspection.duplicate.keys.group.name")

    override fun getShortName(): String = "MamlDuplicateKeys"

    override fun isEnabledByDefault(): Boolean = true

    /**
     * Quick fix that triggers the rename refactoring for the duplicate key.
     */
    private class RenameKeyQuickFix : LocalQuickFix {
        override fun getFamilyName(): String =
            MamlBundle.message("inspection.duplicate.keys.quickfix.rename")

        override fun startInWriteAction(): Boolean = false

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val keyElement = descriptor.psiElement ?: return

            // Run on EDT (Event Dispatch Thread)
            ApplicationManager.getApplication().invokeLater {
                if (!keyElement.isValid) return@invokeLater

                // Get the rename handler and invoke it
                val handler = RefactoringActionHandlerFactory.getInstance().createRenameHandler()
                val editor = descriptor.psiElement?.containingFile?.let { file ->
                    com.intellij.openapi.fileEditor.FileEditorManager.getInstance(project)
                        .selectedTextEditor
                }

                if (editor != null) {
                    // Create a data context for the refactoring
                    val dataContext = com.intellij.openapi.actionSystem.DataContext { dataId ->
                        when (dataId) {
                            com.intellij.openapi.actionSystem.CommonDataKeys.PROJECT.name -> project
                            com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR.name -> editor
                            com.intellij.openapi.actionSystem.CommonDataKeys.PSI_ELEMENT.name -> keyElement
                            com.intellij.openapi.actionSystem.CommonDataKeys.PSI_FILE.name -> keyElement.containingFile
                            else -> null
                        }
                    }

                    // Invoke the rename refactoring
                    handler.invoke(project, editor, keyElement.containingFile, dataContext)
                }
            }
        }
    }
}