package com.davidseptimus.maml.editor

import com.davidseptimus.maml.MamlBundle
import com.intellij.application.options.editor.CodeFoldingOptionsProvider
import com.intellij.codeInsight.folding.CodeFoldingManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.options.BeanConfigurable

/**
 * Provides MAML-specific folding options in the IDE's Code Folding settings page.
 */
class MamlCodeFoldingOptionsProvider :
    BeanConfigurable<MamlCodeFoldingSettings.State>(MamlCodeFoldingSettings.getInstance().state, "MAML"),
    CodeFoldingOptionsProvider {

    init {
        val settings = MamlCodeFoldingSettings.getInstance()
        checkBox(
            MamlBundle.message("folding.unicode.escapes"),
            settings.state::collapseUnicodeEscapes
        )
        checkBox(
            MamlBundle.message("folding.unicode.escapes.group"),
            settings.state::groupUnicodeEscapes
        )
    }

    override fun apply() {
        val oldGroupValue = MamlCodeFoldingSettings.getInstance().isGroupUnicodeEscapes
        super.apply()
        val newGroupValue = MamlCodeFoldingSettings.getInstance().isGroupUnicodeEscapes

        // If the group setting changed, rebuild folding
        if (oldGroupValue != newGroupValue) {
            rebuildAllMamlFolding()
        }
    }

    private fun rebuildAllMamlFolding() {
        ApplicationManager.getApplication().invokeLater {
            EditorFactory.getInstance().allEditors.forEach { editor ->
                val document = editor.document
                val file = FileDocumentManager.getInstance().getFile(document)
                val project = editor.project

                if (file?.extension == "maml" && project != null) {
                    val foldingManager = CodeFoldingManager.getInstance(project)
                    // Run in a batch operation to rebuild folding from scratch
                    editor.foldingModel.runBatchFoldingOperation {
                        // Remove all existing fold regions
                        val allRegions = editor.foldingModel.allFoldRegions
                        allRegions.forEach { region ->
                            editor.foldingModel.removeFoldRegion(region)
                        }
                    }
                    // Rebuild folding after clearing
                    foldingManager.updateFoldRegions(editor)
                }
            }
        }
    }
}