package com.davidseptimus.maml.lang


import com.intellij.lang.Language

object MamlLanguage : Language("MAML") {
    private fun readResolve(): Any = MamlLanguage

    override fun isCaseSensitive(): Boolean {
        return true
    }
}