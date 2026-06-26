package com.seoaudit.app.feature.codegen.data

import com.seoaudit.app.core.domain.model.DiffHunk
import com.seoaudit.app.core.domain.model.DiffLine
import com.seoaudit.app.core.domain.model.DiffLineType
import com.seoaudit.app.core.domain.model.DiffResult
import com.seoaudit.app.feature.codegen.domain.engine.DiffEngine
import javax.inject.Inject

/**
 * Implementation of [DiffEngine] using the LCS (Longest Common Subsequence)
 * algorithm for computing minimal diffs between two text documents.
 *
 * Context lines (2 before/after changes) are included in hunks for readability.
 */
class DiffEngineImpl @Inject constructor() : DiffEngine {

    companion object {
        private const val CONTEXT_LINES = 2
    }

    override fun generateDiff(original: String, modified: String): DiffResult {
        val originalLines = original.lines()
        val modifiedLines = modified.lines()
        val editScript = computeEditScript(originalLines, modifiedLines)
        val hunks = buildHunks(editScript, originalLines, modifiedLines)

        val additions = hunks.sumOf { hunk ->
            hunk.lines.count { it.type == DiffLineType.ADD }
        }
        val deletions = hunks.sumOf { hunk ->
            hunk.lines.count { it.type == DiffLineType.REMOVE }
        }

        return DiffResult(
            hunks = hunks,
            additions = additions,
            deletions = deletions
        )
    }

    override fun applyDiff(original: String, diff: DiffResult): String {
        if (diff.hunks.isEmpty()) return original

        val originalLines = original.lines()
        val result = mutableListOf<String>()
        var originalIndex = 0

        for (hunk in diff.hunks) {
            // Copy lines before this hunk (unchanged)
            val hunkStart = hunk.oldStart - 1
            while (originalIndex < hunkStart) {
                result.add(originalLines[originalIndex])
                originalIndex++
            }

            // Process hunk lines
            for (line in hunk.lines) {
                when (line.type) {
                    DiffLineType.CONTEXT -> {
                        result.add(originalLines[originalIndex])
                        originalIndex++
                    }
                    DiffLineType.REMOVE -> {
                        originalIndex++
                    }
                    DiffLineType.ADD -> {
                        result.add(line.content)
                    }
                }
            }
        }

        // Copy remaining lines after last hunk
        while (originalIndex < originalLines.size) {
            result.add(originalLines[originalIndex])
            originalIndex++
        }

        return result.joinToString("\n")
    }

    override fun validateDiff(
        original: String,
        diff: DiffResult,
        expected: String
    ): Boolean {
        return applyDiff(original, diff) == expected
    }

    /**
     * Computes the LCS table using dynamic programming O(n*m) space.
     */
    private fun computeLcsTable(
        original: List<String>,
        modified: List<String>
    ): Array<IntArray> {
        val n = original.size
        val m = modified.size
        val table = Array(n + 1) { IntArray(m + 1) }

        for (i in 1..n) {
            for (j in 1..m) {
                table[i][j] = if (original[i - 1] == modified[j - 1]) {
                    table[i - 1][j - 1] + 1
                } else {
                    maxOf(table[i - 1][j], table[i][j - 1])
                }
            }
        }

        return table
    }

    /**
     * Represents an edit operation in the diff.
     */
    private enum class EditOp { EQUAL, INSERT, DELETE }

    private data class EditEntry(
        val op: EditOp,
        val oldIndex: Int,
        val newIndex: Int
    )

    /**
     * Backtracks through the LCS table to produce an edit script.
     */
    private fun computeEditScript(
        original: List<String>,
        modified: List<String>
    ): List<EditEntry> {
        val table = computeLcsTable(original, modified)
        val edits = mutableListOf<EditEntry>()
        var i = original.size
        var j = modified.size

        while (i > 0 || j > 0) {
            when {
                i > 0 && j > 0 && original[i - 1] == modified[j - 1] -> {
                    edits.add(EditEntry(EditOp.EQUAL, i - 1, j - 1))
                    i--
                    j--
                }
                j > 0 && (i == 0 || table[i][j - 1] >= table[i - 1][j]) -> {
                    edits.add(EditEntry(EditOp.INSERT, i, j - 1))
                    j--
                }
                else -> {
                    edits.add(EditEntry(EditOp.DELETE, i - 1, j))
                    i--
                }
            }
        }

        return edits.reversed()
    }

