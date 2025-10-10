package com.davidseptimus.maml.lang

import com.davidseptimus.maml.MamlIcons
import com.intellij.openapi.fileTypes.LanguageFileType

class MamlFileType: LanguageFileType(MamlLanguage) {
    override fun getName() = "MAML"
    override fun getDescription() = "MAML file"
    override fun getDefaultExtension() = "maml"
    override fun getIcon() = MamlIcons.FILE
}

val INSTANCE = MamlFileType()