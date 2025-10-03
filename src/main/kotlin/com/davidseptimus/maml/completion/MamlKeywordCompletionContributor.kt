package com.davidseptimus.maml.completion

import com.davidseptimus.maml.lang.psi.MamlInvalidValue
import com.davidseptimus.maml.lang.psi.MamlKey
import com.davidseptimus.maml.lang.psi.MamlObject
import com.davidseptimus.maml.lang.psi.MamlTypes
import com.davidseptimus.maml.settings.MamlSettings
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext

/**
 * Provides keyword completions for MAML files.
 * Suggests keywords including true, false, and null in appropriate contexts.
 */
class MamlKeywordCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withElementType(MamlTypes.IDENTIFIER),
            KeywordCompletionProvider()
        )
    }

    private class KeywordCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet
        ) {
            if (!MamlSettings.getInstance().enableKeywordCompletion) return

            // Don't provide keyword completions in key positions
            val position = parameters.position
            if (position.parentOfType<MamlKey>() != null) return

            result.addElement(
                LookupElementBuilder.create("true")
                    .withTypeText("boolean")
                    .bold()
            )
            result.addElement(
                LookupElementBuilder.create("false")
                    .withTypeText("boolean")
                    .bold()
            )
            result.addElement(
                LookupElementBuilder.create("null")
                    .withTypeText("null")
                    .bold()
            )
        }
    }
}