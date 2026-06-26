package com.seoaudit.app.feature.filesystem.data

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import com.seoaudit.app.core.domain.model.FileContent
import com.seoaudit.app.core.domain.model.FileInfo
import com.seoaudit.app.core.domain.repository.FileSystemRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.charset.Charset
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SAF-based implementation of [FileSystemRepository].
 * Uses Android ContentResolver for file operations and persists
 * URI permissions for future sessions.
 *
 * Validates: Requirements 13.1, 13.2, 13.3, 13.4, 22.1, 22.2, 22.3, 22.4, 22.5
 */
@Singleton
class SafFileRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : FileSystemRepository {

    private val contentResolver: ContentResolver
        get() = context.contentResolver

    override suspend fun readFile(uri: Uri): Result<FileContent> =
        withContext(Dispatchers.IO) {
            try {
                val bytes = contentResolver.openInputStream(uri)?.use {
                    it.readBytes()
                } ?: return@withContext Result.failure(
                    IOException("Cannot open file: $uri")
                )
                val charset = detectCharset(bytes)
                val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
                Result.success(
                    FileContent(
                        uri = uri,
                        content = bytes,
                        charset = charset,
                        mimeType = mimeType
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun writeFile(uri: Uri, content: ByteArray): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                contentResolver.openOutputStream(uri, "wt")?.use {
                    it.write(content)
                } ?: return@withContext Result.failure(
                    IOException("Cannot open file for writing: $uri")
                )
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun createBackup(uri: Uri): Result<Uri> =
        withContext(Dispatchers.IO) {
            try {
                val parentUri = getParentUri(uri)
                    ?: return@withContext Result.failure(
                        IOException("Cannot determine parent directory")
                    )
                val originalName = getFileName(uri)
                val backupName = "${originalName}.bak"
                val backupUri = DocumentsContract.createDocument(
                    contentResolver,
                    parentUri,
                    contentResolver.getType(uri) ?: "application/octet-stream",
                    backupName
                ) ?: return@withContext Result.failure(
                    IOException("Cannot create backup file")
                )
                // Copy content to backup
                val bytes = contentResolver.openInputStream(uri)?.use {
                    it.readBytes()
                } ?: return@withContext Result.failure(
                    IOException("Cannot read original file")
                )
                contentResolver.openOutputStream(backupUri)?.use {
                    it.write(bytes)
                }
                Result.success(backupUri)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun listFiles(
        directoryUri: Uri,
        extensions: List<String>?
    ): Result<List<FileInfo>> = withContext(Dispatchers.IO) {
        try {
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                directoryUri,
                DocumentsContract.getTreeDocumentId(directoryUri)
            )
            val files = mutableListOf<FileInfo>()
            val projection = arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_SIZE
            )
            contentResolver.query(
                childrenUri, projection, null, null, null
            )?.use { cursor ->
                val idIdx = cursor.getColumnIndexOrThrow(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID
                )
                val nameIdx = cursor.getColumnIndexOrThrow(
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME
                )
                val mimeIdx = cursor.getColumnIndexOrThrow(
                    DocumentsContract.Document.COLUMN_MIME_TYPE
                )
                val sizeIdx = cursor.getColumnIndexOrThrow(
                    DocumentsContract.Document.COLUMN_SIZE
                )
                while (cursor.moveToNext()) {
                    val docId = cursor.getString(idIdx)
                    val name = cursor.getString(nameIdx)
                    val mime = cursor.getString(mimeIdx)
                    val size = cursor.getLong(sizeIdx)
                    // Skip directories
                    if (mime == DocumentsContract.Document.MIME_TYPE_DIR) continue
                    // Filter by extension if provided
                    if (extensions != null) {
                        val ext = name.substringAfterLast('.', "")
                        if (ext.lowercase() !in extensions.map { it.lowercase() }) continue
                    }
                    val fileUri = DocumentsContract.buildDocumentUriUsingTree(
                        directoryUri, docId
                    )
                    files.add(FileInfo(name, fileUri, size, mime))
                }
            }
            Result.success(files)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun hasPersistedPermission(uri: Uri): Boolean {
        val persistedUris = contentResolver.persistedUriPermissions
        return persistedUris.any {
            it.uri == uri && it.isReadPermission
        }
    }

    /**
     * Persists read/write URI permission for future sessions.
     */
    fun persistPermission(uri: Uri) {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        contentResolver.takePersistableUriPermission(uri, flags)
    }

    /**
     * Detects charset from BOM (Byte Order Mark) or defaults to UTF-8.
     */
    internal fun detectCharset(bytes: ByteArray): Charset {
        if (bytes.size >= 3 &&
            bytes[0] == 0xEF.toByte() &&
            bytes[1] == 0xBB.toByte() &&
            bytes[2] == 0xBF.toByte()
        ) {
            return Charsets.UTF_8
        }
        if (bytes.size >= 2) {
            if (bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte()) {
                return Charsets.UTF_16BE
            }
            if (bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte()) {
                return Charsets.UTF_16LE
            }
        }
        // Default: attempt UTF-8, fallback to ISO-8859-1
        return try {
            val decoded = String(bytes, Charsets.UTF_8)
            if (decoded.contains('\uFFFD')) Charsets.ISO_8859_1
            else Charsets.UTF_8
        } catch (_: Exception) {
            Charsets.ISO_8859_1
        }
    }

    private fun getFileName(uri: Uri): String {
        val projection = arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(0)
            }
        }
        return uri.lastPathSegment ?: "unknown"
    }

    private fun getParentUri(uri: Uri): Uri? {
        return try {
            val docId = DocumentsContract.getDocumentId(uri)
            val treeUri = DocumentsContract.buildTreeDocumentUri(
                uri.authority, docId.substringBeforeLast('/')
            )
            treeUri
        } catch (_: Exception) {
            null
        }
    }
}
