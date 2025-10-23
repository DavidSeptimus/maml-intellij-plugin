package com.davidseptimus.maml.hints

import com.davidseptimus.maml.MamlBundle
import com.davidseptimus.maml.lang.psi.MamlArray
import com.intellij.codeInsight.hints.*
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Inlay hints provider that shows the number of items in arrays.
 */
@Suppress("UnstableApiUsage")
class MamlInlayHintsProvider : InlayHintsProvider<NoSettings> {

    override val key: SettingsKey<NoSettings> = SettingsKey("maml.hints.array")

    override val name: String = MamlBundle.message("inlayHints.arrayItemCount.name")

    override val previewText: String = """
        {
          items: [1, 2, 3, 4, 5]
          nested: [
            { id: 1 }
            { id: 2 }
          ]
        }
    """.trimIndent()

    override fun createConfigurable(settings: NoSettings): ImmediateConfigurable {
        return object : ImmediateConfigurable {
            override fun createComponent(listener: ChangeListener): JComponent {
                return JPanel()
            }
        }
    }

    override fun createSettings(): NoSettings = NoSettings()

    override fun getCollectorFor(
        file: PsiFile,
        editor: Editor,
        settings: NoSettings,
        sink: InlayHintsSink
    ): InlayHintsCollector {
        return object : FactoryInlayHintsCollector(editor) {
            override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
                if (element is MamlArray) {
                    val itemCount = element.items?.valueList?.size ?: 0
                    if (itemCount > 0) {
                        val closingBracket = element.lastChild
                        if (closingBracket?.text == "]") {
                            val text = if (itemCount == 1) "1 item" else "$itemCount items"
                            val presentation = factory.roundWithBackgroundAndSmallInset(factory.smallText(text))
                            sink.addInlineElement(
                                closingBracket.textRange.startOffset,
                                relatesToPrecedingText = true,
                                presentation = presentation,
                                placeAtTheEndOfLine = true
                            )
                        }
                    }
                }
                return true
            }
        }
    }
}