package com.davidseptimus.maml.json

import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.psi.PsiElement
import com.jetbrains.jsonSchema.ide.JsonSchemaService
import com.jetbrains.jsonSchema.impl.JsonSchemaDocumentationProvider
import org.jetbrains.annotations.Nls

class MamlJsonSchemaDocumentationProvider : DocumentationProvider {
    @Nls
    override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? =
        findSchemaAndGenerateDoc(element, originalElement, true)

    @Nls
    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? =
        findSchemaAndGenerateDoc(element, originalElement, false)

    @Nls
    private fun findSchemaAndGenerateDoc(
        element: PsiElement?,
        originalElement: PsiElement?,
        preferShort: Boolean
    ): String? {
        val targetElement = originalElement ?: element ?: return null
        val file = targetElement.containingFile ?: return null
        val virtualFile = file.virtualFile ?: return null

        val service = JsonSchemaService.Impl.get(targetElement.project)
        val schema = service.getSchemaObject(virtualFile) ?: return null

        return JsonSchemaDocumentationProvider.generateDoc(targetElement, schema, preferShort, null)
    }
}
