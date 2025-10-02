package com.davidseptimus.maml.lang.psi

import com.davidseptimus.maml.lang.INSTANCE
import com.davidseptimus.maml.lang.MamlLanguage
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

class MamlFile(viewProvider: FileViewProvider) :
    PsiFileBase(viewProvider, MamlLanguage) {

    override fun getFileType(): FileType {
        return INSTANCE
    }

    override fun toString(): String {
        return "Maml file"
    }

}
