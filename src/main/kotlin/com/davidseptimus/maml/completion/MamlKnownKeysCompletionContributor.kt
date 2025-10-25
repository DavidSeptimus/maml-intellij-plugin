package com.davidseptimus.maml.completion

import com.davidseptimus.maml.formatter.MamlCodeStyleSettings
import com.davidseptimus.maml.lang.psi.*
import com.davidseptimus.maml.settings.MamlSettings
import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.DumbAware
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext

private const val fakeString = "IntellijIdeaRulezzz"

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
            PlatformPatterns.psiElement().withElementType(MamlTypes.IDENTIFIER)
                .and(PlatformPatterns.psiElement().withParent(MamlKey::class.java)),
            provider
        )

        // Support completion for string keys (quoted)
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withElementType(MamlTypes.STRING)
                .and(PlatformPatterns.psiElement().withParent(MamlKey::class.java)),
            provider
        )

        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withElementType(MamlTypes.UNTERMINATED_STRING).and(
                PlatformPatterns.psiElement().withParent(
                    MamlInvalidKey::class.java
                )
            ),
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
            val currentKeyName = currentKey?.name?.replace("${fakeString}\\s*$".toRegex(), "")

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
                        .withInsertHandler(QuoteAwareInsertHandler())
                )
            }
        }
    }
}

/**
 * Insert handler that prevents double-quotes when completing quoted keys.
 * Also adds colon with appropriate spacing based on code style settings.
 *
 * Handles cases like:
 * - "he<caret>" -> "hello" instead of ""hello""
 * - "he<caret> -> "hello" (adds closing quote for unclosed strings)
 * - Adds colon + spacing when completing a new key
 * - Doesn't add colon if one already exists (when modifying existing key-value pair)
 */
private class QuoteAwareInsertHandler : InsertHandler<LookupElement> {
    override fun handleInsert(context: InsertionContext, item: LookupElement) {
        val editor = context.editor
        val document = editor.document
        val element = context.file.findElementAt(context.startOffset)

        val keyElement = element?.parent
        if (keyElement !is MamlKey && keyElement !is MamlInvalidKey) return

        // Get code style settings
        val mamlSettings = CodeStyle.getCustomSettings(context.file, MamlCodeStyleSettings::class.java)

        // Find the actual token (IDENTIFIER, STRING, or UNTERMINATED_STRING)
        val tokenNode = keyElement.firstChild

        if (tokenNode is LeafPsiElement) {
            val tokenStartOffset = tokenNode.textRange.startOffset
            val tokenEndOffset = tokenNode.textRange.endOffset
            var finalCaretOffset: Int

            when {
                // Case 1: Properly closed quoted string "hello"
                tokenNode.elementType == MamlTypes.STRING -> {
                    // Replace only the content inside the quotes
                    val quoteStartOffset = tokenStartOffset + 1
                    val quoteEndOffset = tokenEndOffset - 1

                    document.replaceString(quoteStartOffset, quoteEndOffset, item.lookupString)
                    finalCaretOffset = quoteStartOffset + item.lookupString.length + 1
                }

                // Case 2: Unterminated string "hello
                tokenNode.elementType == MamlTypes.UNTERMINATED_STRING -> {
                    // Replace the content after the opening quote
                    val quoteStartOffset = tokenStartOffset + 1
                    document.replaceString(quoteStartOffset, tokenEndOffset, item.lookupString)
                    // Add the closing quote
                    document.insertString(quoteStartOffset + item.lookupString.length, "\"")
                    finalCaretOffset = quoteStartOffset + item.lookupString.length + 1
                }

                // Case 3: Unquoted identifier
                else -> {
                    document.replaceString(tokenStartOffset, tokenEndOffset, item.lookupString)
                    finalCaretOffset = tokenStartOffset + item.lookupString.length
                }
            }

            // Commit document changes so PSI is updated
            context.commitDocument()

            // Check if a colon already exists after this key
            val hasExistingColon = hasColonAfterKey(keyElement)

            if (!hasExistingColon) {
                // Add colon with spacing based on preferences
                val colonString = buildString {
                    if (mamlSettings.SPACE_BEFORE_COLON) append(" ")
                    append(":")
                    if (mamlSettings.SPACE_AFTER_COLON) append(" ")
                }

                document.insertString(finalCaretOffset, colonString)
                finalCaretOffset += colonString.length
            }

            editor.caretModel.moveToOffset(finalCaretOffset)
        }
    }

    /**
     * Checks if there's a colon token after the key element.
     * Skips whitespace when looking for the colon.
     */
    private fun hasColonAfterKey(keyElement: PsiElement): Boolean {
        var sibling = keyElement.nextSibling

        // Skip whitespace
        while (sibling is PsiWhiteSpace) {
            sibling = sibling.nextSibling
        }

        // Check if the next non-whitespace element is a colon
        return sibling is LeafPsiElement && sibling.elementType == MamlTypes.COLON
    }
}