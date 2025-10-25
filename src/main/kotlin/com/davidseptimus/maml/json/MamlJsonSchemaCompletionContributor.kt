package com.davidseptimus.maml.json

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.json.pointer.JsonPointerPosition
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.util.Consumer
import com.intellij.util.ThreeState
import com.jetbrains.jsonSchema.extension.JsonLikePsiWalker
import com.jetbrains.jsonSchema.extension.adapters.JsonPropertyAdapter
import com.jetbrains.jsonSchema.extension.adapters.JsonValueAdapter
import com.jetbrains.jsonSchema.ide.JsonSchemaService
import com.jetbrains.jsonSchema.impl.JsonSchemaDocumentationProvider
import com.jetbrains.jsonSchema.impl.JsonSchemaObject
import com.jetbrains.jsonSchema.impl.JsonSchemaResolver
import com.jetbrains.jsonSchema.impl.JsonSchemaType
import com.jetbrains.jsonSchema.impl.light.legacy.JsonSchemaObjectReadingUtils
import com.jetbrains.jsonSchema.impl.tree.JsonSchemaNodeExpansionRequest
import one.util.streamex.StreamEx

class MamlJsonSchemaCompletionContributor : CompletionContributor() {
    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        val position = parameters.position
        val jsonSchemaService = JsonSchemaService.Impl.get(position.project)
        val jsonSchemaObject = jsonSchemaService.getSchemaObject(parameters.originalFile) ?: return

        val completionPosition = parameters.originalPosition ?: parameters.position
        val worker = Worker(jsonSchemaObject, parameters.position, completionPosition, result)
        val isExhaustive = worker.work()

