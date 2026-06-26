package com.seoaudit.app.feature.codegen.domain.usecase

import android.net.Uri
import com.seoaudit.app.core.domain.repository.FileSystemRepository
import com.seoaudit.app.core.domain.repository.WordPressRepository
import com.seoaudit.app.feature.wordpress.domain.model.PageUpdate
import com.seoaudit.app.feature.wordpress.domain.model.WordPressPage
import javax.inject.Inject

data class ApplyFixResult(
    val success: Boolean,
    val message: String,
    val backupUri: Uri? = null
)

data class RiskWarning(
    val isRisky: Boolean,
    val fileName: String,
    val warningMessage: String,
    val safeAlternative: String
)

class ApplyFixUseCase @Inject constructor(
    private val wpRepository: WordPressRepository,
    private val fsRepository: FileSystemRepository
) {
    companion object {
        private val RISKY_FILES = listOf("functions.php", "wp-config.php", ".htaccess")
    }

    suspend fun applyToWordPress(pageId: Long, fixedContent: String): Result<WordPressPage> {
        return wpRepository.updatePage(pageId, PageUpdate(content = fixedContent))
    }

    suspend fun applyToLocalFile(fileUri: Uri, fixedContent: ByteArray): Result<ApplyFixResult> {
        // Create backup first
        val backupResult = fsRepository.createBackup(fileUri)
        val backupUri = backupResult.getOrElse {
            return Result.failure(it)
        }

        // Apply changes
        fsRepository.writeFile(fileUri, fixedContent).getOrElse {
            return Result.failure(it)
        }

        return Result.success(ApplyFixResult(
            success = true,
            message = "Corrección aplicada exitosamente",
            backupUri = backupUri
        ))
    }

    fun checkRiskWarning(fileName: String): RiskWarning {
        val isRisky = RISKY_FILES.any { fileName.endsWith(it, ignoreCase = true) }
        return if (isRisky) {
            RiskWarning(
                isRisky = true,
                fileName = fileName,
                warningMessage = "Editar '$fileName' directamente puede comprometer la estabilidad del sitio.",
                safeAlternative = "Use un plugin de snippets (Code Snippets) o un Child Theme para aplicar cambios de forma segura."
            )
        } else {
            RiskWarning(isRisky = false, fileName = fileName, warningMessage = "", safeAlternative = "")
        }
    }
}
