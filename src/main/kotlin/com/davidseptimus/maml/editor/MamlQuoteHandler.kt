package com.davidseptimus.maml.editor

import com.davidseptimus.maml.lang.psi.MamlTypes
import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler

class MamlQuoteHandler : SimpleTokenSetQuoteHandler(MamlTypes.STRING, MamlTypes.MULTILINE_STRING, MamlTypes.UNTERMINATED_STRING)