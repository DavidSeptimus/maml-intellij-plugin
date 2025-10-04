package com.davidseptimus.maml.inspections

import com.davidseptimus.maml.MamlBundle
import com.davidseptimus.maml.lang.psi.MamlObject
import com.intellij.codeInspection.*
import com.intellij.codeInspection.options.OptPane
import com.intellij.codeInspection.options.OptPane.checkbox
import com.intellij.codeInspection.options.OptPane.pane
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.jsonSchema.extension.JsonLikePsiWalker
import com.jetbrains.jsonSchema.ide.JsonSchemaService
import com.jetbrains.jsonSchema.impl.*

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
        val options = JsonComplianceCheckerOptions(caseInsensitiveEnum)

        // Wrap the holder to add quick fixes for missing required properties
        val wrappedHolder = MamlProblemsHolderWrapper(holder, rootSchema)

        // Run the compliance checker
        // This will add problems to the wrapped holder for any schema violations
        JsonSchemaComplianceChecker(rootSchema, wrappedHolder, walker, session, options).annotate(element)
    }

    /**
     * Wraps ProblemsHolder to intercept problem registrations and add quick fixes
     * for missing required properties.
     */
    private class MamlProblemsHolderWrapper(
        private val delegate: ProblemsHolder,
        private val rootSchema: JsonSchemaObject
    ) : ProblemsHolder(delegate.manager, delegate.file, delegate.isOnTheFly) {

        override fun registerProblem(
            psiElement: PsiElement,
            descriptionTemplate: String,
            highlightType: ProblemHighlightType,
            vararg fixes: LocalQuickFix
        ) {
            val additionalFixes = getQuickFixesForProblem(psiElement, descriptionTemplate)
            val allFixes = fixes.toList() + additionalFixes.toList()
            delegate.registerProblem(psiElement, descriptionTemplate, highlightType, *allFixes.toTypedArray())
        }

        override fun registerProblem(
            psiElement: PsiElement,
            rangeInElement: TextRange?,
            descriptionTemplate: String,
            vararg fixes: LocalQuickFix
        ) {
            val additionalFixes = getQuickFixesForProblem(psiElement, descriptionTemplate)
            val allFixes = fixes.toList() + additionalFixes.toList()
            delegate.registerProblem(psiElement, rangeInElement, descriptionTemplate, *allFixes.toTypedArray())
        }

        override fun registerProblem(
            psiElement: PsiElement,
            descriptionTemplate: String,
            highlightType: ProblemHighlightType,
            rangeInElement: TextRange?,
            vararg fixes: LocalQuickFix
        ) {
            val additionalFixes = getQuickFixesForProblem(psiElement, descriptionTemplate)
            val allFixes = fixes.toList() + additionalFixes.toList()
            delegate.registerProblem(
                psiElement,
                descriptionTemplate,
                highlightType,
                rangeInElement,
                *allFixes.toTypedArray()
            )
        }

        private fun getQuickFixesForProblem(element: PsiElement, message: String): Array<LocalQuickFix> {
            // Check for missing required property error
            getMissingRequiredPropertyFix(element, message)?.let { return arrayOf(it) }

            // Check for disallowed property error
            getDisallowedPropertyFix(element, message)?.let { return arrayOf(it) }

            return emptyArray()
        }

        private fun getMissingRequiredPropertyFix(element: PsiElement, message: String): LocalQuickFix? {
            val missingPropPattern = Regex("Missing required propert(?:y|ies):?\\s+['\"]?([^'\"]+)['\"]?")
            val match = missingPropPattern.find(message) ?: return null
            val propertyName = match.groupValues.getOrNull(1) ?: return null

            // Find the schema for the property to determine a suggested value
            val obj = element as? MamlObject ?: return null
            val walker = JsonLikePsiWalker.getWalker(element, rootSchema) ?: return null
            val position = walker.findPosition(obj, false) ?: return null

            // Resolve the schema for this position
            val valueAdapter = walker.createValueAdapter(obj)
            val schemas = JsonSchemaResolver(delegate.project, rootSchema, position, valueAdapter).resolve()

            for (schema in schemas) {
                val propSchema = schema.getPropertyByName(propertyName)
                val suggestedValue = getSuggestedValue(propSchema)
                return MamlMissingRequiredPropertyQuickFix(propertyName, suggestedValue)
            }

            return null
        }

        private fun getDisallowedPropertyFix(element: PsiElement, message: String): LocalQuickFix? {
            val disallowedPropPattern = Regex("Property is not allowed")
            if (!disallowedPropPattern.containsMatchIn(message)) return null

            // Extract property name from the key-value pair
            val keyValue = element as? com.davidseptimus.maml.lang.psi.MamlKeyValue
                ?: element.parent as? com.davidseptimus.maml.lang.psi.MamlKeyValue
                ?: return null

            val propertyName = keyValue.key.name ?: return null
            return MamlRemoveDisallowedPropertyQuickFix(propertyName)
        }

        private fun getSuggestedValue(schema: JsonSchemaObject?): String {
            if (schema == null) return "\"\""

            return when {
                schema.default != null -> schema.default.toString()
                schema.enum != null && schema.enum!!.isNotEmpty() -> {
                    val first = schema.enum!!.first()
                    if (first is String) "\"$first\"" else first.toString()
                }

                schema.type != null -> when (schema.type) {
                    JsonSchemaType._string -> "\"\""
                    JsonSchemaType._number, JsonSchemaType._integer -> "0"
                    JsonSchemaType._boolean -> "false"
                    JsonSchemaType._array -> "[]"
                    JsonSchemaType._object -> "{}"
                    JsonSchemaType._null -> "null"
                    else -> "\"\""
                }

                else -> "\"\""
            }
        }
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