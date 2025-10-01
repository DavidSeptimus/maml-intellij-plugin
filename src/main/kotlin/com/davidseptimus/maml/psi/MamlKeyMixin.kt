package com.davidseptimus.maml.psi

import com.davidseptimus.maml.MamlNamedElement
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

/**
 * Mixin implementation for MAML keys that provides rename support.
 */
abstract class MamlKeyMixin(node: ASTNode) : ASTWrapperPsiElement(node), MamlNamedElement {

    override fun getName(): String {
        // Return the key name without quotes
        return text.removeSurrounding("\"")
    }

    override fun setName(name: String): MamlNamedElement {
        // Determine if the current key is quoted
        val currentText = text
        val isQuoted = currentText.startsWith("\"") && currentText.endsWith("\"")

        // Create the new text (preserve quoting style)
        val newText = if (isQuoted) {
            "\"$name\""
        } else {
            // For identifiers, keep unquoted
            name
        }

        // Create a new key element with the new text
        val newKey = MamlElementFactory.createKey(project, newText)
        return replace(newKey) as MamlNamedElement
    }
}