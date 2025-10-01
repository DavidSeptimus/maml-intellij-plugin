package com.davidseptimus.maml.json

import com.davidseptimus.maml.psi.*
import com.intellij.json.pointer.JsonPointerPosition
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.util.ThreeState
import com.jetbrains.jsonSchema.extension.JsonLikePsiWalker
import com.jetbrains.jsonSchema.extension.adapters.JsonPropertyAdapter
import com.jetbrains.jsonSchema.extension.adapters.JsonValueAdapter

object MamlJsonPsiWalker : JsonLikePsiWalker {
    override fun isName(element: PsiElement?): ThreeState {
        return if (element is MamlKey) ThreeState.YES else ThreeState.NO
    }

    override fun isPropertyWithValue(element: PsiElement): Boolean {
        return element is MamlKeyValue
    }

    override fun findElementToCheck(element: PsiElement): PsiElement? {
        return element.parentOfType<MamlValue>() ?: element.parentOfType<MamlKeyValue>()
    }

    override fun findPosition(element: PsiElement, forceLastTransition: Boolean): JsonPointerPosition {
        val position = JsonPointerPosition()
        var current: PsiElement = element

        while (current !is PsiFile) {
            val parent = current.parent

            when {
                // Key in a key-value pair
                current is MamlKey && parent is MamlKeyValue -> {
                    if (current == element && !forceLastTransition) {
                        // Skip the current key if we're not forcing the last transition
                        // (used in completion where the key might be incomplete)
                    } else {
                        position.addPrecedingStep(current.text.removeSurrounding("\""))
                    }
                }

                // Value in an array
                current is MamlValue && parent is MamlItems -> {
                    val grandParent = parent.parent
                    if (grandParent is MamlArray) {
                        val values = parent.valueList
                        val index = values.indexOf(current)
                        if (index >= 0) {
                            if (current != element || forceLastTransition) {
                                position.addPrecedingStep(index)
                            }
                        }
                    }
                }

                // KeyValue in Members
                current is MamlKeyValue && parent is MamlMembers -> {
                    val key = current.key.text.removeSurrounding("\"")
                    if (current != element || forceLastTransition) {
                        position.addPrecedingStep(key)
                    }
                }
            }

            current = parent
        }

        return position
    }

    override fun getPropertyNamesOfParentObject(originalPosition: PsiElement, computedPosition: PsiElement?): Set<String> {
        val obj = originalPosition.parentOfType<MamlObject>() ?: return emptySet()
        val members = obj.members ?: return emptySet()
        return members.keyValueList.mapNotNullTo(HashSet()) { kv ->
            kv.key.text.removeSurrounding("\"")
        }
    }

    override fun getParentPropertyAdapter(element: PsiElement): JsonPropertyAdapter? {
        val property = element.parentOfType<MamlKeyValue>(true) ?: return null
        return MamlJsonPropertyAdapter(property)
    }

    override fun isTopJsonElement(element: PsiElement): Boolean {
        return element is MamlFile
    }

    override fun createValueAdapter(element: PsiElement): JsonValueAdapter? {
        return MamlJsonValueAdapter.createAdapterByType(element)
    }

    override fun getRoots(file: PsiFile): List<PsiElement> {
        if (file !is MamlFile) return emptyList()
        return file.children.filterIsInstance<MamlValue>()
    }

    override fun getPropertyNameElement(property: PsiElement?): PsiElement? {
        return (property as? MamlKeyValue)?.key
    }

    override fun hasMissingCommaAfter(element: PsiElement): Boolean = false

    override fun acceptsEmptyRoot(): Boolean = true

    override fun requiresNameQuotes(): Boolean = false

    override fun allowsSingleQuotes(): Boolean = false
}