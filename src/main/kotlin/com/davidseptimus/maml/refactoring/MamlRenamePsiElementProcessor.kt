package com.davidseptimus.maml.refactoring

import com.davidseptimus.maml.psi.MamlNamedElement
import com.davidseptimus.maml.psi.MamlKeyValue
import com.davidseptimus.maml.psi.MamlObject
import com.intellij.psi.PsiElement
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.rename.RenamePsiElementProcessor
import com.intellij.refactoring.util.CommonRefactoringUtil
import com.intellij.refactoring.rename.UnresolvableCollisionUsageInfo
import com.intellij.usageView.UsageInfo

/**
 * Custom rename processor for MAML keys that provides:
 * 1. Conflict detection for duplicate keys
 * 2. Automatic renaming of all matching keys in the current object
 */
class MamlRenamePsiElementProcessor : RenamePsiElementProcessor() {

    override fun canProcessElement(element: PsiElement): Boolean {
        return element is MamlNamedElement
    }

    override fun prepareRenaming(
        element: PsiElement,
        newName: String,
        allRenames: MutableMap<PsiElement, String>,
        scope: SearchScope
    ) {
        if (element !is MamlNamedElement) return

        val keyName = element.name ?: return

        // Find all keys with the same name within the search scope
        val scopeElements = when (scope) {
            is LocalSearchScope -> scope.scope.toList()
            else -> listOf(element.containingFile)
        }

        scopeElements.forEach { scopeElement ->
            val objectsInScope = PsiTreeUtil.findChildrenOfType(scopeElement, MamlObject::class.java)
            objectsInScope.forEach { obj ->
                val keys = findKeysInObject(obj, keyName)
                keys.forEach { key ->
                    allRenames[key] = newName
                }
            }
        }
    }

    override fun substituteElementToRename(element: PsiElement, editor: com.intellij.openapi.editor.Editor?): PsiElement? {
        return if (element is MamlNamedElement) element else null
    }

    override fun findCollisions(
        element: PsiElement,
        newName: String,
        allRenames: MutableMap<out PsiElement, String>,
        result: MutableList<UsageInfo>
    ) {
        if (element !is MamlNamedElement) return

        // Find all objects that will contain keys with the new name
        val file = element.containingFile
        val allObjects = PsiTreeUtil.findChildrenOfType(file, MamlObject::class.java)

        allObjects.forEach { obj ->
            // Get all keys being renamed in this object
            val keysBeingRenamed = obj.members?.keyValueList
                ?.mapNotNull { it.key as? MamlNamedElement }
                ?.filter { allRenames.containsKey(it) }
                ?: emptyList()

            // Get all existing keys in this object that aren't being renamed
            val existingKeys = obj.members?.keyValueList
                ?.mapNotNull { it.key as? MamlNamedElement }
                ?.filter { !allRenames.containsKey(it) }
                ?: emptyList()

            // Check if any existing key has the new name
            existingKeys.forEach { existingKey ->
                if (existingKey.name == newName && keysBeingRenamed.isNotEmpty()) {
                    // There's a conflict - the new name already exists in this object
                    val conflictingKey = keysBeingRenamed.first()
                    result.add(MamlKeyCollisionUsageInfo(
                        existingKey,
                        conflictingKey,
                        "Key '$newName' already exists in this object"
                    ))
                }
            }

            // Check for duplicate keys after rename within the same object
            if (keysBeingRenamed.size > 1) {
                // Multiple keys in the same object are being renamed to the same name
                // This creates duplicates
                keysBeingRenamed.drop(1).forEach { duplicateKey ->
                    result.add(MamlKeyCollisionUsageInfo(
                        keysBeingRenamed.first(),
                        duplicateKey,
                        "Renaming will create duplicate keys in the same object"
                    ))
                }
            }
        }
    }

    private fun findKeysInObject(obj: MamlObject, keyName: String): List<PsiElement> {
        val keys = mutableListOf<PsiElement>()
        val members = obj.members ?: return keys

        members.keyValueList.forEach { kv ->
            val key = kv.key
            if (key.name == keyName) {
                keys.add(key)
            }
        }

        return keys
    }
}

/**
 * Usage info for key collision conflicts during rename.
 */
class MamlKeyCollisionUsageInfo(
    element: PsiElement,
    referencedElement: PsiElement,
    message: String
) : UnresolvableCollisionUsageInfo(element, referencedElement) {

    private val conflictMessage = message

    override fun getDescription(): String {
        return conflictMessage
    }
}