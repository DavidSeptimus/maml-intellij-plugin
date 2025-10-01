package com.davidseptimus.maml

import com.intellij.lexer.FlexAdapter

import java.io.Reader

class MamlLexerAdapter : FlexAdapter(MamlLexer(null as Reader?))
