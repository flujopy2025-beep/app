package com.seoaudit.app.feature.gsc.domain.model

/**
 * Represents a site registered in Google Search Console.
 */
data class GscSite(
    val siteUrl: String,
    val permissionLevel: String
)

/**
 * Result of a successful GSC authentication.
 */
data class GscAuthResult(
    val sites: List<GscSite>,
    val email: String
)
