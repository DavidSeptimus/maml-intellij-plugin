package com.davidseptimus.maml.psi

import com.davidseptimus.maml.MamlLanguage
import com.intellij.psi.tree.IElementType
import org.jetbrains.annotations.NonNls

class MamlElementType(@NonNls debugName: String) : IElementType(debugName, MamlLanguage)