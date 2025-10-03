package com.davidseptimus.maml.formatter

import com.davidseptimus.maml.lang.MamlLanguage
import com.davidseptimus.maml.lang.psi.MamlTypes
import com.intellij.application.options.CodeStyle
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.impl.source.codeStyle.CodeEditUtil
import com.intellij.psi.impl.source.codeStyle.PreFormatProcessor
import com.intellij.psi.impl.source.tree.LeafPsiElement

/**
 * Pre-format processor that ensures proper spacing after the hash (#) in MAML comments.
 * Supports three modes: ANY (no enforcement), AT_LEAST_ONE (ensure at least one space),
 * and EXACTLY_ONE (normalize to exactly one space).
 */
class MamlCommentLeadingSpaceInserter : PreFormatProcessor {

    override fun process(element: ASTNode, range: TextRange): TextRange {
        val psi = element.psi
        val file = psi.containingFile ?: return range

        if (!file.language.isKindOf(MamlLanguage)) {
            return range
        }

        val customSettings = CodeStyle.getCustomSettings(file, MamlCodeStyleSettings::class.java)
        val spaceOption = customSettings.SPACE_AFTER_COMMENT_HASH

        if (spaceOption != CommentHashSpaceOptions.ANY.id) {
            processComments(element, spaceOption)
        }

        return TextRange.create(element.startOffset, element.startOffset + element.textLength)
    }

    private fun processComments(node: ASTNode, spaceOption: Int) {
        var child = node.firstChildNode
        while (child != null) {
            val next = child.treeNext
            if (child.elementType == MamlTypes.COMMENT) {
                val text = child.text
                if (text.isNotEmpty() && text[0] == '#') {
                    val newText = when (spaceOption) {
                        CommentHashSpaceOptions.AT_LEAST_ONE.id -> ensureAtLeastOneSpace(text)
                        CommentHashSpaceOptions.EXACTLY_ONE.id -> ensureExactlyOneSpace(text)
                        else -> null
                    }

                    if (newText != null && newText != text) {
                        val newComment = LeafPsiElement(MamlTypes.COMMENT, newText)
                        // Mark as generated so formatter doesn't check old indentation
                        CodeEditUtil.setNodeGenerated(newComment, true)
                        node.replaceChild(child, newComment)
                    }
                }
            } else {
                // Recursively process children
                processComments(child, spaceOption)
            }
            child = next
        }
    }

    private fun ensureAtLeastOneSpace(text: String): String? {
        // If there's already at least one space after #, do nothing
        if (text.length > 1 && text[1] == ' ') {
            return null
        }
        // Otherwise, insert exactly one space
        return "#" + " " + text.substring(1)
    }

    private fun ensureExactlyOneSpace(text: String): String? {
        if (text.length == 1) {
            // Just "#" - add one space
            return "# "
        }

        // Count leading spaces after #
        var spaceCount = 0
        var idx = 1
        while (idx < text.length && text[idx] == ' ') {
            spaceCount++
            idx++
        }

        // If already exactly one space, do nothing
        if (spaceCount == 1) {
            return null
        }

        // Otherwise normalize to exactly one space
        val contentAfterSpaces = text.substring(idx)
        return "# $contentAfterSpaces"
    }
}
