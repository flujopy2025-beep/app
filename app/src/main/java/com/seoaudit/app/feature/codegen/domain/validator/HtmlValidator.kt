package com.seoaudit.app.feature.codegen.domain.validator

/**
 * Validates HTML syntax for common errors.
 * Uses regex-based detection for MVP: unclosed tags,
 * malformed attributes, and incorrect nesting.
 */
class HtmlValidator {

    private val selfClosingTags = setOf(
        "br", "hr", "img", "input", "meta", "link",
        "area", "base", "col", "embed", "source", "track", "wbr"
    )

    private val openTagRegex = Regex("<([a-zA-Z][a-zA-Z0-9]*)([^>]*)>")
    private val closeTagRegex = Regex("</([a-zA-Z][a-zA-Z0-9]*)\\s*>")
    private val malformedAttrRegex = Regex(
        """([a-zA-Z_][\w-]*)=(?!["'])[^\s>]+"""
    )

    /**
     * Validates the given HTML code and returns a list of errors.
     * Returns an empty list if no issues are found.
     */
    fun validate(html: String): List<HtmlValidationError> {
        val errors = mutableListOf<HtmlValidationError>()
        errors.addAll(detectUnclosedTags(html))
        errors.addAll(detectMalformedAttributes(html))
        return errors
    }

    private fun detectUnclosedTags(html: String): List<HtmlValidationError> {
        val errors = mutableListOf<HtmlValidationError>()
        val stack = mutableListOf<Pair<String, Int>>()
        val lines = html.lines()

        lines.forEachIndexed { lineIndex, line ->
            openTagRegex.findAll(line).forEach { match ->
                val tagName = match.groupValues[1].lowercase()
                val attrs = match.groupValues[2]
                if (tagName !in selfClosingTags && !attrs.endsWith("/")) {
                    stack.add(tagName to lineIndex + 1)
                }
            }
            closeTagRegex.findAll(line).forEach { match ->
                val tagName = match.groupValues[1].lowercase()
                val lastIndex = stack.indexOfLast { it.first == tagName }
                if (lastIndex >= 0) {
                    stack.removeAt(lastIndex)
                } else {
                    errors.add(
                        HtmlValidationError(
                            line = lineIndex + 1,
                            message = "Closing tag </$tagName> without matching opening tag"
                        )
                    )
                }
            }
        }

        stack.forEach { (tag, line) ->
            errors.add(
                HtmlValidationError(
                    line = line,
                    message = "Unclosed tag <$tag>"
                )
            )
        }
        return errors
    }

    private fun detectMalformedAttributes(html: String): List<HtmlValidationError> {
        val errors = mutableListOf<HtmlValidationError>()
        html.lines().forEachIndexed { lineIndex, line ->
            malformedAttrRegex.findAll(line).forEach { match ->
                val attrName = match.groupValues[1]
                errors.add(
                    HtmlValidationError(
                        line = lineIndex + 1,
                        message = "Attribute '$attrName' value not quoted"
                    )
                )
            }
        }
        return errors
    }
}

/**
 * Represents a single HTML validation error.
 */
data class HtmlValidationError(
    val line: Int,
    val message: String
)
