package com.davidseptimus.maml.lang.psi

import com.davidseptimus.maml.lang.MamlLanguage
import com.intellij.psi.tree.IElementType
import org.jetbrains.annotations.NonNls

class MamlTokenType(@NonNls debugName: String) :
    IElementType(debugName, MamlLanguage) {

    override fun toString(): String {
        return "MamlTokenType.${super.toString()}"
    }
}
