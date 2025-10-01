package com.davidseptimus.maml.json

import com.davidseptimus.maml.psi.*
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.elementType
import com.jetbrains.jsonSchema.extension.adapters.JsonArrayValueAdapter
import com.jetbrains.jsonSchema.extension.adapters.JsonObjectValueAdapter
import com.jetbrains.jsonSchema.extension.adapters.JsonPropertyAdapter
import com.jetbrains.jsonSchema.extension.adapters.JsonValueAdapter

sealed class MamlJsonValueAdapter<T : PsiElement>(protected val element: T) : JsonValueAdapter {
    final override fun getDelegate(): PsiElement = element

    override fun isObject(): Boolean = false
    override fun isArray(): Boolean = false
    override fun isNull(): Boolean = false
    override fun isStringLiteral(): Boolean = false
    override fun isNumberLiteral(): Boolean = false
    override fun isBooleanLiteral(): Boolean = false

    override fun getAsObject(): JsonObjectValueAdapter? = null
    override fun getAsArray(): JsonArrayValueAdapter? = null

    companion object {
        fun createAdapterByType(value: PsiElement): MamlJsonValueAdapter<*>? = when {
            value is MamlObject -> MamlJsonObjectAdapter(value)
            value is MamlArray -> MamlJsonArrayAdapter(value)
            value is MamlValue -> {
                when {
                    value.`object` != null -> MamlJsonObjectAdapter(value.`object`!!)
                    value.array != null -> MamlJsonArrayAdapter(value.array!!)
                    else -> MamlJsonGenericValueAdapter(value)
                }
            }

            value is MamlKey -> MamlJsonGenericValueAdapter(value)
            value is LeafPsiElement -> MamlJsonGenericValueAdapter(value)
            else -> null
        }
    }
}

class MamlJsonGenericValueAdapter(value: PsiElement) : MamlJsonValueAdapter<PsiElement>(value) {
    override fun isStringLiteral(): Boolean {
        return when (element) {
            is MamlKey -> true
            else -> {
                val type =
                    if (element is MamlValue) element.firstChild.elementType
                    else element.elementType
                type == MamlTypes.STRING ||
                        type == MamlTypes.MULTILINE_STRING ||
                        type == MamlTypes.IDENTIFIER
            }
        }
    }

    override fun isNumberLiteral(): Boolean {
        return element.firstChild.elementType == MamlTypes.NUMBER
    }

    override fun isBooleanLiteral(): Boolean {
        val type = element.firstChild.elementType
        return type == MamlTypes.TRUE || type == MamlTypes.FALSE
    }

    override fun isNull(): Boolean {
        return element.elementType == MamlTypes.NULL || element.firstChild.elementType == MamlTypes.NULL
    }
}

class MamlJsonArrayAdapter(array: MamlArray) : MamlJsonValueAdapter<MamlArray>(array), JsonArrayValueAdapter {
    private val childAdapters by lazy {
        val items = element.items?.valueList ?: emptyList()
        items.mapNotNull { createAdapterByType(it) }
    }

    override fun isArray(): Boolean = true
    override fun isNull(): Boolean = false

    override fun getAsArray(): JsonArrayValueAdapter = this
    override fun getElements(): List<JsonValueAdapter> = childAdapters
}

class MamlJsonObjectAdapter(obj: MamlObject) : MamlJsonValueAdapter<MamlObject>(obj), JsonObjectValueAdapter {
    private val childAdapters by lazy {
        val members = element.members?.keyValueList ?: emptyList()
        members.map { MamlJsonPropertyAdapter(it) }
    }

    override fun isObject(): Boolean = true
    override fun isNull(): Boolean = false

    override fun getAsObject(): JsonObjectValueAdapter = this
    override fun getPropertyList(): List<JsonPropertyAdapter> = childAdapters
}