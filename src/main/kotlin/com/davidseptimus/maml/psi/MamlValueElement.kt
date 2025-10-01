package com.davidseptimus.maml.psi

import com.intellij.psi.PsiElement

interface MamlValueElement: PsiElement {
    val ref: String
}
