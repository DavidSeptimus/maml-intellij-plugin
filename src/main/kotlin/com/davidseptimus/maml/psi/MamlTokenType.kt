package com.davidseptimus.maml.psi

import com.davidseptimus.maml.MamlLanguage
import com.intellij.psi.tree.IElementType
import org.jetbrains.annotations.NonNls

class MamlTokenType(@NonNls debugName: String) :
    IElementType(debugName, MamlLanguage) {

    override fun toString(): String {
        return "MamlTokenType.${super.toString()}"
    }
}
