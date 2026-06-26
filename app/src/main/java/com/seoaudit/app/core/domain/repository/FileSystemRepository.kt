package com.seoaudit.app.core.domain.repository

import android.net.Uri
import com.seoaudit.app.core.domain.model.FileContent
import com.seoaudit.app.core.domain.model.FileInfo

/**
 * Repository interface for file system operations via SAF.
 * Provides methods to read, write, backup, and list files
 * in user-selected directories with persisted permissions.
 *
 * Validates: Requirements 13.1, 13.2, 13.3, 13.4, 22.1, 22.2, 22.3, 22.4, 22.5
 */
interface FileSystemRepository {

    /**
     * Reads file content from the given URI, detecting charset encoding.
     *
     * @param uri URI of the file to read
     * @return Result containing the file content with detected charset
     */
    suspend fun readFile(uri: Uri): Result<FileContent>

    /**
     * Writes content to the file at the given URI, preserving encoding.
     *
     * @param uri URI of the file to write
     * @param content Byte content to write
     * @return Result.success(Unit) if written successfully
     */
    suspend fun writeFile(uri: Uri, content: ByteArray): Result<Unit>

    /**
     * Creates a backup copy of the file before modification.
     *
     * @param uri URI of the file to backup
     * @return Result containing the URI of the backup file
     */
    suspend fun createBackup(uri: Uri): Result<Uri>

    /**
     * Lists files in a directory, optionally filtered by extensions.
     *
     * @param directoryUri URI of the directory to list
     * @param extensions Optional list of file extensions to filter (e.g., "html", "php")
     * @return Result containing list of file metadata
     */
    suspend fun listFiles(
        directoryUri: Uri,
        extensions: List<String>? = null
    ): Result<List<FileInfo>>

    /**
     * Checks whether the app has persisted permission for the given URI.
     *
     * @param uri URI to check permission for
     * @return true if permission is persisted, false otherwise
     */
    fun hasPersistedPermission(uri: Uri): Boolean
}
