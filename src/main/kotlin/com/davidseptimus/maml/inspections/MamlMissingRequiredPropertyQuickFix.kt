package com.davidseptimus.maml.inspections

import com.davidseptimus.maml.MamlBundle
import com.davidseptimus.maml.lang.psi.MamlElementFactory
import com.davidseptimus.maml.lang.psi.MamlObject
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.util.parentOfType

/**
 * Quick fix that adds a missing required property to a MAML object.
 */
class MamlMissingRequiredPropertyQuickFix(
    private val propertyName: String,
    private val suggestedValue: String = "\"\""
) : LocalQuickFix {

    override fun getName(): String =
        MamlBundle.message("inspection.missing.required.property.quickfix.name", propertyName)

    override fun getFamilyName(): String =
        MamlBundle.message("inspection.missing.required.property.quickfix.family")

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement
        val obj = element as? MamlObject ?: element.parentOfType<MamlObject>() ?: return

        val members = obj.members

        // Create the new key-value pair
        val newKeyValue = MamlElementFactory.createKeyValue(project, propertyName, suggestedValue)

        if (members == null) {
            // Object is empty, need to add members
            val newMembers = MamlElementFactory.createMembers(project, propertyName, suggestedValue)
            obj.addAfter(newMembers, obj.firstChild) // After opening brace
        } else {
            // Add to existing members
            val lastKeyValue = members.keyValueList.lastOrNull()
            if (lastKeyValue != null) {
                // Add after the last key-value pair
                val addedKeyValue = members.addAfter(newKeyValue, lastKeyValue)

                // Add a newline before the new property for better formatting
                val newline = MamlElementFactory.createNewline(project)
                members.addBefore(newline, addedKeyValue)
            } else {
                // No existing key-values, add as first
                members.add(newKeyValue)
            }
        }
    }
}