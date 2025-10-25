package com.davidseptimus.maml.completion

import com.davidseptimus.maml.lang.psi.*
import com.davidseptimus.maml.settings.MamlSettings
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.DumbAware
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext

private val fakeString = "IntellijIdeaRulezzz"

/**
 * Provides completion suggestions for known keys in the current file.
 * Collects all keys that appear in the file and suggests them when typing a new key.
 */
class MamlKnownKeysCompletionContributor : CompletionContributor(), DumbAware {
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

            // Collect all keys with their occurrence counts
            val keyOccurrences = CachedValuesManager.getCachedValue(file) {
                val occurrences = mutableMapOf<String, Int>()

                file.accept(object : MamlRecursiveElementVisitor() {
                    override fun visitElement(element: PsiElement) {
                        if (element is MamlKey) {
                            element.name?.let { keyName ->
                                occurrences[keyName] = (occurrences[keyName] ?: 0) + 1
                            }
                        }
                        super.visitElement(element)
                    }
                })

                CachedValueProvider.Result.create(occurrences, PsiModificationTracker.MODIFICATION_COUNT)
            }

            // Get existing keys in the current object
            val currentObject = position.parentOfType<MamlObject>()
            val existingKeys = currentObject?.members?.keyValueList
                ?.mapNotNull { it.key.name }
                ?.toSet() ?: emptySet()

            // Get the current key being edited
            val currentKey = position.parentOfType<MamlKey>()
            val currentKeyName = currentKey?.name?.removeSuffix(fakeString)

            // Determine which keys to exclude from suggestions
            val keysToExclude = mutableSetOf<String>()
            keysToExclude.addAll(existingKeys)

            // If current key name appears only once in the file, exclude it
            if (currentKeyName != null && keyOccurrences[currentKeyName] == 1) {
                keysToExclude.add(currentKeyName)
            }

            // Filter available keys
            val availableKeys = keyOccurrences.keys - keysToExclude

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