        // Stop other contributors if our completions are exhaustive
        if (isExhaustive) {
            result.stopHere()
        }
    }

    private class Worker(
        private val rootSchema: JsonSchemaObject,
        private val position: PsiElement,
        private val originalPosition: PsiElement,
        private val resultConsumer: Consumer<LookupElement?>
    ) {
        private val completionVariants: MutableSet<LookupElement> = mutableSetOf()
        private val walker: JsonLikePsiWalker? = JsonLikePsiWalker.getWalker(position, rootSchema)
        private val project: Project = originalPosition.project
        private val wrapInQuotes: Boolean
        private val insideStringLiteral: Boolean
        private var isExhaustive: Boolean = false

        init {
            val positionParent = position.parent
            val isInsideQuotedString = positionParent != null && walker != null && walker.isQuotedString(positionParent)
            wrapInQuotes = !isInsideQuotedString
            insideStringLiteral = isInsideQuotedString
        }

        fun work(): Boolean {
            val checkable = walker?.findElementToCheck(position) ?: return false
            val isName = walker.isName(checkable)
            val pointerPosition = walker.findPosition(checkable, isName == ThreeState.NO)
            if (pointerPosition == null || pointerPosition.isEmpty && isName == ThreeState.NO) return false

            val expansionRequest = JsonSchemaNodeExpansionRequest(
                walker.getParentPropertyAdapter(position)?.parentObject,
                false
            )
            val schemas = JsonSchemaResolver(project, rootSchema, pointerPosition, expansionRequest).resolve()
            val knownNames = hashSetOf<String>()

            for (schema in schemas) {
                if (isName != ThreeState.NO) {
                    val properties = walker.getPropertyNamesOfParentObject(originalPosition, position)
                    val adapter = walker.getParentPropertyAdapter(checkable)

                    addAllPropertyVariants(schema, properties, adapter, knownNames)
                    // Property completion is exhaustive if additionalProperties is not allowed
                    if (!isAdditionalPropertiesAllowed(schema)) {
                        isExhaustive = true
                    }
                }

                if (isName != ThreeState.YES) {
                    val wasExhaustive = suggestValues(schema, isName == ThreeState.NO)
                    if (wasExhaustive) {
                        isExhaustive = true
                    }
                }
            }

            for (variant in completionVariants) {
                resultConsumer.consume(variant)
            }

            return isExhaustive
        }

        /**
         * Checks if the schema allows additional properties beyond those defined.
         * Returns true if additionalProperties is true or a schema, false if it's explicitly false.
         */
        private fun isAdditionalPropertiesAllowed(schema: JsonSchemaObject): Boolean {
            // If additionalPropertiesAllowed is null, it's implicitly true (default behavior)
            // If it's explicitly false, then only defined properties are allowed
            return schema.additionalPropertiesAllowed != false
        }

        private fun addAllPropertyVariants(
            schema: JsonSchemaObject,
            properties: Collection<String>,
            adapter: JsonPropertyAdapter?,
            knownNames: MutableSet<String>
        ) {
            val variants = StreamEx.of(schema.propertyNames).filter { name ->
                !properties.contains(name) && !knownNames.contains(name) || name == adapter?.name
            }

            for (variant in variants) {
                knownNames.add(variant)
                val jsonSchemaObject = schema.getPropertyByName(variant)

                if (jsonSchemaObject != null) {
                    addPropertyVariant(variant, jsonSchemaObject, adapter?.nameValueAdapter)
                }
            }
        }

        private fun addPropertyVariant(
            key: String,
            jsonSchemaObject: JsonSchemaObject,
            originalPositionAdapter: JsonValueAdapter?
        ) {
            val currentVariants = JsonSchemaResolver(
                project,
                jsonSchemaObject,
                JsonPointerPosition(),
                JsonSchemaNodeExpansionRequest(originalPositionAdapter, false)
            ).resolve()
            val schemaObject = currentVariants.firstOrNull() ?: jsonSchemaObject

            var description = JsonSchemaDocumentationProvider.getBestDocumentation(true, schemaObject)
            if (description.isNullOrBlank()) {
                description = JsonSchemaObjectReadingUtils.getTypeDescription(schemaObject, true).orEmpty()
            }
            description = StringUtil.unescapeXmlEntities(description)

            val propertyKey = if (!shouldWrapInQuotes(key, false)) {
                key
            } else {
                walker?.escapeInvalidIdentifier(key) ?: StringUtil.wrapWithDoubleQuote(key)
            }

            val lookupElement = LookupElementBuilder.create(propertyKey)
                .withTypeText(description)
                .withIcon(getIconForType(JsonSchemaObjectReadingUtils.guessType(schemaObject)))
                .withInsertHandler(createDefaultPropertyInsertHandler(schemaObject))

            completionVariants.add(lookupElement)
        }

        /**
         * Suggests values based on schema and returns whether the suggestions are exhaustive.
         * Exhaustive means no other completion providers should add suggestions.
         */
        private fun suggestValues(schema: JsonSchemaObject, isSurelyValue: Boolean): Boolean {
            val enumVariants = schema.enum
            if (enumVariants != null) {
                for (o in enumVariants) {
                    if (insideStringLiteral && o !is String) continue

                    // For string enum values, use addStringVariant for proper quote handling
                    if (o is String) {
                        addStringVariant(o)
                    } else {
                        // For non-string values (numbers, booleans, null), add directly
                        addValueVariant(o.toString())
                    }
                }
                // Enums are exhaustive - only these specific values are allowed
                return true
            } else if (isSurelyValue) {
                val type = JsonSchemaObjectReadingUtils.guessType(schema)
                suggestByType(schema, type)
                // Value completions are always exhaustive when we have schema information
                return true
            }
            return false
        }

        /**
         * Suggests values by type.
         */
        private fun suggestByType(schema: JsonSchemaObject, type: JsonSchemaType?) {
            if (type == JsonSchemaType._string) {
                addPossibleStringValue(schema)
            }
            if (insideStringLiteral) {
                return
            }
            when (type) {
                JsonSchemaType._boolean -> {
                    addValueVariant("true", type = JsonSchemaType._boolean)
                    addValueVariant("false", type = JsonSchemaType._boolean)
                }

                JsonSchemaType._null -> {
                    addValueVariant("null", type = JsonSchemaType._null)
                }

                JsonSchemaType._array -> {
                    val value = walker!!.defaultArrayValue
                    addValueVariant(
                        key = value,
                        altText = "[...]",
                        handler = createArrayOrObjectLiteralInsertHandler(value.length),
                        type = JsonSchemaType._array
                    )
                }

                JsonSchemaType._object -> {
                    val value = walker!!.defaultObjectValue
                    addValueVariant(
                        key = value,
                        altText = "{...}",
                        handler = createArrayOrObjectLiteralInsertHandler(value.length),
                        type = JsonSchemaType._object
                    )
                }

                else -> {
                    //  no suggestions
                }
            }
        }

        private fun addPossibleStringValue(schema: JsonSchemaObject) {
            val defaultValue = schema.default?.toString() ?: ""
            addStringVariant(defaultValue)
        }

        private fun addStringVariant(defaultValueString: String) {
            if (defaultValueString.isEmpty()) {
                completionVariants.add(createEmptyStringCompletion())
                return
            }
            var normalizedValue: String = defaultValueString
            val shouldQuote = walker!!.requiresValueQuotes()
            val isQuoted = StringUtil.isQuotedString(normalizedValue)
            if (shouldQuote && !isQuoted) {
                normalizedValue = StringUtil.wrapWithDoubleQuote(normalizedValue)
            } else if (!shouldQuote && isQuoted) {
                normalizedValue = StringUtil.unquoteString(normalizedValue)
            }
            addValueVariant(normalizedValue, type = JsonSchemaType._string)
        }

        private fun createEmptyStringCompletion(): LookupElement {
            return LookupElementBuilder.create("\"\"")
                .withPresentableText("\"\"")
                .withInsertHandler { context, _ ->
                    val editor = context.editor
                    val caretOffset = context.tailOffset
                    // Position caret between the quotes
                    editor.caretModel.moveToOffset(caretOffset - 1)
                }
        }

        private fun addValueVariant(
            key: String,
            altText: String? = null,
            handler: InsertHandler<LookupElement?>? = null,
            type: JsonSchemaType? = null
        ) {
            val unquoted = StringUtil.unquoteString(key)
            val lookupString = if (!shouldWrapInQuotes(unquoted, true)) unquoted else key
            val builder = LookupElementBuilder.create(lookupString)
                .withPresentableText(altText ?: lookupString)
                .withInsertHandler(handler)
                .withIcon(getIconForType(type))

            completionVariants.add(builder)
        }

        private fun shouldWrapInQuotes(key: String?, isValue: Boolean): Boolean {
            return wrapInQuotes && walker != null &&
                    (isValue && walker.requiresValueQuotes() ||
                            !isValue && walker.requiresNameQuotes() ||
                            key != null && !walker.isValidIdentifier(key, project))
        }

        @Suppress("UNUSED_PARAMETER")
        private fun createDefaultPropertyInsertHandler(jsonSchemaObject: JsonSchemaObject): InsertHandler<LookupElement> {
            return InsertHandler { context, _ ->
                ApplicationManager.getApplication().assertWriteAccessAllowed()
                val editor = context.editor
                val project = context.project

                // Don't insert anything if we're inside a string literal
                if (insideStringLiteral) return@InsertHandler

                val hasValue = walker?.let {
                    it.isPropertyWithValue(it.findElementToCheck(position))
                } == true

                var offset = editor.caretModel.offset
                val initialOffset = offset
                val docChars = context.document.charsSequence

                // Skip whitespace
                while (offset < docChars.length && Character.isWhitespace(docChars[offset])) {
                    offset++
                }

                val propertyValueSeparator = walker!!.getPropertyValueSeparator(null)

                if (hasValue) {
                    // Property already has a value, just ensure there's a colon
                    if (offset < docChars.length && !docChars.subSequence(offset, docChars.length)
                            .startsWith(propertyValueSeparator)
                    ) {
                        editor.document.insertString(initialOffset, propertyValueSeparator)
                    }
                    return@InsertHandler
                }

                // Check if colon already exists
                if (offset < docChars.length && docChars.subSequence(offset, docChars.length)
                        .startsWith(propertyValueSeparator)
                ) {
                    // Move caret after colon and space
                    val nextOffset = offset + propertyValueSeparator.length
                    if (nextOffset < docChars.length && docChars[nextOffset] == ' ') {
                        editor.caretModel.moveToOffset(nextOffset + 1)
                    } else {
                        editor.caretModel.moveToOffset(nextOffset)
                        EditorModificationUtil.insertStringAtCaret(editor, " ", false, true, 1)
                    }
                } else {
                    // Insert colon and trigger autocomplete
                    val stringToInsert = "$propertyValueSeparator "
                    EditorModificationUtil.insertStringAtCaret(
                        editor,
                        stringToInsert,
                        false,
                        true,
                        stringToInsert.length
                    )
                }

                PsiDocumentManager.getInstance(project).commitDocument(editor.document)
                AutoPopupController.getInstance(context.project).scheduleAutoPopup(context.editor)
            }
        }

        @Suppress("UNUSED_PARAMETER")
        private fun createArrayOrObjectLiteralInsertHandler(insertedTextSize: Int): InsertHandler<LookupElement?> {
            return InsertHandler { context, _ ->
                val editor = context.editor
                EditorModificationUtil.moveCaretRelatively(editor, -1)
            }
        }

        private fun getIconForType(type: JsonSchemaType?) = when (type) {
            JsonSchemaType._object -> AllIcons.Json.Object
            JsonSchemaType._array -> AllIcons.Json.Array
            else -> null // no icon for primitive types
        }
    }
}