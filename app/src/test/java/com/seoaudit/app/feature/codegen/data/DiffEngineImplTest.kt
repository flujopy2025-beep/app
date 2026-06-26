package com.seoaudit.app.feature.codegen.data

import com.seoaudit.app.core.domain.model.DiffLineType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DiffEngineImplTest {

    private lateinit var diffEngine: DiffEngineImpl

    @BeforeEach
    fun setUp() {
        diffEngine = DiffEngineImpl()
    }

    @Test
    fun `generateDiff with identical strings produces empty hunks`() {
        val text = "line1\nline2\nline3"
        val result = diffEngine.generateDiff(text, text)

        assertEquals(0, result.hunks.size)
        assertEquals(0, result.additions)
        assertEquals(0, result.deletions)
    }

    @Test
    fun `generateDiff detects single line addition`() {
        val original = "line1\nline2\nline3"
        val modified = "line1\nline2\nnew line\nline3"
        val result = diffEngine.generateDiff(original, modified)

        assertEquals(1, result.additions)
        assertEquals(0, result.deletions)
        assertTrue(result.hunks.isNotEmpty())
    }

    @Test
    fun `generateDiff detects single line removal`() {
        val original = "line1\nline2\nline3"
        val modified = "line1\nline3"
        val result = diffEngine.generateDiff(original, modified)

        assertEquals(0, result.additions)
        assertEquals(1, result.deletions)
        assertTrue(result.hunks.isNotEmpty())
    }

    @Test
    fun `generateDiff detects line modification`() {
        val original = "line1\nline2\nline3"
        val modified = "line1\nmodified\nline3"
        val result = diffEngine.generateDiff(original, modified)

        assertEquals(1, result.additions)
        assertEquals(1, result.deletions)
    }

    @Test
    fun `applyDiff reproduces modified text from original`() {
        val original = "line1\nline2\nline3\nline4\nline5"
        val modified = "line1\nchanged\nline3\nnew\nline5"

        val diff = diffEngine.generateDiff(original, modified)
        val applied = diffEngine.applyDiff(original, diff)

        assertEquals(modified, applied)
    }

    @Test
    fun `applyDiff with empty hunks returns original`() {
        val original = "line1\nline2\nline3"
        val diff = diffEngine.generateDiff(original, original)
        val applied = diffEngine.applyDiff(original, diff)

        assertEquals(original, applied)
    }

    @Test
    fun `validateDiff returns true for valid round-trip`() {
        val original = "hello\nworld\nfoo"
        val modified = "hello\nuniverse\nfoo\nbar"

        val diff = diffEngine.generateDiff(original, modified)
        assertTrue(diffEngine.validateDiff(original, diff, modified))
    }

    @Test
    fun `round-trip works for complete replacement`() {
        val original = "aaa\nbbb\nccc"
        val modified = "xxx\nyyy\nzzz"

        val diff = diffEngine.generateDiff(original, modified)
        val applied = diffEngine.applyDiff(original, diff)

        assertEquals(modified, applied)
    }

    @Test
    fun `round-trip works for prepending lines`() {
        val original = "line1\nline2"
        val modified = "new0\nline1\nline2"

        val diff = diffEngine.generateDiff(original, modified)
        val applied = diffEngine.applyDiff(original, diff)

        assertEquals(modified, applied)
    }

    @Test
    fun `round-trip works for appending lines`() {
        val original = "line1\nline2"
        val modified = "line1\nline2\nline3"

        val diff = diffEngine.generateDiff(original, modified)
        val applied = diffEngine.applyDiff(original, diff)

        assertEquals(modified, applied)
    }

    @Test
    fun `round-trip works for single line text`() {
        val original = "hello"
        val modified = "world"

        val diff = diffEngine.generateDiff(original, modified)
        val applied = diffEngine.applyDiff(original, diff)

        assertEquals(modified, applied)
    }

    @Test
    fun `hunk lines include correct types`() {
        val original = "a\nb\nc"
        val modified = "a\nx\nc"

        val diff = diffEngine.generateDiff(original, modified)
        val hunk = diff.hunks.first()

        val removeLines = hunk.lines.filter { it.type == DiffLineType.REMOVE }
        val addLines = hunk.lines.filter { it.type == DiffLineType.ADD }

        assertEquals(1, removeLines.size)
        assertEquals("b", removeLines.first().content)
        assertEquals(1, addLines.size)
        assertEquals("x", addLines.first().content)
    }
}
