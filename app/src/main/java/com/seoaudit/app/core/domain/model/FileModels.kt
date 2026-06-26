package com.seoaudit.app.core.domain.model

import android.net.Uri
import java.nio.charset.Charset

/**
 * Represents the content of a file read from the filesystem.
 *
 * @param uri URI of the file
 * @param content Raw byte content of the file
 * @param charset Detected charset encoding of the file
 * @param mimeType MIME type of the file
 */
data class FileContent(
    val uri: Uri,
    val content: ByteArray,
    val charset: Charset,
    val mimeType: String
) {
    /** Returns file content as a decoded string using the detected charset. */
    fun asString(): String = String(content, charset)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FileContent) return false
        return uri == other.uri &&
            content.contentEquals(other.content) &&
            charset == other.charset &&
            mimeType == other.mimeType
    }

    override fun hashCode(): Int {
        var result = uri.hashCode()
        result = 31 * result + content.contentHashCode()
        result = 31 * result + charset.hashCode()
        result = 31 * result + mimeType.hashCode()
        return result
    }
}

/**
 * Represents metadata about a file in a directory listing.
 *
 * @param name Display name of the file
 * @param uri URI for accessing the file
 * @param size File size in bytes
 * @param mimeType MIME type of the file
 */
data class FileInfo(
    val name: String,
    val uri: Uri,
    val size: Long,
    val mimeType: String
)
