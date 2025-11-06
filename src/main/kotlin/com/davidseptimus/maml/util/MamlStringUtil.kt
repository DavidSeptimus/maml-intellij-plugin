package com.davidseptimus.maml.util

/**
 * Utility functions for converting between different string formats in MAML.
 */
object MamlStringUtil {

    /**
     * Converts a single-line quoted string to multiline string format.
     *
     * @param quotedString The string with quotes (e.g., "hello\nworld")
     * @return The multiline string content without triple quotes
     */
    fun quotedToMultilineContent(quotedString: String): String {
        // Remove opening and closing quotes
        val content = quotedString.substring(1, quotedString.length - 1)

        // Unescape escape sequences for multiline
        val result = StringBuilder()
        var i = 0
        while (i < content.length) {
            if (content[i] == '\\' && i + 1 < content.length) {
                when (content[i + 1]) {
                    'n' -> result.append('\n')
                    't' -> result.append('\t')
                    'r' -> result.append('\r')
                    '"' -> result.append('"')
                    '\\' -> result.append('\\')
                    else -> {
                        // Keep unknown escapes as-is
                        result.append(content[i])
                        result.append(content[i + 1])
                    }
                }
                i += 2
            } else {
                result.append(content[i])
                i++
            }
        }
        return result.toString()
    }

    /**
     * Converts a multiline string to single-line quoted string format.
     *
     * @param multilineString The multiline string with triple quotes (e.g., """hello
     * world""")
     * @return The quoted string content without quotes
     */
    fun multilineToQuotedContent(multilineString: String): String {
        // Remove opening and closing triple quotes
        var content = multilineString.substring(3, multilineString.length - 3)

        // strip leading newline if it immediately follows the opening delimiter
        if (content.startsWith('\n')) {
            content = content.substring(1)
        }

        // Escape special characters for single-line string
        val result = StringBuilder()
        for (char in content) {
            when (char) {
                '\\' -> result.append("\\\\")
                '"' -> result.append("\\\"")
                '\n' -> result.append("\\n")
                '\r' -> result.append("\\r")
                '\t' -> result.append("\\t")
                '\b' -> result.append("\\b")
                else -> result.append(char)
            }
        }
        return result.toString()
    }

    /**
     * Wraps content in multiline string triple quotes.
     *
     * @param content The unescaped content
     * @return The full multiline string with triple quotes
     */
    fun wrapInMultilineQuotes(content: String): String {
        return when {
            content.isEmpty() -> "\"\"\"\n\"\"\""  // Empty string requires one newline
            content.contains('\n') -> "\"\"\"\n$content\"\"\""
            else -> "\"\"\"$content\"\"\""
        }
    }

    /**
     * Wraps content in single-line string quotes.
     *
     * @param content The escaped content
     * @return The full quoted string
     */
    fun wrapInQuotes(content: String): String {
        return "\"$content\""
    }
}