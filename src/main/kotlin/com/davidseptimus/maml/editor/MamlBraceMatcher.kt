package com.davidseptimus.maml.editor

import com.davidseptimus.maml.lang.MamlLanguage
import com.davidseptimus.maml.lang.psi.MamlTypes
import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType

class MamlBraceMatcher : PairedBraceMatcher {
    override fun getPairs() = PAIRS

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean = true

    override fun getCodeConstructStart(file: PsiFile, openingBraceOffset: Int): Int
            = openingBraceOffset

}

private val PAIRS: Array<BracePair> = arrayOf(
    // Grammar Kit hack - ignore braces in recovery (adapted from TOML plugin -- is this needed?)
    BracePair(IElementType("FAKE_L_BRACE", MamlLanguage), IElementType("FAKE_L_BRACE", MamlLanguage), true),
    BracePair(MamlTypes.LBRACKET, MamlTypes.RBRACKET, false),
    BracePair(MamlTypes.LBRACE, MamlTypes.RBRACE, true)
)