package com.davidseptimus.maml.completion

import com.davidseptimus.maml.lang.psi.MamlInvalidValue
import com.davidseptimus.maml.lang.psi.MamlTypes
import com.davidseptimus.maml.settings.MamlSettings
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.DumbAware
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext

/**
 * Provides keyword completions for MAML files.
 * Suggests keywords including true, false, and null in appropriate contexts.
 */
class MamlKeywordCompletionContributor : CompletionContributor(), DumbAware {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withElementType(MamlTypes.IDENTIFIER).and(
                PlatformPatterns.psiElement().withParent(
                    MamlInvalidValue::class.java
                )
            ),
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