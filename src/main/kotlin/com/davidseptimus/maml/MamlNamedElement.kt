package com.davidseptimus.maml

import com.intellij.psi.PsiNamedElement

/**
 * Interface for MAML elements that have a name and can be renamed.
 * This is used for keys in key-value pairs to support refactoring operations.
 */
interface MamlNamedElement : PsiNamedElement {
    /**
     * Returns the name of this element without quotes.
     */
    override fun getName(): String?

    /**
     * Sets a new name for this element.
     * This will update the underlying text while preserving quotes if present.
     */
    override fun setName(name: String): MamlNamedElement
}