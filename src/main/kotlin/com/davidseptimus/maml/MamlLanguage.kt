package com.davidseptimus.maml


import com.intellij.lang.Language

object MamlLanguage : Language("MAML") {
    private fun readResolve(): Any = MamlLanguage
}