    /**
     * Builds hunks from the edit script, including context lines.
     */
    private fun buildHunks(
        editScript: List<EditEntry>,
        originalLines: List<String>,
        modifiedLines: List<String>
    ): List<DiffHunk> {
        // Find change ranges (non-EQUAL entries)
        val changeIndices = editScript.indices.filter {
            editScript[it].op != EditOp.EQUAL
        }

        if (changeIndices.isEmpty()) return emptyList()

        // Group changes that are within CONTEXT_LINES of each other
        val groups = groupChanges(changeIndices, editScript)
        return groups.map { group ->
            buildSingleHunk(group, editScript, originalLines, modifiedLines)
        }
    }

    /**
     * Groups change indices that are close enough to share context.
     */
    private fun groupChanges(
        changeIndices: List<Int>,
        editScript: List<EditEntry>
    ): List<List<Int>> {
        if (changeIndices.isEmpty()) return emptyList()

        val groups = mutableListOf<MutableList<Int>>()
        var currentGroup = mutableListOf(changeIndices[0])

        for (k in 1 until changeIndices.size) {
            val prevIdx = changeIndices[k - 1]
            val currIdx = changeIndices[k]
            // Count EQUAL entries between prev and curr
            val equalsBetween = (prevIdx + 1 until currIdx).count {
                editScript[it].op == EditOp.EQUAL
            }
            if (equalsBetween <= CONTEXT_LINES * 2) {
                currentGroup.add(currIdx)
            } else {
                groups.add(currentGroup)
                currentGroup = mutableListOf(currIdx)
            }
        }
        groups.add(currentGroup)
        return groups
    }

    /**
     * Builds a single DiffHunk from a group of change indices.
     */
    private fun buildSingleHunk(
        group: List<Int>,
        editScript: List<EditEntry>,
        originalLines: List<String>,
        modifiedLines: List<String>
    ): DiffHunk {
        val firstChange = group.first()
        val lastChange = group.last()

        // Determine context boundaries in the edit script
        val contextStart = maxOf(0, firstChange - CONTEXT_LINES)
        val contextEnd = minOf(editScript.size - 1, lastChange + CONTEXT_LINES)

        val lines = mutableListOf<DiffLine>()
        var oldStart = Int.MAX_VALUE
        var newStart = Int.MAX_VALUE
        var oldCount = 0
        var newCount = 0

        for (idx in contextStart..contextEnd) {
            val entry = editScript[idx]
            when (entry.op) {
                EditOp.EQUAL -> {
                    val lineNum = entry.oldIndex + 1
                    if (oldStart == Int.MAX_VALUE) {
                        oldStart = lineNum
                        newStart = entry.newIndex + 1
                    }
                    lines.add(DiffLine(DiffLineType.CONTEXT, originalLines[entry.oldIndex], lineNum))
                    oldCount++
                    newCount++
                }
                EditOp.DELETE -> {
                    val lineNum = entry.oldIndex + 1
                    if (oldStart == Int.MAX_VALUE) {
                        oldStart = lineNum
                        newStart = entry.newIndex + 1
                    }
                    lines.add(DiffLine(DiffLineType.REMOVE, originalLines[entry.oldIndex], lineNum))
                    oldCount++
                }
                EditOp.INSERT -> {
                    val lineNum = entry.newIndex + 1
                    if (oldStart == Int.MAX_VALUE) {
                        oldStart = entry.oldIndex + 1
                        newStart = lineNum
                    }
                    lines.add(DiffLine(DiffLineType.ADD, modifiedLines[entry.newIndex], lineNum))
                    newCount++
                }
            }
        }

        return DiffHunk(
            oldStart = oldStart,
            oldLines = oldCount,
            newStart = newStart,
            newLines = newCount,
            lines = lines
        )
    }
}
