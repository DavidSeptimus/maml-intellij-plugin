package com.davidseptimus.maml.json

import com.davidseptimus.maml.psi.MamlFile
import com.intellij.psi.PsiElement
import com.jetbrains.jsonSchema.extension.JsonLikePsiWalker
import com.jetbrains.jsonSchema.extension.JsonLikePsiWalkerFactory
import com.jetbrains.jsonSchema.impl.JsonSchemaObject

class MamlJsonLikePsiWalkerFactory : JsonLikePsiWalkerFactory {
    override fun handles(element: PsiElement): Boolean {
        return element.containingFile is MamlFile
    }

    override fun create(schemaObject: JsonSchemaObject?): JsonLikePsiWalker {
        return MamlJsonPsiWalker
    }
}