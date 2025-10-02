package com.davidseptimus.maml.editor

import com.davidseptimus.maml.lang.psi.MamlArray
import com.davidseptimus.maml.lang.psi.MamlObject
import com.davidseptimus.maml.lang.psi.MamlTypes
import com.davidseptimus.maml.settings.MamlSettings
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

class MamlFoldingBuilder : FoldingBuilderEx() {

    override fun buildFoldRegions(
        root: PsiElement,
        document: Document,
        quick: Boolean
    ): Array<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()

        // Fold objects
        PsiTreeUtil.findChildrenOfType(root, MamlObject::class.java).forEach { obj ->
            val range = obj.textRange
            if (isMultiLine(range, document)) {
                descriptors.add(FoldingDescriptor(obj.node, range))
            }
        }

        // Fold arrays
        PsiTreeUtil.findChildrenOfType(root, MamlArray::class.java).forEach { array ->
            val range = array.textRange
            if (isMultiLine(range, document)) {
                descriptors.add(FoldingDescriptor(array.node, range))
            }
        }

        // Fold multiline strings
        PsiTreeUtil.findChildrenOfType(root, PsiElement::class.java).forEach { element ->
            if (element.node.elementType == MamlTypes.MULTILINE_STRING) {
                val range = element.textRange
                if (isMultiLine(range, document)) {
                    descriptors.add(FoldingDescriptor(element.node, range))
                }
            }
        }

        // Fold consecutive comment blocks
        foldCommentBlocks(root, document, descriptors)

        return descriptors.toTypedArray()
    }

    private fun foldCommentBlocks(
        root: PsiElement,
        document: Document,
        descriptors: MutableList<FoldingDescriptor>
    ) {
        val comments = PsiTreeUtil.findChildrenOfType(root, PsiElement::class.java)
            .filter { it.node.elementType == MamlTypes.COMMENT }
            .sortedBy { it.textRange.startOffset }

        if (comments.isEmpty()) return

        var blockStart: PsiElement? = null
        var lastComment: PsiElement? = null

        for (comment in comments) {
            if (blockStart == null) {
                // Start a new comment block
                blockStart = comment
                lastComment = comment
            } else {
                // Check if this comment is on the next line (or close enough to be consecutive)
                val lastLine = document.getLineNumber(lastComment!!.textRange.endOffset)
                val currentLine = document.getLineNumber(comment.textRange.startOffset)

                if (currentLine - lastLine <= 1) {
                    // Continue the block
                    lastComment = comment
                } else {
                    // End the previous block and start a new one
                    if (isMultiLine(TextRange(blockStart.textRange.startOffset, lastComment.textRange.endOffset), document)) {
                        descriptors.add(
                            FoldingDescriptor(
                                blockStart.node,
                                TextRange(blockStart.textRange.startOffset, lastComment.textRange.endOffset)
                            )
                        )
                    }
                    blockStart = comment
                    lastComment = comment
                }
            }
        }

        // Don't forget the last block
        if (blockStart != null && lastComment != null) {
            if (isMultiLine(TextRange(blockStart.textRange.startOffset, lastComment.textRange.endOffset), document)) {
                descriptors.add(
                    FoldingDescriptor(
                        blockStart.node,
                        TextRange(blockStart.textRange.startOffset, lastComment.textRange.endOffset)
                    )
                )
            }
        }
    }

    private fun isMultiLine(range: TextRange, document: Document): Boolean {
        val startLine = document.getLineNumber(range.startOffset)
        val endLine = document.getLineNumber(range.endOffset)
        return endLine > startLine
    }

    override fun getPlaceholderText(node: ASTNode): String {
        return when (node.elementType) {
            MamlTypes.OBJECT -> "{...}"
            MamlTypes.ARRAY -> "[...]"
            MamlTypes.MULTILINE_STRING -> getMultilineStringPlaceholder(node)
            MamlTypes.COMMENT -> getCommentPlaceholder(node)
            else -> "..."
        }
    }

    private fun getMultilineStringPlaceholder(node: ASTNode): String {
        val settings = MamlSettings.Companion.getInstance()
        val text = node.text

        // Remove the triple quotes
        val content = text.removePrefix("\"\"\"").removeSuffix("\"\"\"")

        // Get the first line
        val firstLine = content.lines().firstOrNull()?.trim() ?: ""

        if (firstLine.isEmpty()) {
            return "\"\"\"...\"\"\""
        }

        // Take first N words
        val preview = firstLine.split(Regex("\\s+"))
            .take(settings.multilineStringPreviewWords)
            .joinToString(" ")

        return "\"\"\"$preview...\"\"\""
    }

    private fun getCommentPlaceholder(node: ASTNode): String {
        val settings = MamlSettings.Companion.getInstance()
        val text = node.text

        // Get the first line of the comment block
        val firstLine = text.lines().firstOrNull() ?: return "# ..."

        // Remove the # and any leading whitespace
        val content = firstLine.removePrefix("#").trim()

        if (content.isEmpty()) {
            return "# ..."
        }

        // Take first N words
        val preview = content.split(Regex("\\s+"))
            .take(settings.commentPreviewWords)
            .joinToString(" ")

        return "# $preview..."
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        // Don't collapse anything by default
        return false
    }
}