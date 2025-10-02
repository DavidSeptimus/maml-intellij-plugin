package com.davidseptimus.maml.psi

import com.davidseptimus.maml.psi.MamlNamedElement
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.PsiTreeUtil

/**
 * Mixin implementation for MAML keys that provides rename support and references.
 */
abstract class MamlKeyMixin(node: ASTNode) : ASTWrapperPsiElement(node), MamlNamedElement {

    override fun getName(): String {
        // Return the key name without quotes
        return text.removeSurrounding("\"")
    }

    override fun setName(name: String): MamlNamedElement {
        // Find the identifier or string token that contains the actual key name
        val keyNode = node.findChildByType(MamlTypes.IDENTIFIER)
            ?: node.findChildByType(MamlTypes.STRING)

        if (keyNode != null && keyNode is LeafPsiElement) {
            // Determine if the current key is quoted
            val currentText = keyNode.text
            val isQuoted = currentText.startsWith("\"") && currentText.endsWith("\"")

            // Create the new text (preserve quoting style)
            val newText = if (isQuoted) {
                "\"$name\""
            } else {
                // For identifiers, keep unquoted
                name
            }

            // Directly replace the token text
            keyNode.replaceWithText(newText)
        }

        return this
    }

    override fun getReference(): PsiReference? {
        return MamlKeyReference(this)
    }

    override fun getReferences(): Array<PsiReference> {
        val ref = reference
        return if (ref != null) arrayOf(ref) else PsiReference.EMPTY_ARRAY
    }
}

/**
 * Reference implementation for MAML keys that enables find usages and rename refactoring.
 */
class MamlKeyReference(private val key: MamlKeyMixin) : PsiReference {

    override fun getElement(): PsiElement = key

    override fun getRangeInElement(): com.intellij.openapi.util.TextRange {
        // For quoted strings, reference the text inside the quotes
        val text = key.text
        return if (text.startsWith("\"") && text.endsWith("\"")) {
            com.intellij.openapi.util.TextRange(1, text.length - 1)
        } else {
            com.intellij.openapi.util.TextRange(0, text.length)
        }
    }

    override fun resolve(): PsiElement {
        return key
    }

    override fun getCanonicalText(): String {
        return key.name
    }

    override fun handleElementRename(newElementName: String): PsiElement {
        return key.setName(newElementName)
    }

    override fun bindToElement(element: PsiElement): PsiElement {
        return key
    }

    override fun isReferenceTo(element: PsiElement): Boolean {
        // A key references another key if they have the same name and are in the same file
        if (element !is MamlNamedElement) return false
        return element.name == key.name && element.containingFile == key.containingFile
    }

    override fun isSoft(): Boolean {
        // Soft references don't cause errors if unresolved
        return true
    }
}