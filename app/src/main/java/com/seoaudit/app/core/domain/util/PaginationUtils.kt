package com.seoaudit.app.core.domain.util

import kotlin.math.ceil

/**
 * Utility for computing pagination metadata.
 * Activates pagination only when total items exceed threshold.
 */
object PaginationUtils {
    const val PAGE_SIZE = 50
    const val PAGINATION_THRESHOLD = 50

    fun shouldPaginate(totalItems: Int): Boolean = totalItems > PAGINATION_THRESHOLD

    fun totalPages(totalItems: Int, pageSize: Int = PAGE_SIZE): Int =
        if (totalItems <= 0) 1 else ceil(totalItems.toDouble() / pageSize).toInt()

    fun <T> paginateList(items: List<T>, page: Int, pageSize: Int = PAGE_SIZE): PaginatedPage<T> {
        val total = items.size
        val totalPages = totalPages(total, pageSize)
        val startIndex = (page - 1) * pageSize
        val endIndex = minOf(startIndex + pageSize, total)
        val pageItems = if (startIndex < total) items.subList(startIndex, endIndex) else emptyList()

        return PaginatedPage(
            items = pageItems,
            currentPage = page,
            totalPages = totalPages,
            totalItems = total,
            pageSize = pageSize
        )
    }
}

data class PaginatedPage<T>(
    val items: List<T>,
    val currentPage: Int,
    val totalPages: Int,
    val totalItems: Int,
    val pageSize: Int
)
