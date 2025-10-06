package com.davidseptimus.maml.intentions

import com.davidseptimus.maml.MamlBundle
import com.davidseptimus.maml.lang.psi.*
import com.davidseptimus.maml.lang.psi.MamlPsiUtil.isPrimitiveValue
import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement

/**
 * Intention action that recursively inlines/flattens container contents onto a single line,
 * adding commas as needed between items.
 */
class InlineContentsIntention : PsiElementBaseIntentionAction(), IntentionAction {

    override fun getText(): String = MamlBundle.message("intention.inline.contents.text")

    override fun getFamilyName(): String = MamlBundle.message("intention.inline.contents.family")

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        val target = MamlPsiUtil.container(element) ?: getTargetContainer(element) ?: return false
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

        // Get code style settings for spaces inside braces/brackets
        val settings = CodeStyle.getSettings(project)
        val mamlSettings = settings.getCommonSettings("Maml")
        val spaceInArray = mamlSettings.SPACE_WITHIN_BRACKETS
        val spaceInObject = mamlSettings.SPACE_WITHIN_BRACES

        // Build the inlined text
        val inlinedText = buildInlinedText(target, spaceInArray, spaceInObject)

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

        editor.document.replaceString(start, end, inlinedText)
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

        // Otherwise find nearest container ancestor
        return MamlPsiUtil.container(element)
    }

    /**
     * Build the inlined text representation of a container, respecting code style spacing.
     */
    private fun buildInlinedText(container: PsiElement, spaceInArray: Boolean, spaceInObject: Boolean): String {
        return when (container) {
            is MamlArray -> buildInlinedArray(container, spaceInArray, spaceInObject)
            is MamlObject -> buildInlinedObject(container, spaceInArray, spaceInObject)
            else -> ""
        }
    }

    private fun buildInlinedArray(array: MamlArray, spaceInArray: Boolean, spaceInObject: Boolean): String {
        val items = array.items ?: return ""
        val values = items.valueList
        val joined = values.joinToString(", ") { value ->
            buildInlinedValue(value, spaceInArray, spaceInObject)
        }
        val space = if (spaceInArray && joined.isNotEmpty()) " " else ""
        return "$space$joined$space"
    }

    private fun buildInlinedObject(obj: MamlObject, spaceInArray: Boolean, spaceInObject: Boolean): String {
        val members = obj.members ?: return ""
        val keyValues = members.keyValueList
        val joined = keyValues.joinToString(", ") { kv ->
            val key = kv.key.text
            val value = kv.value?.let { buildInlinedValue(it, spaceInArray, spaceInObject) } ?: ""
            "$key: $value"
        }
        val space = if (spaceInObject && joined.isNotEmpty()) " " else ""
        return "$space$joined$space"
    }

    private fun buildInlinedValue(value: MamlValue, spaceInArray: Boolean, spaceInObject: Boolean): String {
        return when (val child = value.firstChild) {
            is MamlArray -> "[" + buildInlinedArray(child, spaceInArray, spaceInObject) + "]"
            is MamlObject -> "{" + buildInlinedObject(child, spaceInArray, spaceInObject) + "}"
            else -> value.text.trim()
        }
    }

    override fun startInWriteAction(): Boolean = true
}
