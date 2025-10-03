package com.davidseptimus.maml.formatter

import com.davidseptimus.maml.lang.MamlLanguage
import com.davidseptimus.maml.lang.psi.*
import com.davidseptimus.maml.lang.psi.MamlTypes
import com.intellij.application.options.CodeStyle
import com.intellij.lang.ASTNode
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.impl.source.codeStyle.PreFormatProcessor
import com.intellij.util.DocumentUtil

class MamlTrailingCommaRemover : PreFormatProcessor {
    override fun process(element: ASTNode, range: TextRange): TextRange {
        val rootPsi = element.psi
        if (rootPsi.language != MamlLanguage) {
            return range
        }

        val settings = CodeStyle.getCustomSettings(rootPsi.containingFile, MamlCodeStyleSettings::class.java)
        if (settings.KEEP_TRAILING_COMMA) {
            return range
        }

        val psiDocumentManager = PsiDocumentManager.getInstance(rootPsi.project)
        val document = psiDocumentManager.getDocument(rootPsi.containingFile) ?: return range

        DocumentUtil.executeInBulk(document) {
            psiDocumentManager.doPostponedOperationsAndUnblockDocument(document)
            val visitor = Visitor(document)
            rootPsi.accept(visitor)
            psiDocumentManager.commitDocument(document)
        }

        return range
    }

    private class Visitor(private val document: Document) : MamlRecursiveElementVisitor() {
        private var offsetDelta = 0

        override fun visitArray(o: MamlArray) {
            super.visitArray(o)
            val lastChild = o.lastChild
            if (lastChild?.node?.elementType != MamlTypes.RBRACKET) {
                return
            }
            val items = o.items
            val lastElement = items?.valueList?.lastOrNull() ?: o.firstChild
            deleteTrailingCommas(lastElement)
        }

        override fun visitObject(o: MamlObject) {
            super.visitObject(o)
            val lastChild = o.lastChild
            if (lastChild?.node?.elementType != MamlTypes.RBRACE) {
                return
            }
            val members = o.members
            val lastElement = members?.keyValueList?.lastOrNull() ?: o.firstChild
            deleteTrailingCommas(lastElement)
        }

        private fun deleteTrailingCommas(lastElementOrOpeningBrace: PsiElement?) {
            var element = lastElementOrOpeningBrace?.nextSibling

            while (element != null) {
                if (element.node.elementType == MamlTypes.COMMA ||
                    (element is PsiErrorElement && element.text == ",")) {
                    deleteNode(element.node)
                } else if (element !is PsiComment && element !is PsiWhiteSpace) {
                    break
                }
                element = element.nextSibling
            }
        }

        private fun deleteNode(node: ASTNode) {
            val length = node.textLength
            document.deleteString(node.startOffset + offsetDelta, node.startOffset + length + offsetDelta)
            offsetDelta -= length
        }
    }
}