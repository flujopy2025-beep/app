package com.seoaudit.app.feature.wordpress.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for a WordPress page from the REST API.
 */
@Serializable
data class WpPageDto(
    val id: Long,
    val title: RenderedContent = RenderedContent(),
    val content: RenderedContent = RenderedContent(),
    val excerpt: RenderedContent = RenderedContent(),
    val slug: String = "",
    val status: String = "draft",
    val link: String = "",
    @SerialName("yoast_head_json")
    val yoastMeta: YoastMeta? = null
)

@Serializable
data class RenderedContent(
    val rendered: String = ""
)

@Serializable
data class YoastMeta(
    val description: String? = null
)

/**
 * DTO for a WordPress theme from the REST API.
 */
@Serializable
data class WpThemeDto(
    val stylesheet: String = "",
    val name: RenderedContent = RenderedContent(),
    val version: String = "",
    val status: String = "inactive"
)

/**
 * DTO for a WordPress plugin from the REST API.
 */
@Serializable
data class WpPluginDto(
    val plugin: String = "",
    val name: String = "",
    val version: String = "",
    val status: String = "inactive"
)

/**
 * Parameters for listing pages via API query params.
 */
data class WpPageListParams(
    val page: Int = 1,
    val perPage: Int = 20,
    val status: String? = null,
    val search: String? = null
)

/**
 * DTO for updating a WordPress page.
 */
@Serializable
data class WpPageUpdateDto(
    val title: String? = null,
    val content: String? = null,
    val excerpt: String? = null,
    val slug: String? = null,
    val status: String? = null
)
