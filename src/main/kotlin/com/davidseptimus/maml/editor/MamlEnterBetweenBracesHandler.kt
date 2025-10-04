package com.davidseptimus.maml.editor

import com.davidseptimus.maml.lang.MamlLanguage
import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiFile

/**
 * Handles pressing Enter between braces or brackets in MAML files.
 *
 * When pressing Enter between:
 * - `{|}` becomes `{\n  |\n}`
 * - `[|]` becomes `[\n  |\n]`
 */
class MamlEnterBetweenBracesHandler : EnterHandlerDelegateAdapter() {

    override fun preprocessEnter(
        file: PsiFile,
        editor: Editor,
        caretOffsetRef: Ref<Int>,
        caretAdvanceRef: Ref<Int>,
        dataContext: DataContext,
        originalHandler: EditorActionHandler?
    ): EnterHandlerDelegate.Result {
        if (file.language != MamlLanguage) {
            return EnterHandlerDelegate.Result.Continue
        }

        val caretOffset = caretOffsetRef.get()
        val document = editor.document
        val text = document.charsSequence

        if (caretOffset < 1 || caretOffset >= text.length) {
            return EnterHandlerDelegate.Result.Continue
        }

        val charBefore = text[caretOffset - 1]
        val charAfter = text[caretOffset]

        // Check if we're between braces or brackets
        val isBetweenBraces = (charBefore == '{' && charAfter == '}') ||
                (charBefore == '[' && charAfter == ']')

        if (!isBetweenBraces) {
            return EnterHandlerDelegate.Result.Continue
        }

        // Calculate indentation for the opening brace's line
        val openBracePos = caretOffset - 1
        val lineNumber = document.getLineNumber(openBracePos)
        val lineStartOffset = document.getLineStartOffset(lineNumber)

        // Find the actual indentation (leading whitespace only)
        var indentEnd = lineStartOffset
        while (indentEnd < text.length && (text[indentEnd] == ' ' || text[indentEnd] == '\t')) {
            indentEnd++
        }
        val baseIndent = text.substring(lineStartOffset, indentEnd)

        // Get the indent size from code style settings
        val indentSize = CodeStyle.getSettings(file).getIndentSize(file.fileType)
        val indentChar = if (CodeStyle.getSettings(file).useTabCharacter(file.fileType)) "\t" else " "
        val indentString = indentChar.repeat(indentSize)

        // Insert two newlines: one with extra indent for cursor, one with base indent for closing brace
        val textToInsert = "\n$baseIndent$indentString\n$baseIndent"
        document.insertString(caretOffset, textToInsert)

        // Position cursor after the first newline and indent
        val newCaretOffset = caretOffset + 1 + baseIndent.length + indentString.length
        editor.caretModel.moveToOffset(newCaretOffset)

        // Stop further processing since we handled it completely
        return EnterHandlerDelegate.Result.Stop
    }
}