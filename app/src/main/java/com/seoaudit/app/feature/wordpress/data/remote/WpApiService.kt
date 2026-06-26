package com.seoaudit.app.feature.wordpress.data.remote

import android.util.Base64
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Ktor-based HTTP client for the WordPress REST API.
 * Uses HTTP Basic authentication with Application Passwords.
 */
@Singleton
class WpApiService @Inject constructor(
    private val httpClient: HttpClient
) {
    private var baseUrl: String = ""
    private var authHeader: String = ""

    /**
     * Configures the service with WordPress site credentials.
     */
    fun configure(siteUrl: String, username: String, appPassword: String) {
        baseUrl = siteUrl.trimEnd('/')
        val credentials = "$username:$appPassword"
        authHeader = Base64.encodeToString(
            credentials.toByteArray(Charsets.UTF_8),
            Base64.NO_WRAP
        )
    }

    /**
     * Retrieves a single page by ID.
     */
    suspend fun getPage(pageId: Long): WpPageDto {
        return httpClient.get("$baseUrl/wp-json/wp/v2/pages/$pageId") {
            header("Authorization", "Basic $authHeader")
        }.body()
    }

    /**
     * Lists pages with optional filtering parameters.
     */
    suspend fun listPages(params: WpPageListParams): HttpResponse {
        return httpClient.get("$baseUrl/wp-json/wp/v2/pages") {
            header("Authorization", "Basic $authHeader")
            parameter("per_page", params.perPage)
            parameter("page", params.page)
            params.status?.let { parameter("status", it) }
            params.search?.let { parameter("search", it) }
        }
    }

    /**
     * Updates a page with the given changes.
     */
    suspend fun updatePage(pageId: Long, update: WpPageUpdateDto): WpPageDto {
        return httpClient.post("$baseUrl/wp-json/wp/v2/pages/$pageId") {
            header("Authorization", "Basic $authHeader")
            setBody(update)
        }.body()
    }

    /**
     * Lists all installed themes.
     */
    suspend fun listThemes(): List<WpThemeDto> {
        return httpClient.get("$baseUrl/wp-json/wp/v2/themes") {
            header("Authorization", "Basic $authHeader")
        }.body()
    }

    /**
     * Lists all installed plugins.
     */
    suspend fun listPlugins(): List<WpPluginDto> {
        return httpClient.get("$baseUrl/wp-json/wp/v2/plugins") {
            header("Authorization", "Basic $authHeader")
        }.body()
    }
}
