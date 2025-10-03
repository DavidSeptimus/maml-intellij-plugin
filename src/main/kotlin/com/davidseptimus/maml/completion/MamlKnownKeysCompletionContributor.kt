package com.davidseptimus.maml.completion

import com.davidseptimus.maml.lang.psi.MamlKey
import com.davidseptimus.maml.lang.psi.MamlObject
import com.davidseptimus.maml.lang.psi.MamlRecursiveElementVisitor
import com.davidseptimus.maml.lang.psi.MamlTypes
import com.davidseptimus.maml.lang.psi.MamlValue
import com.davidseptimus.maml.settings.MamlSettings
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext

/**
 * Provides completion suggestions for known keys in the current file.
 * Collects all keys that appear in the file and suggests them when typing a new key.
 */
class MamlKnownKeysCompletionContributor : CompletionContributor() {
    init {
        val provider = KnownKeysCompletionProvider()

        // Support completion for identifier keys (unquoted)
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withElementType(MamlTypes.IDENTIFIER),
            provider
        )

        // Support completion for string keys (quoted)
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withElementType(MamlTypes.STRING),
            provider
        )
    }

    private class KnownKeysCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet
        ) {
            if (!MamlSettings.getInstance().enableKnownKeysCompletion) return

            val position = parameters.position

            // Only provide completion in key positions (not in value positions)
            // If we're not in a MamlKey parent, don't suggest keys
            if (position.parentOfType<MamlKey>() == null && position.parentOfType<MamlValue>() != null) {
                return
            }

            val file = parameters.originalFile
            val knownKeys = CachedValuesManager.getCachedValue(file) {
                val keys = mutableSetOf<String>()

                file.accept(object : MamlRecursiveElementVisitor() {
                    override fun visitElement(element: PsiElement) {
                        if (element is MamlKey) {
                            element.name?.let { keys.add(it) }
                        }
                        super.visitElement(element)
                    }
                })

                CachedValueProvider.Result.create(keys, PsiModificationTracker.MODIFICATION_COUNT)
            }

            // Get existing keys in the current object
            val currentObject = position.parentOfType<MamlObject>()
            val existingKeys = currentObject?.members?.keyValueList
                ?.mapNotNull { it.key.name }
                ?.toSet() ?: emptySet()

            // Filter out keys that already exist in the current object
            val availableKeys = knownKeys - existingKeys

            // Add completion items for each available key
            for (key in availableKeys.sorted()) {
                result.addElement(
                    LookupElementBuilder.create(key)
                        .withTypeText("known key")
                )
            }
        }
    }
}