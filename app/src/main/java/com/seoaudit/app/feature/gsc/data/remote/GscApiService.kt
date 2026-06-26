package com.seoaudit.app.feature.gsc.data.remote

import com.seoaudit.app.feature.gsc.data.GscTokenManager
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.encodeURLPath
import javax.inject.Inject
import javax.inject.Singleton

/**
 * HTTP client for Google Search Console API.
 *
 * Provides methods to query search analytics and list sites.
 * Uses bearer auth token from GscTokenManager.
 */
@Singleton
class GscApiService @Inject constructor(
    private val httpClient: HttpClient,
    private val tokenManager: GscTokenManager
) {
    companion object {
        private const val BASE_URL =
            "https://searchconsole.googleapis.com/webmasters/v3"
    }

    /**
     * Queries search analytics data for a specific site.
     * POST /sites/{siteUrl}/searchAnalytics/query
     */
    suspend fun queryAnalytics(
        siteUrl: String,
        request: GscAnalyticsRequest
    ): GscAnalyticsResponse {
        val token = tokenManager.getValidToken()
        val encodedSiteUrl = siteUrl.encodeURLPath()
        return httpClient.post(
            "$BASE_URL/sites/$encodedSiteUrl/searchAnalytics/query"
        ) {
            bearerAuth(token)
            setBody(request)
        }.body()
    }

    /**
     * Lists all sites available in the user's Search Console account.
     * GET /sites
     */
    suspend fun listSites(): GscSitesListResponse {
        val token = tokenManager.getValidToken()
        return httpClient.get("$BASE_URL/sites") {
            bearerAuth(token)
        }.body()
    }
}
