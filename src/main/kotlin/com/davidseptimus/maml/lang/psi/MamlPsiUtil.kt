// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.davidseptimus.maml.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.elementType

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

    fun container(element: PsiElement?): PsiElement? {
        return findAncestor(element) { it is MamlArray || it is MamlObject }
    }

    fun hasAncestor(element: PsiElement?, predicate: (PsiElement) -> Boolean): Boolean {
        return findAncestor(element, predicate) != null
    }

    fun findAncestor(element: PsiElement?, predicate: (PsiElement) -> Boolean): PsiElement? {
        var current = element?.parent
        while (current != null) {
            if (predicate(current)) {
                return current
            }
            current = current.parent
        }
        return null
    }

    fun isPrimitiveValue(element: PsiElement): Boolean {
        return PRIMITIVE_VALUES.contains(element.node.elementType) || element.elementType == MamlTypes.STRING && element.parent is MamlValue
    }

    fun isEmptyContainer(element: PsiElement): Boolean {
        return when (element) {
            is MamlArray -> element.items?.valueList.isNullOrEmpty()
            is MamlObject -> element.members?.keyValueList.isNullOrEmpty()
            else -> {
                when (element.elementType) {
                    MamlTypes.RBRACKET -> element.prevSibling?.elementType == MamlTypes.LBRACKET
                    MamlTypes.RBRACE -> element.prevSibling?.elementType == MamlTypes.LBRACE
                    else -> false
                }
            }
        }
    }

    private val PRIMITIVE_VALUES = TokenSet.create(
        MamlTypes.MULTILINE_STRING,
        MamlTypes.NUMBER,
        MamlTypes.TRUE,
        MamlTypes.FALSE,
        MamlTypes.NULL
    )
}