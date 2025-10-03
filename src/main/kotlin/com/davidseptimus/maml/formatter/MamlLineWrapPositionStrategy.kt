package com.davidseptimus.maml.formatter

import com.davidseptimus.maml.lang.psi.MamlTypes
import com.intellij.openapi.editor.DefaultLineWrapPositionStrategy
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtilCore

class MamlLineWrapPositionStrategy : DefaultLineWrapPositionStrategy() {
    override fun calculateWrapPosition(
        document: Document,
        project: Project?,
        startOffset: Int,
        endOffset: Int,
        maxPreferredOffset: Int,
        allowToBeyondMaxPreferredOffset: Boolean,
        isSoftWrap: Boolean
    ): Int {
        if (isSoftWrap) {
            return super.calculateWrapPosition(
                document,
                project,
                startOffset,
                endOffset,
                maxPreferredOffset,
                allowToBeyondMaxPreferredOffset,
                true
            )
        }

        if (project == null) return -1

        val wrapPosition = getMinWrapPosition(document, project, maxPreferredOffset)
        if (wrapPosition == SKIP_WRAPPING) return -1

        val minWrapPosition = maxOf(startOffset, wrapPosition)
        return super.calculateWrapPosition(
            document,
            project,
            minWrapPosition,
            endOffset,
            maxPreferredOffset,
            allowToBeyondMaxPreferredOffset,
            isSoftWrap
        )
    }
}

private const val SKIP_WRAPPING = -2

private fun getMinWrapPosition(document: Document, project: Project, offset: Int): Int {
    val manager = PsiDocumentManager.getInstance(project)
    if (manager.isUncommited(document)) {
        manager.commitDocument(document)
    }

    val psiFile = manager.getPsiFile(document) ?: return -1
    val currElement = psiFile.findElementAt(offset)
    val elementType: IElementType? = PsiUtilCore.getElementType(currElement)

    when (elementType) {
        MamlTypes.STRING,
        MamlTypes.MULTILINE_STRING,
        MamlTypes.IDENTIFIER,
        MamlTypes.NUMBER,
        MamlTypes.TRUE,
        MamlTypes.FALSE,
        MamlTypes.NULL -> {
            return currElement?.textRange?.endOffset ?: -1
        }
        MamlTypes.COLON -> {
            return SKIP_WRAPPING
        }
    }

    if (currElement != null) {
        if (currElement is PsiComment ||
            PsiUtilCore.getElementType(PsiTreeUtil.skipWhitespacesForward(currElement)) == MamlTypes.COMMA) {
            return SKIP_WRAPPING
        }
    }

    return -1
}