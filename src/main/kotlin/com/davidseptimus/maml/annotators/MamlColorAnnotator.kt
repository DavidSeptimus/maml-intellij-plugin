package com.davidseptimus.maml.annotators

import com.davidseptimus.maml.lang.psi.MamlTypes
import com.davidseptimus.maml.lang.psi.MamlValueElement
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.ui.ColorChooserService
import com.intellij.ui.ColorPickerListener
import com.intellij.ui.ColorUtil
import com.intellij.util.ui.ColorIcon
import java.awt.Color
import javax.swing.Icon

/**
 * Annotator that adds gutter icons for hex color values with a color picker.
 */
class MamlColorAnnotator : Annotator {

    private val hexColorPattern = Regex("^#([0-9A-Fa-f]{3}|[0-9A-Fa-f]{6}|[0-9A-Fa-f]{8})$")

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is MamlValueElement) return

        // Check if this is a string value
        val child = element.firstChild
        if (child !is LeafPsiElement) return
        if (child.elementType != MamlTypes.STRING) return

        val text = element.text.trim().removeSurrounding("\"")
        if (!hexColorPattern.matches(text)) return

        val color = parseColor(text) ?: return

        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .gutterIconRenderer(ColorIconRenderer(element, color))
            .create()
    }

    private fun parseColor(colorText: String): Color? {
        if (!colorText.startsWith("#")) return null

        return try {
            val hex = colorText.substring(1)
            when (hex.length) {
                3 -> {
                    // #RGB -> #RRGGBB
                    val r = hex[0].toString().repeat(2)
                    val g = hex[1].toString().repeat(2)
                    val b = hex[2].toString().repeat(2)
                    ColorUtil.fromHex("$r$g$b")
                }
                6 -> {
                    // #RRGGBB
                    ColorUtil.fromHex(hex)
                }
                8 -> {
                    // #RRGGBBAA
                    ColorUtil.fromHex(hex)
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    private class ColorIconRenderer(
        private val element: PsiElement,
        private val color: Color
    ) : GutterIconRenderer() {

        override fun getIcon(): Icon {
            return ColorIcon(12, color)
        }

        override fun getClickAction(): AnAction {
            return object : AnAction() {
                override fun actionPerformed(event: AnActionEvent) {
                    val editor = event.getData(CommonDataKeys.EDITOR) ?: return
                    val project = element.project

                    val listener = object : ColorPickerListener {
                        override fun colorChanged(newColor: Color) {
                            val newColorHex = "#${ColorUtil.toHex(newColor)}"
                            WriteCommandAction.runWriteCommandAction(project) {
                                val child = element.firstChild
                                if (child is LeafPsiElement && child.elementType == MamlTypes.STRING) {
                                    child.replaceWithText("\"$newColorHex\"")
                                }
                            }
                        }

                        override fun closed(color: Color?) {
                            // No action needed on close
                        }
                    }

                    ColorChooserService.instance.showDialog(
                        project,
                        editor.component,
                        "Choose Color",
                        color,
                        true,
                        listOf(listener),
                        true
                    )
                }
            }
        }

        override fun getTooltipText(): String {
            return "Color: #${ColorUtil.toHex(color)}"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ColorIconRenderer) return false
            return color == other.color && element == other.element
        }

        override fun hashCode(): Int {
            var result = element.hashCode()
            result = 31 * result + color.hashCode()
            return result
        }
    }
}