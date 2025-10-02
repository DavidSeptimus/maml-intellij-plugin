package com.davidseptimus.maml.highlighting

import com.davidseptimus.maml.lang.MamlLexerAdapter
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType

class MamlSyntaxHighlighter: SyntaxHighlighterBase() {
    override fun getHighlightingLexer() = MamlLexerAdapter()

    override fun getTokenHighlights(tokenType: IElementType?) =
        MamlSyntaxHighlighterColors.getTokenHighlights(tokenType)
}