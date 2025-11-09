package com.davidseptimus.maml.formatter

import com.davidseptimus.maml.lang.psi.MamlArray
import com.davidseptimus.maml.lang.psi.MamlKeyValue
import com.davidseptimus.maml.lang.psi.MamlObject
import com.davidseptimus.maml.lang.psi.MamlPsiUtil
import com.davidseptimus.maml.lang.psi.MamlTypes.*
import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.tree.TokenSet


private val MAML_OPEN_BRACES = TokenSet.create(LBRACKET, LBRACE)
private val MAML_CLOSE_BRACES = TokenSet.create(RBRACKET, RBRACE)
private val MAML_ALL_BRACES = TokenSet.orSet(MAML_OPEN_BRACES, MAML_CLOSE_BRACES)
private val MAML_CONTAINERS = TokenSet.create(ARRAY, OBJECT)

class MamlBlock(
    private val parent: MamlBlock?,
    private val node: ASTNode,
    private val customSettings: MamlCodeStyleSettings,
    private val alignment: Alignment?,
    private val indent: Indent,
    private val wrap: Wrap?,
    private val spacingBuilder: SpacingBuilder
) : ASTBlock {

    private val psiElement: PsiElement = node.psi
    private val childWrap: Wrap? = when (psiElement) {
        is MamlObject -> Wrap.createWrap(customSettings.OBJECT_WRAPPING, true)
        is MamlArray -> Wrap.createWrap(customSettings.ARRAY_WRAPPING, true)
        else -> null
    }
    private val propertyValueAlignment: Alignment? = when {
        psiElement is MamlObject -> Alignment.createAlignment(true)
        MamlPsiUtil.hasElementType(node, MEMBERS) -> Alignment.createAlignment(true)
        else -> null
    }

    private var subBlocks: List<Block>? = null

    override fun getNode(): ASTNode = node

    override fun getTextRange(): TextRange = node.textRange

    override fun getSubBlocks(): List<Block> {
        if (subBlocks == null) {
            val propertyAlignment = customSettings.PROPERTY_ALIGNMENT
            val children = node.getChildren(null)
            subBlocks = children
                .filterNot { isWhitespaceOrEmpty(it) }
                .map { makeSubBlock(it, propertyAlignment) }
        }
        return subBlocks!!
    }

    private fun makeSubBlock(childNode: ASTNode, propertyAlignment: Int): Block {
        var indent = Indent.getNoneIndent()
        var alignment: Alignment? = null
        var wrap: Wrap? = null

        if (MamlPsiUtil.hasElementType(node, MAML_CONTAINERS)) {
            if (MamlPsiUtil.hasElementType(childNode, COMMA)) {
                wrap = Wrap.createWrap(WrapType.NONE, true)
            } else if (MamlPsiUtil.hasElementType(childNode, COMMENT)) {
                // Check if this is an inline comment (on same line as code)
                if (isInlineComment(childNode)) {
                    // Inline comments should not wrap to preserve their position on the same line
                    wrap = Wrap.createWrap(WrapType.NONE, true)
                } else {
                    // Line comments get normal wrapping and indentation
                    wrap = this.childWrap
                    indent = Indent.getNormalIndent()
                }
            } else if (!MamlPsiUtil.hasElementType(childNode, MAML_ALL_BRACES)) {
                wrap = this.childWrap
                indent = Indent.getNormalIndent()
            } else if (MamlPsiUtil.hasElementType(childNode, MAML_OPEN_BRACES)) {
                if (MamlPsiUtil.isPropertyValue(psiElement) &&
                    propertyAlignment == MamlCodeStyleSettings.ALIGN_PROPERTY_ON_VALUE
                ) {
                    parent?.parent?.propertyValueAlignment?.let {
                        alignment = it
                    }
                }
            } else if (MamlPsiUtil.hasElementType(childNode, MAML_CLOSE_BRACES)) {
                wrap = this.childWrap
                // Closing braces alignment depends on context:
                // - If container is a property value (key: {...}), align with the key
                // - If container is an array item (e.g., [1,2,3,{...}]), align with start of line (array child level)
                // - Otherwise, align at the container's level
                indent = when {
                    MamlPsiUtil.isPropertyValue(psiElement.parent) || MamlPsiUtil.isArrayItem(psiElement.parent) -> {
                        // Property value case: braces should align with key
                        // Array item case: align with array's child indent level (start of items)
                        // The opening brace may appear after other items like [1,2,3,{
                        // Closing should align with where '1' starts, not where '{' is
                        Indent.getNoneIndent()
                    }

                    else -> {
                        // Other cases: use getSpaceIndent for proper wrapping behavior
                        Indent.getSpaceIndent(0, true)
                    }
                }
            }
        }
        // Handle properties alignment
        else if (MamlPsiUtil.hasElementType(node, KEY_VALUE)) {
            parent?.propertyValueAlignment?.let { parentAlignment ->
                if (MamlPsiUtil.hasElementType(childNode, COLON) &&
                    propertyAlignment == MamlCodeStyleSettings.ALIGN_PROPERTY_ON_COLON
                ) {
                    alignment = parentAlignment
                } else if (MamlPsiUtil.isPropertyValue(childNode.psi) &&
                    propertyAlignment == MamlCodeStyleSettings.ALIGN_PROPERTY_ON_VALUE
                ) {
                    if (!MamlPsiUtil.hasElementType(childNode, MAML_CONTAINERS)) {
                        alignment = parentAlignment
                    }
                }
            }
        } else if (childNode.elementType === MULTILINE_STRING) {
            // Multiline strings should preserve their internal indentation by aligning at absolute none indent level
            indent = Indent.getAbsoluteNoneIndent()
        }

        return MamlBlock(
            this,
            childNode,
            customSettings,
            alignment,
            indent,
            wrap,
            spacingBuilder
        )
    }

    override fun getWrap(): Wrap? = wrap

    override fun getIndent(): Indent = indent

    override fun getAlignment(): Alignment? = alignment

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        // Prevent line breaks before inline comments
        if (child1 != null && child2 is MamlBlock) {
            val node1 = (child1 as? MamlBlock)?.node
            val node2 = child2.node

            if (node1 != null &&
                MamlPsiUtil.hasElementType(node2, COMMENT) &&
                isInlineComment(node2)
            ) {
                // Force single space, no line breaks allowed for inline comments
                return Spacing.createSpacing(1, 1, 0, false, 0)
            }

            // Handle multiline string wrapping when enabled
            if (customSettings.WRAP_MULTILINE_STRINGS &&
                node1 != null &&
                MamlPsiUtil.hasElementType(node1, COLON) &&
                MamlPsiUtil.hasElementType(node2, VALUE) &&
                MamlPsiUtil.hasElementType(node, KEY_VALUE)
            ) {
                // Check if the value contains a multiline string
                val valueChildren = node2.getChildren(null)
                val hasMultilineString = valueChildren.any {
                    MamlPsiUtil.hasElementType(it, MULTILINE_STRING)
                }
                if (hasMultilineString) {
                    // Force line break after colon before multiline string
                    return Spacing.createSpacing(0, 0, 1, true, 0)
                }
            }
        }

        return spacingBuilder.getSpacing(this, child1, child2)
    }

    override fun getChildAttributes(newChildIndex: Int): ChildAttributes {
        return if (MamlPsiUtil.hasElementType(node, MAML_CONTAINERS)) {
            ChildAttributes(Indent.getNormalIndent(), null)
        } else {
            ChildAttributes(Indent.getNoneIndent(), null)
        }
    }

    override fun isIncomplete(): Boolean {
        val lastChildNode = node.lastChildNode
        return when {
            MamlPsiUtil.hasElementType(node, OBJECT) ->
                lastChildNode != null && lastChildNode.elementType != RBRACE

            MamlPsiUtil.hasElementType(node, ARRAY) ->
                lastChildNode != null && lastChildNode.elementType != RBRACKET

            MamlPsiUtil.hasElementType(node, KEY_VALUE) ->
                (psiElement as MamlKeyValue).value == null

            MamlPsiUtil.hasElementType(node, INCOMPLETE_KEY_VALUE) ->
                true  // Always incomplete - missing colon and value

            MamlPsiUtil.hasElementType(node, INVALID_VALUE) ->
                true  // Always incomplete - partial/invalid value

            else -> false
        }
    }

    override fun isLeaf(): Boolean = node.firstChildNode == null

    private fun isWhitespaceOrEmpty(node: ASTNode): Boolean =
        node.elementType == TokenType.WHITE_SPACE || node.textLength == 0

    /**
     * Determines if a comment node is an inline comment (appears after code on the same line)
     * vs a line comment (appears on its own line).
     *
     * @param commentNode The comment node to check
     * @return true if the comment is inline (no newline between it and previous element)
     */
    private fun isInlineComment(commentNode: ASTNode): Boolean {
        // Find previous non-whitespace sibling
        var prevSibling = commentNode.treePrev
        while (prevSibling != null && prevSibling.elementType == TokenType.WHITE_SPACE) {
            prevSibling = prevSibling.treePrev
        }

        if (prevSibling == null) return false

        // Check the text between the end of the previous sibling and the start of the comment
        // If it contains a newline, the comment is on its own line (line comment)
        // Otherwise, it's an inline comment
        val textBetween = commentNode.psi.containingFile.text.substring(
            prevSibling.textRange.endOffset,
            commentNode.startOffset
        )

        return !textBetween.contains('\n')
    }
}
