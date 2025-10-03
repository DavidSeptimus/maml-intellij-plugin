package com.davidseptimus.maml.formatter

import com.davidseptimus.maml.lang.psi.*
import com.davidseptimus.maml.lang.psi.MamlTypes.*
import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.lang.tree.util.siblings
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.formatter.FormatterUtil.isWhitespaceOrEmpty
import com.intellij.psi.tree.TokenSet

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
        MamlPsiUtil.hasElementType(node, MamlTypes.MEMBERS) -> Alignment.createAlignment(true)
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

    override fun getSpacing(child1: Block?, child2: Block): Spacing? =
        spacingBuilder.getSpacing(this, child1, child2)

    override fun getChildAttributes(newChildIndex: Int): ChildAttributes {
        return if (MamlPsiUtil.hasElementType(node, MAML_CONTAINERS)) {
            ChildAttributes(Indent.getNormalIndent(), null)
        } else if (node.psi is PsiFile) {
            ChildAttributes(Indent.getNoneIndent(), null)
        } else {
            ChildAttributes(null, null)
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

            else -> false
        }
    }

    override fun isLeaf(): Boolean = node.firstChildNode == null

    companion object {
        private val MAML_OPEN_BRACES = TokenSet.create(LBRACKET, LBRACE)
        private val MAML_CLOSE_BRACES = TokenSet.create(RBRACKET, RBRACE)
        private val MAML_ALL_BRACES = TokenSet.orSet(MAML_OPEN_BRACES, MAML_CLOSE_BRACES)
        private val MAML_CONTAINERS = TokenSet.create(ARRAY, OBJECT)

        private fun isWhitespaceOrEmpty(node: ASTNode): Boolean =
            node.elementType == TokenType.WHITE_SPACE || node.textLength == 0
    }
}