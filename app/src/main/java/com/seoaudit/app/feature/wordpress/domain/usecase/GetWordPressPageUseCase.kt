package com.seoaudit.app.feature.wordpress.domain.usecase

import com.seoaudit.app.core.domain.repository.WordPressRepository
import com.seoaudit.app.feature.wordpress.domain.model.WordPressPage
import javax.inject.Inject

/**
 * Use case for retrieving a single WordPress page by ID.
 * Returns complete content including title, meta, HTML, and slug.
 */
class GetWordPressPageUseCase @Inject constructor(
    private val wpRepository: WordPressRepository
) {
    suspend operator fun invoke(pageId: Long): Result<WordPressPage> {
        return wpRepository.getPage(pageId)
    }
}
