package com.davidseptimus.maml.refactoring

import com.intellij.lang.refactoring.NamesValidator
import com.intellij.openapi.project.Project

/**
 * Validates identifiers for MAML refactoring operations.
 * This is used by the rename refactoring to validate new names.
 */
class MamlNamesValidator : NamesValidator {

    /**
     * Checks if the given string is a valid keyword in MAML.
     * MAML has a few reserved literals that cannot be used as unquoted keys.
     */
    override fun isKeyword(name: String, project: Project?): Boolean {
        return name in KEYWORDS
    }

    /**
     * Checks if the given string is a valid identifier in MAML.
     * In MAML, identifiers can be:
     * - Unquoted: alphanumeric, dash, underscore (but not starting with a digit)
     * - Quoted: any string
     */
    override fun isIdentifier(name: String, project: Project?): Boolean {
        // Empty names are not valid
        if (name.isEmpty()) return false

        // If it's a keyword, it can still be used as a quoted key
        if (isKeyword(name, project)) return true

        // Check if it's a valid unquoted identifier
        // Must start with letter or underscore, followed by alphanumeric, dash, or underscore
        return IDENTIFIER_PATTERN.matches(name)
    }

}

// MAML keywords (literals that have special meaning)
private val KEYWORDS = setOf("true", "false", "null")

// Pattern for valid unquoted identifiers
private val IDENTIFIER_PATTERN = Regex("[a-zA-Z_][a-zA-Z0-9_-]*")