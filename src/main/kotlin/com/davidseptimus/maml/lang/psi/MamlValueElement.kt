package com.davidseptimus.maml.lang.psi

import com.intellij.psi.PsiElement

interface MamlValueElement: PsiElement {
    val ref: String
}
