package com.davidseptimus.maml.structure

import com.davidseptimus.maml.lang.psi.MamlFile
import com.davidseptimus.maml.lang.psi.MamlKeyValue
import com.davidseptimus.maml.lang.psi.MamlObject
import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.StructureViewModelBase
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.Sorter
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile

/**
 * Structure view model for MAML files.
 * Defines which elements are shown in the structure view and how they're sorted.
 */
class MamlStructureViewModel(psiFile: PsiFile, editor: Editor?) :
    StructureViewModelBase(psiFile, editor, MamlStructureViewElement(psiFile as MamlFile)),
    StructureViewModel.ElementInfoProvider {

    init {
        withSuitableClasses(MamlFile::class.java, MamlKeyValue::class.java, MamlObject::class.java)
    }

    override fun getSorters(): Array<Sorter> {
        return arrayOf(Sorter.ALPHA_SORTER)
    }

    override fun isAlwaysShowsPlus(element: StructureViewTreeElement): Boolean {
        return false
    }

    override fun isAlwaysLeaf(element: StructureViewTreeElement): Boolean {
        val value = element.value
        return value is MamlKeyValue && !hasObjectOrArrayValue(value)
    }

    private fun hasObjectOrArrayValue(keyValue: MamlKeyValue): Boolean {
        val value = keyValue.value ?: return false
        val content = value.firstChild
        return content is MamlObject || content is com.davidseptimus.maml.lang.psi.MamlArray
    }
}