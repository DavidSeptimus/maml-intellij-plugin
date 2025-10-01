package com.davidseptimus.maml.json

import com.davidseptimus.maml.MamlFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.jsonSchema.extension.JsonSchemaEnabler

class MamlJsonSchemaEnabler : JsonSchemaEnabler {
    override fun isEnabledForFile(file: VirtualFile, project: Project?): Boolean {
        return file.fileType is MamlFileType
    }

    override fun canBeSchemaFile(file: VirtualFile): Boolean {
        // MAML files themselves cannot be schema files (schemas are JSON)
        return false
    }
}