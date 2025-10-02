package com.davidseptimus.maml.json

import com.davidseptimus.maml.lang.psi.MamlKeyValue
import com.davidseptimus.maml.lang.psi.MamlObject
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.jetbrains.jsonSchema.extension.adapters.JsonObjectValueAdapter
import com.jetbrains.jsonSchema.extension.adapters.JsonPropertyAdapter
import com.jetbrains.jsonSchema.extension.adapters.JsonValueAdapter

class MamlJsonPropertyAdapter(private val keyValue: MamlKeyValue) : JsonPropertyAdapter {
    override fun getName(): String {
        return keyValue.key.text.removeSurrounding("\"")
    }

    override fun getNameValueAdapter(): JsonValueAdapter {
        return MamlJsonGenericValueAdapter(keyValue.key)
    }

    override fun getDelegate(): PsiElement = keyValue

    override fun getValues(): Collection<JsonValueAdapter> {
        val value = keyValue.value ?: return emptyList()
        val adapter = MamlJsonValueAdapter.createAdapterByType(value)
        return if (adapter != null) listOf(adapter) else emptyList()
    }

    override fun getParentObject(): JsonObjectValueAdapter? {
        val parent = keyValue.parentOfType<MamlObject>()
        return if (parent != null) MamlJsonObjectAdapter(parent) else null
    }
}