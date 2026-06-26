package com.seoaudit.app.feature.wordpress.data

import com.seoaudit.app.core.domain.repository.ConnectionStatus
import com.seoaudit.app.core.domain.repository.WordPressRepository
import com.seoaudit.app.feature.wordpress.data.remote.WpApiService
import com.seoaudit.app.feature.wordpress.data.remote.WpPageDto
import com.seoaudit.app.feature.wordpress.data.remote.WpPageListParams
import com.seoaudit.app.feature.wordpress.data.remote.WpPageUpdateDto
import com.seoaudit.app.feature.wordpress.data.remote.WpPluginDto
import com.seoaudit.app.feature.wordpress.data.remote.WpThemeDto
import com.seoaudit.app.feature.wordpress.domain.model.PageListParams
import com.seoaudit.app.feature.wordpress.domain.model.PageStatus
import com.seoaudit.app.feature.wordpress.domain.model.PageUpdate
import com.seoaudit.app.feature.wordpress.domain.model.PaginatedResult
import com.seoaudit.app.feature.wordpress.domain.model.WordPressPage
import com.seoaudit.app.feature.wordpress.domain.model.WpAuthResult
import com.seoaudit.app.feature.wordpress.domain.model.WpConnectionConfig
import com.seoaudit.app.feature.wordpress.domain.model.WpPlugin
import com.seoaudit.app.feature.wordpress.domain.model.WpTheme
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of WordPressRepository using Ktor HTTP client.
 * Handles DTO-to-Domain mapping and connection state management.
 */
@Singleton
class WordPressRepositoryImpl @Inject constructor(
    private val wpApiService: WpApiService
) : WordPressRepository {

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    private var currentSiteUrl: String = ""

    override suspend fun authenticate(config: WpConnectionConfig): Result<WpAuthResult> =
        runCatching {
            _connectionStatus.value = ConnectionStatus.CONNECTING
            wpApiService.configure(config.siteUrl, config.username, config.appPassword)
            currentSiteUrl = config.siteUrl.trimEnd('/')

            // Verify connection by listing pages (tests read permission)
            val response = wpApiService.listPages(WpPageListParams(perPage = 1))
            val pages: List<WpPageDto> = response.body()

            // Verify write permission with a test update (dry run via reading)
            val hasWrite = verifyWritePermission()

            _connectionStatus.value = ConnectionStatus.CONNECTED
            WpAuthResult(
                siteName = extractSiteName(currentSiteUrl),
                siteUrl = currentSiteUrl,
                wpVersion = "detected",
                hasWritePermission = hasWrite
            )
        }.onFailure {
            _connectionStatus.value = ConnectionStatus.ERROR
        }

    override suspend fun getPage(pageId: Long): Result<WordPressPage> = runCatching {
        val dto = wpApiService.getPage(pageId)
        dto.toDomain(currentSiteUrl)
    }

    override suspend fun listPages(params: PageListParams): Result<PaginatedResult<WordPressPage>> =
        runCatching {
            val apiParams = params.toApiParams()
            val response: HttpResponse = wpApiService.listPages(apiParams)
            val pages: List<WpPageDto> = response.body()
            val totalItems = response.headers["X-WP-Total"]?.toIntOrNull() ?: pages.size
            val totalPages = response.headers["X-WP-TotalPages"]?.toIntOrNull() ?: 1

            PaginatedResult(
                items = pages.map { it.toDomain(currentSiteUrl) },
                totalItems = totalItems,
                totalPages = totalPages,
                currentPage = params.page
            )
        }

    override suspend fun updatePage(pageId: Long, update: PageUpdate): Result<WordPressPage> =
        runCatching {
            val updateDto = update.toDto()
            val dto = wpApiService.updatePage(pageId, updateDto)
            dto.toDomain(currentSiteUrl)
        }

    override suspend fun listThemes(): Result<List<WpTheme>> = runCatching {
        wpApiService.listThemes().map { it.toDomain() }
    }

    override suspend fun listPlugins(): Result<List<WpPlugin>> = runCatching {
        wpApiService.listPlugins().map { it.toDomain() }
    }

    override fun observeConnectionStatus(): Flow<ConnectionStatus> = _connectionStatus

    // --- DTO to Domain Mappers ---

    private fun WpPageDto.toDomain(siteUrl: String): WordPressPage = WordPressPage(
        id = id,
        title = title.rendered,
        content = content.rendered,
        excerpt = excerpt.rendered,
        slug = slug,
        status = PageStatus.fromString(status),
        url = link,
        siteUrl = siteUrl,
        metaDescription = yoastMeta?.description ?: ""
    )

    private fun WpThemeDto.toDomain(): WpTheme = WpTheme(
        stylesheet = stylesheet,
        name = name.rendered,
        version = version,
        isActive = status == "active"
    )

    private fun WpPluginDto.toDomain(): WpPlugin = WpPlugin(
        plugin = plugin,
        name = name,
        version = version,
        isActive = status == "active"
    )

    // --- Domain to DTO Mappers ---

    private fun PageListParams.toApiParams(): WpPageListParams = WpPageListParams(
        page = page,
        perPage = perPage,
        status = status?.name?.lowercase(),
        search = search
    )

    private fun PageUpdate.toDto(): WpPageUpdateDto = WpPageUpdateDto(
        title = title,
        content = content,
        excerpt = excerpt,
        slug = slug,
        status = status?.name?.lowercase()
    )

    // --- Helpers ---

    private suspend fun verifyWritePermission(): Boolean = try {
        // Attempt to list plugins - requires edit permission
        wpApiService.listPlugins()
        true
    } catch (_: Exception) {
        false
    }

    private fun extractSiteName(url: String): String {
        return url.removePrefix("https://")
            .removePrefix("http://")
            .removeSuffix("/")
    }
}
