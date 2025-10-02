package com.davidseptimus.maml.structure

import com.davidseptimus.maml.lang.psi.MamlFile
import com.davidseptimus.maml.lang.psi.*
import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.NavigatablePsiElement
import javax.swing.Icon

/**
 * Structure view element for MAML files.
 * Displays objects and key-value pairs in a hierarchical tree.
 */
class MamlStructureViewElement(private val element: NavigatablePsiElement) : StructureViewTreeElement, SortableTreeElement {

    override fun getValue(): Any = element

    override fun navigate(requestFocus: Boolean) {
        element.navigate(requestFocus)
    }

    override fun canNavigate(): Boolean = element.canNavigate()

    override fun canNavigateToSource(): Boolean = element.canNavigateToSource()

    override fun getAlphaSortKey(): String {
        return when (element) {
            is MamlKeyValue -> element.key.text.removeSurrounding("\"")
            is MamlFile -> element.name
            else -> element.text.take(100)
        }
    }

    override fun getPresentation(): ItemPresentation {
        return when (element) {
            is MamlKeyValue -> {
                object : ItemPresentation {
                    override fun getPresentableText(): String {
                        return element.key.text.removeSurrounding("\"")
                    }

                    override fun getLocationString(): String? {
                        val value = element.value
                        return when (val content = value?.firstChild) {
                            is MamlObject, is MamlArray -> null
                            else -> {
                                var text = value?.text?.take(30)
                                if (text != null && text.length == 30) {
                                    text += "..."
                                }
                                text
                            }
                        }
                    }

                    override fun getIcon(unused: Boolean): Icon? {
                        val value = element.value
                        return when (value?.firstChild) {
                            is MamlObject -> AllIcons.Json.Object
                            is MamlArray -> AllIcons.Json.Array
                            else -> AllIcons.Nodes.Property
                        }
                    }
                }
            }
            is MamlObject -> {
                object : ItemPresentation {
                    override fun getPresentableText(): String = "object"
                    override fun getIcon(unused: Boolean): Icon = AllIcons.Json.Object
                }
            }
            is MamlArray -> {
                object : ItemPresentation {
                    override fun getPresentableText(): String = "array"
                    override fun getIcon(unused: Boolean): Icon = AllIcons.Json.Array
                }
            }
            else -> element.presentation ?: PresentationData()
        }
    }

    override fun getChildren(): Array<TreeElement> {
        val children = mutableListOf<TreeElement>()

        when (element) {
            is MamlFile -> {
                // Show top-level value - collapse into file level to avoid extra nesting
                val topLevelValue = com.intellij.psi.util.PsiTreeUtil.findChildOfType(element, MamlValue::class.java)
                topLevelValue?.let { value ->
                    when (val content = value.firstChild) {
                        is MamlObject -> {
                            // For top-level objects, show their members directly (collapsed)
                            content.members?.keyValueList?.forEach { kv ->
                                children.add(MamlStructureViewElement(kv as NavigatablePsiElement))
                            }
                        }
                        is MamlArray -> {
                            // For top-level arrays, show array items directly (collapsed)
                            content.items?.valueList?.forEachIndexed { index, item ->
                                val obj = item.`object`
                                if (obj != null) {
                                    children.add(MamlArrayItemStructureViewElement(obj, index))
                                }
                            }
                        }
                    }
                }
            }
            is MamlKeyValue -> {
                // Show the value if it's an object or array
                element.value?.let { value ->
                    when (val content = value.firstChild) {
                        is MamlObject -> {
                            content.members?.keyValueList?.forEach { kv ->
                                children.add(MamlStructureViewElement(kv as NavigatablePsiElement))
                            }
                        }
                        is MamlArray -> {
                            // Show array items if they are objects
                            content.items?.valueList?.forEachIndexed { index, item ->
                                val obj = item.`object`
                                if (obj != null) {
                                    children.add(MamlArrayItemStructureViewElement(obj, index))
                                }
                            }
                        }
                    }
                }
            }
            is MamlObject -> {
                // Show all key-value pairs
                element.members?.keyValueList?.forEach { kv ->
                    children.add(MamlStructureViewElement(kv as NavigatablePsiElement))
                }
            }
            is MamlArray -> {
                // Show array items if they are objects
                element.items?.valueList?.forEachIndexed { index, item ->
                    val obj = item.`object`
                    if (obj != null) {
                        children.add(MamlArrayItemStructureViewElement(obj, index))
                    }
                }
            }
        }

        return children.toTypedArray()
    }
}

/**
 * Structure view element for array items.
 * Shows the index and contents of array items that are objects.
 */
class MamlArrayItemStructureViewElement(
    private val element: MamlObject,
    private val index: Int
) : StructureViewTreeElement, SortableTreeElement {

    override fun getValue(): Any = element

    override fun navigate(requestFocus: Boolean) {
        (element as? NavigatablePsiElement)?.navigate(requestFocus)
    }

    override fun canNavigate(): Boolean = (element as? NavigatablePsiElement)?.canNavigate() ?: false

    override fun canNavigateToSource(): Boolean = (element as? NavigatablePsiElement)?.canNavigateToSource() ?: false

    override fun getAlphaSortKey(): String = "$index"

    override fun getPresentation(): ItemPresentation {
        return object : ItemPresentation {
            override fun getPresentableText(): String {
                return "object"
            }

            override fun getIcon(unused: Boolean): Icon {
                return AllIcons.Json.Object
            }
        }
    }

    override fun getChildren(): Array<TreeElement> {
        val children = mutableListOf<TreeElement>()

        element.members?.keyValueList?.forEach { kv ->
            children.add(MamlStructureViewElement(kv as NavigatablePsiElement))
        }

        return children.toTypedArray()
    }
}