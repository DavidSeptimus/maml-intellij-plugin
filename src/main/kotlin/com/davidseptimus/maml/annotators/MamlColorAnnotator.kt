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
    private val rgbPattern =
        Regex("^rgb\\s*\\(\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*\\)$", RegexOption.IGNORE_CASE)
    private val rgbaPattern = Regex(
        "^rgba\\s*\\(\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*,\\s*(0|1|0?\\.\\d+|1\\.0+)\\s*\\)$",
        RegexOption.IGNORE_CASE
    )

    private enum class ColorFormat {
        HEX_RGB,      // #RGB
        HEX_RRGGBB,   // #RRGGBB
        HEX_RGBA,     // #RRGGBBAA
        RGB,          // rgb(r, g, b)
        RGBA          // rgba(r, g, b, a)
    }

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is MamlValueElement) return

        // Check if this is a string value
        val child = element.firstChild
        if (child !is LeafPsiElement) return
        if (child.elementType != MamlTypes.STRING) return

        val text = element.text.trim().removeSurrounding("\"")
        val color = parseColor(text) ?: return

        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .gutterIconRenderer(ColorIconRenderer(element, color, hexColorPattern, rgbPattern, rgbaPattern))
            .create()
    }

    private fun parseColor(colorText: String): Color? {
        // Try hex color
        if (hexColorPattern.matches(colorText)) {
            return parseHexColor(colorText)
        }

        // Try rgb() or rgba()
        rgbPattern.matchEntire(colorText)?.let { match ->
            return parseRgbColor(match)
        }

        rgbaPattern.matchEntire(colorText)?.let { match ->
            return parseRgbaColor(match)
        }

        return null
    }

    private fun parseHexColor(colorText: String): Color? {
        return try {
            val hex = colorText.substring(1)
            when (hex.length) {
                3 -> ColorUtil.fromHex(hex) // #RGB
                6 -> ColorUtil.fromHex(hex) // #RRGGBB
                8 -> ColorUtil.fromHex(hex) // #RRGGBBAA
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun parseRgbColor(match: MatchResult): Color? {
        return try {
            val r = match.groupValues[1].toInt()
            val g = match.groupValues[2].toInt()
            val b = match.groupValues[3].toInt()

            if (r in 0..255 && g in 0..255 && b in 0..255) {
                Color(r, g, b)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun parseRgbaColor(match: MatchResult): Color? {
        return try {
            val r = match.groupValues[1].toInt()
            val g = match.groupValues[2].toInt()
            val b = match.groupValues[3].toInt()
            val a = (match.groupValues[4].toFloat() * 255).toInt()

            if (r in 0..255 && g in 0..255 && b in 0..255 && a in 0..255) {
                Color(r, g, b, a)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private class ColorIconRenderer(
        private val element: PsiElement,
        private val color: Color,
        private val hexColorPattern: Regex,
        private val rgbPattern: Regex,
        private val rgbaPattern: Regex
    ) : GutterIconRenderer() {

        override fun getIcon(): Icon {
            return ColorIcon(12, color)
        }

        override fun getClickAction(): AnAction {
            return object : AnAction() {
                override fun actionPerformed(event: AnActionEvent) {
                    val editor = event.getData(CommonDataKeys.EDITOR) ?: return
                    val project = element.project

                    // Determine original format
                    val originalText = element.text.trim().removeSurrounding("\"")
                    val originalFormat = detectColorFormat(originalText)

                    val listener = object : ColorPickerListener {
                        override fun colorChanged(newColor: Color) {
                            val newColorText = formatColor(newColor, originalFormat)
                            WriteCommandAction.runWriteCommandAction(project) {
                                val child = element.firstChild
                                if (child is LeafPsiElement && child.elementType == MamlTypes.STRING) {
                                    child.replaceWithText("\"$newColorText\"")
                                }
                            }
                        }

                        override fun closed(@Suppress("UNUSED_PARAMETER") color: Color?) {
                            // No action needed on close
                        }
                    }

                    // Determine if we should enable opacity based on original color format
                    val enableOpacity = originalFormat == ColorFormat.RGBA || originalFormat == ColorFormat.HEX_RGBA

                    ColorChooserService.instance.showDialog(
                        project,
                        editor.component,
                        "Choose Color",
                        color,
                        enableOpacity,
                        listOf(listener),
                        true
                    )
                }
            }
        }

        private fun detectColorFormat(colorText: String): ColorFormat {
            return when {
                rgbaPattern.matches(colorText) -> ColorFormat.RGBA
                rgbPattern.matches(colorText) -> ColorFormat.RGB
                hexColorPattern.matches(colorText) -> {
                    val hex = colorText.substring(1)
                    when (hex.length) {
                        3 -> ColorFormat.HEX_RGB
                        6 -> ColorFormat.HEX_RRGGBB
                        8 -> ColorFormat.HEX_RGBA
                        else -> ColorFormat.HEX_RRGGBB
                    }
                }

                else -> ColorFormat.HEX_RRGGBB
            }
        }

        private fun formatColor(color: Color, format: ColorFormat): String {
            return when (format) {
                ColorFormat.RGB -> "rgb(${color.red}, ${color.green}, ${color.blue})"
                ColorFormat.RGBA -> {
                    val alpha = String.format("%.2f", color.alpha / 255.0)
                    "rgba(${color.red}, ${color.green}, ${color.blue}, $alpha)"
                }

                ColorFormat.HEX_RGB -> {
                    // Convert to 3-character hex if possible
                    val r = color.red.toString(16).padStart(2, '0')
                    val g = color.green.toString(16).padStart(2, '0')
                    val b = color.blue.toString(16).padStart(2, '0')
                    if (r[0] == r[1] && g[0] == g[1] && b[0] == b[1]) {
                        "#${r[0]}${g[0]}${b[0]}"
                    } else {
                        "#$r$g$b"
                    }
                }

                ColorFormat.HEX_RRGGBB -> "#${ColorUtil.toHex(color).substring(0, 6)}"
                ColorFormat.HEX_RGBA -> "#${ColorUtil.toHex(color)}"
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