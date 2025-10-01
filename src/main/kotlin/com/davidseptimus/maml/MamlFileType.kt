package com.davidseptimus.maml

import com.intellij.openapi.fileTypes.LanguageFileType

class MamlFileType: LanguageFileType(MamlLanguage) {
    override fun getName() = "MAML"
    override fun getDescription() = "MAML file"
    override fun getDefaultExtension() = "com/davidseptimus/maml"
    override fun getIcon() = MamlIcons.FILE

}

val INSTANCE = MamlFileType()