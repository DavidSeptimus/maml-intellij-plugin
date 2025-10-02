package com.davidseptimus.maml.lang.psi

import com.davidseptimus.maml.lang.MamlLanguage
import com.intellij.psi.tree.IElementType
import org.jetbrains.annotations.NonNls

class MamlElementType(@NonNls debugName: String) : IElementType(debugName, MamlLanguage)