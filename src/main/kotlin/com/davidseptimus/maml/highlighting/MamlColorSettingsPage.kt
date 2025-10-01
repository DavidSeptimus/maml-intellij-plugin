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

          # Strings with escape sequences
          message: "Hello\nWorld\t!",
          path: "C:\\Program Files\\App",
          quote: "He said \"Hello\"",
          unicode: "Star: \u2605",

          # Invalid escape sequences (highlighted as errors)
          invalid: "Bad\xescape",

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
    """.trimIndent()

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey>? = null

}

private val DESCRIPTORS = arrayOf(
    // Punctuation
    AttributesDescriptor("Punctuation", MamlTokenAttributes.PUNCTUATION),
    AttributesDescriptor("Punctuation//Braces", MamlTokenAttributes.BRACES),
    AttributesDescriptor("Punctuation//Brackets", MamlTokenAttributes.BRACKETS),
    AttributesDescriptor("Punctuation//Comma", MamlTokenAttributes.COMMA),
    AttributesDescriptor("Punctuation//Colon", MamlTokenAttributes.COLON),

    // Values
    AttributesDescriptor("Values//String", MamlTokenAttributes.STRING),
    AttributesDescriptor("Values//Multiline string", MamlTokenAttributes.MULTILINE_STRING),
    AttributesDescriptor("Values//Number", MamlTokenAttributes.NUMBER),
    AttributesDescriptor("Values//Keyword", MamlTokenAttributes.KEYWORD),

    // Identifiers
    AttributesDescriptor("Identifiers//Identifier", MamlTokenAttributes.IDENTIFIER),
    AttributesDescriptor("Identifiers//Key", MamlTokenAttributes.KEY),

    // Comments
    AttributesDescriptor("Comments//Comment", MamlTokenAttributes.COMMENT),

    // Escape sequences
    AttributesDescriptor("Escape sequences//Valid escape sequence", MamlTokenAttributes.VALID_ESCAPE),
    AttributesDescriptor("Escape sequences//Invalid escape sequence", MamlTokenAttributes.INVALID_ESCAPE),

    // Errors
    AttributesDescriptor("Errors//Bad character", MamlTokenAttributes.BAD_CHARACTER)
)