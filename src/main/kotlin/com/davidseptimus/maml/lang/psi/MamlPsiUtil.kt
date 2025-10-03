// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.davidseptimus.maml.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

object MamlPsiUtil {
    fun hasElementType(node: ASTNode, type: IElementType): Boolean =
        node.elementType == type

    fun hasElementType(node: ASTNode, vararg types: IElementType): Boolean =
        types.contains(node.elementType)

    fun hasElementType(node: ASTNode, types: TokenSet): Boolean =
        types.contains(node.elementType)

    fun isPropertyValue(element: PsiElement): Boolean {
        val parent = element.parent
        return parent is MamlKeyValue && parent.value == element
    }
}