package com.davidseptimus.maml.intentions

import com.davidseptimus.maml.MamlBundle
import com.davidseptimus.maml.formatter.MamlCodeStyleSettings
import com.davidseptimus.maml.lang.MamlLanguage
import com.davidseptimus.maml.lang.psi.*
import com.davidseptimus.maml.lang.psi.MamlPsiUtil.isPrimitiveValue
import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.elementType

/**
 * Intention action that recursively expands/unfolds container contents onto multiple lines.
 * Respects user's code style settings for comma usage.
 */
class ExpandContentsIntention : PsiElementBaseIntentionAction(), IntentionAction {

    override fun getText(): String = MamlBundle.message("intention.expand.contents.text")

    override fun getFamilyName(): String = MamlBundle.message("intention.expand.contents.family")

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        val target = getTargetContainer(element) ?: return false
        if (MamlPsiUtil.isEmptyContainer(element) || target is MamlObject && isPrimitiveValue(element)) return false
        return when (target) {
            is MamlArray -> (target.items?.valueList?.size ?: 0) > 0
            is MamlObject -> (target.members?.keyValueList?.size ?: 0) > 0
            else -> false
        }
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        if (editor == null) return

        val target = getTargetContainer(element) ?: return
        val file = element.containingFile

        // Get code style settings
        val codeStyleSettings = CodeStyle.getSettings(file)
        val customSettings = codeStyleSettings.getCustomSettings(MamlCodeStyleSettings::class.java)
        val commonSettings = codeStyleSettings.getCommonSettings(MamlLanguage)

        val useCommas = !customSettings.REMOVE_COMMAS
        val indentSize = commonSettings.indentOptions?.INDENT_SIZE ?: 4
        val useTab = commonSettings.indentOptions?.USE_TAB_CHARACTER ?: false

        // Build the expanded text
        val indentString = if (useTab) "\t" else " ".repeat(indentSize)
        val expandedText = buildExpandedText(target, useCommas, indentString)

        // Replace the container's text content (everything between braces/brackets)
        val start = when (target) {
            is MamlArray -> {
                val lbracket = target.node.findChildByType(MamlTypes.LBRACKET)
                lbracket?.startOffset?.plus(1) ?: return
            }

            is MamlObject -> {
                val lbrace = target.node.findChildByType(MamlTypes.LBRACE)
                lbrace?.startOffset?.plus(1) ?: return
            }

            else -> return
        }

        val end = when (target) {
            is MamlArray -> {
                val rbracket = target.node.findChildByType(MamlTypes.RBRACKET)
                rbracket?.startOffset ?: return
            }

            is MamlObject -> {
                val rbrace = target.node.findChildByType(MamlTypes.RBRACE)
                rbrace?.startOffset ?: return
            }

            else -> return
        }

        editor.document.replaceString(start, end, expandedText)
    }

    /**
     * Get the target container:
     * - If element is a key, get the value's container (if value is array/object)
     * - Otherwise, get the nearest ancestor container
     */
    private fun getTargetContainer(element: PsiElement): PsiElement? {
        // Check if we're on a key
        val parent = element.parent
        if (parent is MamlKey) {
            val keyValue = parent.parent as? MamlKeyValue
            val value = keyValue?.value
            // If the value is a container, use it
            val firstChild = value?.firstChild
            if (firstChild is MamlArray || firstChild is MamlObject) {
                return firstChild
            }
        }

        // Otherwise find nearest container ancestor (for structural tokens, check parent)
        if (STRUCTURAL_TOKENS.contains(element.elementType)) {
            return MamlPsiUtil.container(element.parent)
        }
        return MamlPsiUtil.container(element)
    }

    /**
     * Build the expanded text representation of a container.
     */
    private fun buildExpandedText(
        container: PsiElement,
        useCommas: Boolean,
        indentString: String,
    ): String {
        return when (container) {
            is MamlArray -> buildExpandedArray(container, useCommas, indentString, 0)
            is MamlObject -> buildExpandedObject(container, useCommas, indentString, 0)
            else -> ""
        }
    }

    private fun buildExpandedArray(
        array: MamlArray,
        useCommas: Boolean,
        indentString: String,
        currentDepth: Int
    ): String {
        val items = array.items ?: return ""
        val values = items.valueList

        val indent = indentString.repeat(currentDepth + 1)
        val separator = if (useCommas) "," else ""

        val expandedItems = values.joinToString(separator + "\n") { value ->
            val expandedValue = buildExpandedValue(value, useCommas, indentString, currentDepth + 1)
            "$indent$expandedValue"
        }

        return "\n$expandedItems\n${indentString.repeat(currentDepth)}"
    }

    private fun buildExpandedObject(
        obj: MamlObject,
        useCommas: Boolean,
        indentString: String,
        currentDepth: Int
    ): String {
        val members = obj.members ?: return ""
        val keyValues = members.keyValueList

        val indent = indentString.repeat(currentDepth + 1)
        val separator = if (useCommas) "," else ""

        val expandedItems = keyValues.joinToString(separator + "\n") { kv ->
            val key = kv.key.text
            val value = kv.value?.let { buildExpandedValue(it, useCommas, indentString, currentDepth + 1) } ?: ""
            "$indent$key: $value"
        }

        return "\n$expandedItems\n${indentString.repeat(currentDepth)}"
    }

    private fun buildExpandedValue(
        value: MamlValue,
        useCommas: Boolean,
        indentString: String,
        currentDepth: Int
    ): String {
        return when (val child = value.firstChild) {
            is MamlArray -> {
                val expanded = buildExpandedArray(child, useCommas, indentString, currentDepth)
                "[$expanded]"
            }

            is MamlObject -> {
                val expanded = buildExpandedObject(child, useCommas, indentString, currentDepth)
                "{$expanded}"
            }

            else -> value.text.trim()
        }
    }

    override fun startInWriteAction(): Boolean = true
}

private val STRUCTURAL_TOKENS = TokenSet.create(
    MamlTypes.LBRACE,
    MamlTypes.RBRACE,
    MamlTypes.LBRACKET,
    MamlTypes.RBRACKET,
)