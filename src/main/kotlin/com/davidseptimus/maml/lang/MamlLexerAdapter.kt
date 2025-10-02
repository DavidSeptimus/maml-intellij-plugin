package com.davidseptimus.maml.lang

import com.intellij.lexer.FlexAdapter

import java.io.Reader

class MamlLexerAdapter : FlexAdapter(MamlLexer(null as Reader?))
