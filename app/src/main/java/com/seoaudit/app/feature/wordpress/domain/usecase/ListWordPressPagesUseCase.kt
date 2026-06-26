package com.seoaudit.app.feature.wordpress.domain.usecase

import com.seoaudit.app.core.domain.repository.WordPressRepository
import com.seoaudit.app.feature.wordpress.domain.model.PageListParams
import com.seoaudit.app.feature.wordpress.domain.model.PaginatedResult
import com.seoaudit.app.feature.wordpress.domain.model.WordPressPage
import javax.inject.Inject

/**
 * Use case for listing WordPress pages with pagination support.
 */
class ListWordPressPagesUseCase @Inject constructor(
    private val wpRepository: WordPressRepository
) {
    suspend operator fun invoke(
        params: PageListParams = PageListParams()
    ): Result<PaginatedResult<WordPressPage>> {
        return wpRepository.listPages(params)
    }
}
