package com.davidseptimus.maml.inspections

import com.davidseptimus.maml.MamlBundle
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.options.OptPane
import com.intellij.codeInspection.options.OptPane.checkbox
import com.intellij.codeInspection.options.OptPane.pane
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.jsonSchema.extension.JsonLikePsiWalker
import com.jetbrains.jsonSchema.ide.JsonSchemaService
import com.jetbrains.jsonSchema.impl.JsonComplianceCheckerOptions
import com.jetbrains.jsonSchema.impl.JsonSchemaComplianceChecker
import com.jetbrains.jsonSchema.impl.JsonSchemaObject

/**
 * Inspection that validates MAML files against their associated JSON schemas.
 *
 * This inspection integrates with IntelliJ's JSON Schema infrastructure to provide:
 * - Type validation
 * - Required property checking
 * - Enum value validation
 * - Pattern matching
 * - Min/max constraints
 * - Additional property validation
 * - And all other JSON Schema validation features
 */
class MamlJsonSchemaComplianceInspection : LocalInspectionTool() {

    @JvmField
    var caseInsensitiveEnum = false

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor {
        val file = holder.file
        val virtualFile = file.viewProvider.virtualFile

        // Get the JSON schema service
        val service = JsonSchemaService.Impl.get(file.project)
        if (!service.isApplicableToFile(virtualFile)) {
            return PsiElementVisitor.EMPTY_VISITOR
        }

        // Get the schema for this file
        val schema = service.getSchemaObject(file) ?: return PsiElementVisitor.EMPTY_VISITOR

        // Get the walker to find the root value element
        val walker = JsonLikePsiWalker.getWalker(file, schema) ?: return PsiElementVisitor.EMPTY_VISITOR

        // Get all root elements - MAML should have a single root value
        val allRoots = walker.getRoots(file)
        val root = allRoots?.singleOrNull() ?: return PsiElementVisitor.EMPTY_VISITOR

        // Return a visitor that validates the root value
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                // Only validate the root element
                // The checker will traverse the entire tree from there
                if (element == root) {
                    annotate(element, schema, holder, session)
                }
                super.visitElement(element)
            }
        }
    }

    private fun annotate(
        element: PsiElement,
        rootSchema: JsonSchemaObject,
        holder: ProblemsHolder,
        session: LocalInspectionToolSession
    ) {
        // Get the PSI walker for MAML files
        val walker = JsonLikePsiWalker.getWalker(element, rootSchema) ?: return

        // Create compliance checker with options
        val options = JsonComplianceCheckerOptions(caseInsensitiveEnum,)

        // Run the compliance checker
        // This will add problems to the holder for any schema violations
        JsonSchemaComplianceChecker(rootSchema, holder, walker, session, options).annotate(element)
    }

    override fun getOptionsPane(): OptPane {
        return pane(
            checkbox("caseInsensitiveEnum", MamlBundle.message("inspection.schema.compliance.case.insensitive.enum"))
        )
    }

    override fun getDisplayName(): String =
        MamlBundle.message("inspection.schema.compliance.display.name")

    override fun getGroupDisplayName(): String =
        MamlBundle.message("inspection.schema.compliance.group.name")

    override fun isEnabledByDefault(): Boolean = true
}