package com.davidseptimus.maml.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.paths.WebReference
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*

abstract class MamlValueMixin(node: ASTNode) : ASTWrapperPsiElement(node), MamlValueElement {

    override fun getReference(): PsiReference? {
        val refValue = ref
        if (refValue.isEmpty()) return null

        return object : PsiReferenceBase<MamlValueMixin>(this, TextRange(0, textLength), false) {
            override fun resolve(): PsiElement? {
                return resolveFile(refValue)
            }

            private fun resolveFile(ref: String): PsiElement? {
                if (ref.isEmpty()) return null
                if (ref.startsWith("http://") || ref.startsWith("https://")) {
                    return WebReference(element, ref).resolve()
                }
                var fileRef = ref
                if (fileRef.startsWith("file://")) {
                    fileRef = fileRef.removePrefix("file://")
                }
                val directory = element.containingFile
                    ?.containingDirectory?.virtualFile ?: return null
                val file = resolveFile(fileRef, directory) ?: return null

                return PsiManager.getInstance(element.project).findFile(file)
            }
        }
    }

    override fun getReferences(): Array<PsiReference> {
        val reference = reference
        return if (reference != null) arrayOf(reference) else PsiReference.EMPTY_ARRAY
    }

    override fun getName(): String? {
        return ref
    }


    override val ref: String
        get() = text.trim().removeSurrounding("\"")
}