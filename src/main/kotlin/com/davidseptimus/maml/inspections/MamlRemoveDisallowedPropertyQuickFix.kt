package com.davidseptimus.maml.inspections

import com.davidseptimus.maml.MamlBundle
import com.davidseptimus.maml.lang.psi.MamlKeyValue
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.util.parentOfType

/**
 * Quick fix that removes a disallowed property from a MAML object.
 */
class MamlRemoveDisallowedPropertyQuickFix(
    private val propertyName: String
) : LocalQuickFix {

    override fun getName(): String =
        MamlBundle.message("inspection.disallowed.property.quickfix.name", propertyName)

    override fun getFamilyName(): String =
        MamlBundle.message("inspection.disallowed.property.quickfix.family")

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement
        val keyValue = element as? MamlKeyValue ?: element.parentOfType<MamlKeyValue>() ?: return

        // Delete the entire key-value pair
        keyValue.delete()
    }
}