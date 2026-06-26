package com.seoaudit.app.feature.codegen.domain.engine

import com.seoaudit.app.core.domain.model.DiffResult

/**
 * Engine for generating, applying, and validating text diffs.
 *
 * Uses LCS (Longest Common Subsequence) algorithm to compute
 * minimal diffs between two text documents.
 */
interface DiffEngine {

    /**
     * Generates a diff between the original and modified text.
     *
     * @param original The original text content
     * @param modified The modified text content
     * @return DiffResult containing hunks, additions, and deletions
     */
    fun generateDiff(original: String, modified: String): DiffResult

    /**
     * Applies a diff to the original text to reproduce the modified text.
     *
     * @param original The original text content
     * @param diff The diff to apply
     * @return The resulting text after applying the diff
     */
    fun applyDiff(original: String, diff: DiffResult): String

    /**
     * Validates that applying a diff to the original produces the expected result.
     *
     * @param original The original text content
     * @param diff The diff to validate
     * @param expected The expected result after applying the diff
     * @return true if applyDiff(original, diff) == expected
     */
    fun validateDiff(original: String, diff: DiffResult, expected: String): Boolean
}
