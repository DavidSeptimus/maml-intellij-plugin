package com.davidseptimus.maml

import com.davidseptimus.maml.psi.MamlTypes
import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler

class MamlQuoteHandler : SimpleTokenSetQuoteHandler(MamlTypes.STRING, MamlTypes.MULTILINE_STRING)