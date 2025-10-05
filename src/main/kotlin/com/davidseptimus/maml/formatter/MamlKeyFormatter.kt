package com.davidseptimus.maml.formatter

import com.davidseptimus.maml.lang.MamlLanguage
import com.davidseptimus.maml.lang.psi.MamlKey
import com.davidseptimus.maml.lang.psi.MamlTypes
import com.intellij.application.options.CodeStyle
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.impl.source.codeStyle.CodeEditUtil
import com.intellij.psi.impl.source.codeStyle.PreFormatProcessor
import com.intellij.psi.impl.source.tree.LeafPsiElement

/**
 * Pre-format processor that modifies key quoting based on the configured style.
 *
 * Supports three modes:
 * 1. DO_NOT_MODIFY: Leaves keys as-is
 * 2. REMOVE_QUOTES: Unquotes string keys that can be safely represented as identifiers
 * 3. ADD_QUOTES: Quotes all identifier keys as strings
 *
 * A quoted string key can be safely unquoted if:
 * - It contains only alphanumeric characters, dashes, and underscores
 * - It is not a reserved keyword (true, false, null)
 *
 * Examples:
 * ```
 * REMOVE_QUOTES:
 *   "simple-key": value  -> simple-key: value
 *   "hello_world": 123   -> hello_world: 123
 *   "true": value        -> "true": value  (kept quoted - reserved keyword)
 *
 * ADD_QUOTES:
 *   simple-key: value    -> "simple-key": value
 *   hello_world: 123     -> "hello_world": 123
 * ```
 */
class MamlKeyFormatter : PreFormatProcessor {

    override fun process(element: ASTNode, range: TextRange): TextRange {
        val psi = element.psi
        val file = psi.containingFile ?: return range

        if (!file.language.isKindOf(MamlLanguage)) {
            return range
        }

        val customSettings = CodeStyle.getCustomSettings(file, MamlCodeStyleSettings::class.java)
        val quotingStyle =
            MamlCodeStyleSettings.KeyQuotingStyle.entries.find { it.id == customSettings.KEY_QUOTING_STYLE }
                ?: MamlCodeStyleSettings.KeyQuotingStyle.DO_NOT_MODIFY

        when (quotingStyle) {
            MamlCodeStyleSettings.KeyQuotingStyle.REMOVE_QUOTES -> {
                unquoteKeys(element)
            }

            MamlCodeStyleSettings.KeyQuotingStyle.ADD_QUOTES -> {
                quoteKeys(element)
            }

            else -> {}
        }

        return TextRange.create(element.startOffset, element.startOffset + element.textLength)
    }

    private fun unquoteKeys(node: ASTNode) {
        var child = node.firstChildNode
        while (child != null) {
            val next = child.treeNext

            if (child.elementType == MamlTypes.STRING && child.psi.parent is MamlKey) {
                tryUnquoteKey(node, child)
            } else {
                // Recursively process children
                unquoteKeys(child)
            }

            child = next
        }
    }

    /**
     * Attempts to unquote a string key if it can be safely represented as an identifier.
     */
    private fun tryUnquoteKey(parent: ASTNode, stringNode: ASTNode) {
        if (stringNode !is LeafPsiElement) {
            return
        }

        val text = stringNode.text
        if (!text.startsWith("\"") || !text.endsWith("\"")) {
            return
        }

        // Extract the content between quotes
        val content = text.substring(1, text.length - 1)

        if (canBeUnquoted(content)) {
            // Replace the quoted string with an unquoted identifier
            val identifierNode = LeafPsiElement(MamlTypes.IDENTIFIER, content)
            CodeEditUtil.setNodeGenerated(identifierNode, true)
            parent.replaceChild(stringNode, identifierNode)
        }
    }

    /**
     * Determines if a string content can be safely represented as an identifier.
     *
     * Returns true if:
     * 1. The string is not empty
     * 2. It contains only alphanumeric characters, dashes, and underscores
     * 3. It is not a reserved keyword (true, false, null)
     */
    private fun canBeUnquoted(content: String): Boolean {
        if (content.isEmpty()) {
            return false
        }

        // Check if it's a reserved keyword
        if (content == "true" || content == "false" || content == "null") {
            return false
        }

        // Check if it matches the identifier pattern: [a-zA-Z0-9_-]+
        return content.all { it.isLetterOrDigit() || it == '_' || it == '-' }
    }

    /**
     * Converts all identifier keys to quoted string keys.
     */
    private fun quoteKeys(node: ASTNode) {
        var child = node.firstChildNode
        while (child != null) {
            val next = child.treeNext

            if (child.elementType == MamlTypes.IDENTIFIER && child.psi.parent is MamlKey) {
                tryQuoteKey(node, child)
            } else {
                // Recursively process children
                quoteKeys(child)
            }

            child = next
        }
    }

    /**
     * Attempts to quote an identifier key as a string.
     */
    private fun tryQuoteKey(parent: ASTNode, identifierNode: ASTNode) {
        if (identifierNode !is LeafPsiElement) {
            return
        }

        val content = identifierNode.text
        val quotedString = "\"$content\""

        // Replace the identifier with a quoted string
        val stringNode = LeafPsiElement(MamlTypes.STRING, quotedString)
        CodeEditUtil.setNodeGenerated(stringNode, true)
        parent.replaceChild(identifierNode, stringNode)
    }
}