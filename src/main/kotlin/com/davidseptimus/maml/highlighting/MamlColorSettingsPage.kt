package com.davidseptimus.maml.highlighting

import com.davidseptimus.maml.MamlIcons
import com.davidseptimus.maml.highlighting.MamlTokenAttributes
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import javax.swing.Icon

class MamlColorSettingsPage : ColorSettingsPage {
    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = DESCRIPTORS

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName(): String = "MAML"

    override fun getIcon(): Icon? = MamlIcons.FILE

    override fun getHighlighter(): SyntaxHighlighter = MamlSyntaxHighlighter()

    override fun getDemoText(): String = """
        # MAML Configuration File
        # This demonstrates all syntax highlighting features

        {
          # String keys and values
          "name": "MAML Demo",
          title: "Unquoted Key Example",

          # Numbers
          version: 1.0,
          count: 42,
          negative: -100,
          scientific: 1.5e-10,

          # Booleans and null
          enabled: true,
          disabled: false,
          placeholder: null,

          # Nested object
          "config": {
            debug-mode: true,
            max_retries: 3
          },

          # Arrays
          tags: ["development", "testing", "production"],
          ports: [8080, 8443, 9000],

          # Multiline string
          description: ""${'"'}
            This is a multiline string
            that can span multiple lines.
            It preserves formatting and whitespace.
          ""${'"'},

          # Mixed array
          data: [
            { id: 1, active: true }
            { id: 2, active: false }
            null
          ]
        }
        \bad
    """.trimIndent()

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey>? = null

}

private val DESCRIPTORS = arrayOf(
    AttributesDescriptor("Braces", MamlTokenAttributes.BRACES),
    AttributesDescriptor("Brackets", MamlTokenAttributes.BRACKETS),
    AttributesDescriptor("Comma", MamlTokenAttributes.COMMA),
    AttributesDescriptor("Colon", MamlTokenAttributes.COLON),
    AttributesDescriptor("String", MamlTokenAttributes.STRING),
    AttributesDescriptor("Multiline string", MamlTokenAttributes.MULTILINE_STRING),
    AttributesDescriptor("Number", MamlTokenAttributes.NUMBER),
    AttributesDescriptor("Keyword", MamlTokenAttributes.KEYWORD),
    AttributesDescriptor("Identifier", MamlTokenAttributes.IDENTIFIER),
    AttributesDescriptor("Comment", MamlTokenAttributes.COMMENT),
    AttributesDescriptor("Bad character", MamlTokenAttributes.BAD_CHARACTER)
)