package com.davidseptimus.maml.hints

import com.davidseptimus.maml.lang.psi.MamlArray
import com.intellij.codeInsight.hints.declarative.*
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

/**
 * Declarative inlay hints provider that shows the number of items in arrays.
 */
class MamlInlayHintsProvider : InlayHintsProvider {

    companion object {
        const val PROVIDER_ID: String = "maml.hints.array"
    }

    override fun createCollector(file: PsiFile, editor: Editor): InlayHintsCollector {
        return MamlArrayItemCountCollector()
    }

    private class MamlArrayItemCountCollector : SharedBypassCollector {
        override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
            if (element is MamlArray) {
                val itemCount = element.items?.valueList?.size ?: 0
                if (itemCount > 0) {
                    val closingBracket = element.lastChild
                    if (closingBracket?.text == "]") {
                        val text = if (itemCount == 1) "1 item" else "$itemCount items"
                        sink.addPresentation(
                            InlineInlayPosition(closingBracket.textRange.endOffset, relatedToPrevious = true),
                            hintFormat = HintFormat(
                                HintColorKind.TextWithoutBackground, HintFontSize.ABitSmallerThanInEditor,
                                HintMarginPadding.MarginAndSmallerPadding
                            )
                        ) {
                            text(text)
                        }
                    }
                }
            }
        }
    }
}