// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.davidseptimus.maml.lang.psi

import com.intellij.psi.PsiElement

open class MamlRecursiveElementVisitor : MamlVisitor() {
    override fun visitElement(element: PsiElement) {
        element.acceptChildren(this)
    }
}