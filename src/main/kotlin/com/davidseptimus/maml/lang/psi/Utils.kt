package com.davidseptimus.maml.lang.psi

import com.intellij.openapi.vfs.VirtualFile
import java.io.File

fun resolveFile(filepath: String, base: VirtualFile): VirtualFile? {
    val target = File(filepath)

    // First try absolute path
    if (target.isAbsolute) {
        return base.fileSystem.findFileByPath(filepath)
    }

    // Try relative to current file
    base.findFileByRelativePath(filepath)?.let { return it }

    // Fallback to project root
    val projectBase = base.fileSystem.findFileByPath(base.path.split("/").dropLast(1).joinToString("/"))
    projectBase?.findFileByRelativePath(filepath)?.let { return it }

    return null
}
