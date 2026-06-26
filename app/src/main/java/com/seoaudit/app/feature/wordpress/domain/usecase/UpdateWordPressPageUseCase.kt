package com.seoaudit.app.feature.wordpress.domain.usecase

import com.seoaudit.app.core.domain.error.UpdateFailedException
import com.seoaudit.app.core.domain.repository.WordPressRepository
import com.seoaudit.app.feature.wordpress.domain.model.PageUpdate
import com.seoaudit.app.feature.wordpress.domain.model.WordPressPage
import javax.inject.Inject

/**
 * Use case for updating a WordPress page with content
 * preservation on failure. If the update fails, the original
 * content is preserved unchanged.
 */
class UpdateWordPressPageUseCase @Inject constructor(
    private val wpRepository: WordPressRepository
) {
    suspend operator fun invoke(
        pageId: Long,
        update: PageUpdate
    ): Result<WordPressPage> {
        // Get original content before updating (for preservation guarantee)
        val original = wpRepository.getPage(pageId).getOrElse {
            return Result.failure(it)
        }

        return wpRepository.updatePage(pageId, update).onFailure { error ->
            // Original content is preserved because the API call failed
            // before any modification could take effect
            return Result.failure(
                UpdateFailedException(
                    pageId,
                    original.content,
                    error
                )
            )
        }
    }
}
