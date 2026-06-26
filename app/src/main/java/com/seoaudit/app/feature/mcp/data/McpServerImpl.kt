package com.seoaudit.app.feature.mcp.data

import com.seoaudit.app.core.domain.model.McpToolResult
import com.seoaudit.app.core.domain.model.ServiceType
import com.seoaudit.app.core.domain.model.SiteAuditProgress
import com.seoaudit.app.core.domain.repository.CredentialRepository
import com.seoaudit.app.core.domain.repository.GscDimension
import com.seoaudit.app.core.domain.repository.GscRepository
import com.seoaudit.app.core.domain.repository.WordPressRepository
import com.seoaudit.app.core.domain.util.DateUtils
import com.seoaudit.app.feature.audit.domain.usecase.AuditPageUseCase
import com.seoaudit.app.feature.audit.domain.usecase.AuditSiteUseCase
import com.seoaudit.app.feature.codegen.domain.usecase.ApplyFixUseCase
import com.seoaudit.app.feature.wordpress.domain.model.PageListParams
import com.seoaudit.app.feature.wordpress.domain.model.WpConnectionConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MCP Tool definition for server registration.
 */
data class McpToolDefinition(
    val name: String,
    val description: String,
    val inputSchema: JsonObject
)

/**
 * MCP Server implementation that exposes SEO audit capabilities
 * as MCP tools following the Model Context Protocol specification.
 *
 * Validates: Requirements 7.1, 7.2, 7.3, 7.4, 7.5
 */
@Singleton
class McpServerImpl @Inject constructor(
    private val gscRepository: GscRepository,
    private val wpRepository: WordPressRepository,
    private val auditPageUseCase: AuditPageUseCase,
    private val auditSiteUseCase: AuditSiteUseCase,
    private val applyFixUseCase: ApplyFixUseCase,
    private val credentialRepository: CredentialRepository,
    private val resourceProvider: McpResourceProvider,
    private val json: Json
) {

    val tools: List<McpToolDefinition> = buildToolDefinitions()

    private var isRunning: Boolean = false

    fun isServerRunning(): Boolean = isRunning

    fun start() { isRunning = true }

    fun stop() { isRunning = false }

    /**
     * Dispatches a tool call to the appropriate handler.
     * Validates parameters and returns structured results.
     */
    suspend fun handleToolCall(
        toolName: String,
        params: JsonObject
    ): McpToolResult {
        if (!isRunning) {
            return McpToolResult.error(
                "MCP Server is not running",
                code = -32000
            )
        }

        return when (toolName) {
            "gsc_authenticate" -> handleGscAuthenticate()
            "gsc_list_sites" -> handleGscListSites()
            "gsc_get_performance" -> handleGscGetPerformance(params)
            "wp_authenticate" -> handleWpAuthenticate(params)
            "wp_list_pages" -> handleWpListPages(params)
            "audit_page" -> handleAuditPage(params)
            "audit_site" -> handleAuditSite(params)
            "generate_fix" -> handleGenerateFix(params)
            "apply_fix" -> handleApplyFix(params)
            "disconnect_account" -> handleDisconnect(params)
            else -> McpToolResult.error(
                "Tool not found: $toolName",
                code = -32601
            )
        }
    }

    private suspend fun handleGscAuthenticate(): McpToolResult {
        return gscRepository.authenticate().fold(
            onSuccess = {
                McpToolResult.success(
                    """{"status":"connected","message":"GSC authenticated"}"""
                )
            },
            onFailure = {
                McpToolResult.error(
                    it.message ?: "GSC authentication failed",
                    code = -32000
                )
            }
        )
    }

    private suspend fun handleGscListSites(): McpToolResult {
        return gscRepository.listSites().fold(
            onSuccess = { sites ->
                val sitesJson = sites.joinToString(",") { site ->
                    """{"siteUrl":"${site.siteUrl}"}"""
                }
                McpToolResult.success("""{"sites":[$sitesJson]}""")
            },
            onFailure = {
                McpToolResult.error(
                    it.message ?: "Failed to list GSC sites",
                    code = -32000
                )
            }
        )
    }

    private suspend fun handleGscGetPerformance(
        params: JsonObject
    ): McpToolResult {
        val siteUrl = params["siteUrl"]
            ?.jsonPrimitive?.content
            ?: return McpToolResult.error(
                "Missing required parameter: siteUrl",
                code = -32602
            )

        val dateRange = DateUtils.defaultDateRange()

        return gscRepository.getPerformanceData(
            siteUrl = siteUrl,
            dateRange = dateRange,
            dimensions = listOf(GscDimension.PAGE)
        ).fold(
            onSuccess = { data ->
                val rowsJson = data.rows.take(50).joinToString(",") { row ->
                    """{"keys":${row.keys},"clicks":${row.clicks},""" +
                        """"impressions":${row.impressions},""" +
                        """"ctr":${row.ctr},"position":${row.position}}"""
                }
                McpToolResult.success(
                    """{"siteUrl":"${data.siteUrl}","rows":[$rowsJson]}"""
                )
            },
            onFailure = {
                McpToolResult.error(
                    it.message ?: "Failed to get performance data",
                    code = -32000
                )
            }
        )
    }

    private suspend fun handleWpAuthenticate(
        params: JsonObject
    ): McpToolResult {
        val siteUrl = params["siteUrl"]?.jsonPrimitive?.content
            ?: return McpToolResult.error(
                "Missing required parameter: siteUrl",
                code = -32602
            )
        val username = params["username"]?.jsonPrimitive?.content
            ?: return McpToolResult.error(
                "Missing required parameter: username",
                code = -32602
            )
        val appPassword = params["appPassword"]?.jsonPrimitive?.content
            ?: return McpToolResult.error(
                "Missing required parameter: appPassword",
                code = -32602
            )

        val config = WpConnectionConfig(siteUrl, username, appPassword)
        return wpRepository.authenticate(config).fold(
            onSuccess = {
                McpToolResult.success(
                    """{"status":"connected","siteName":"${it.siteName}"}"""
                )
            },
            onFailure = {
                McpToolResult.error(
                    it.message ?: "WordPress authentication failed",
                    code = -32000
                )
            }
        )
    }

    private suspend fun handleWpListPages(
        params: JsonObject
    ): McpToolResult {
        val perPage = params["perPage"]
            ?.jsonPrimitive?.longOrNull?.toInt() ?: 20

        return wpRepository.listPages(
            PageListParams(perPage = perPage)
        ).fold(
            onSuccess = { result ->
                val pagesJson = result.items.joinToString(",") { page ->
                    """{"id":${page.id},"title":"${page.title}",""" +
                        """"url":"${page.url}","status":"${page.status}"}"""
                }
                McpToolResult.success(
                    """{"pages":[$pagesJson],"total":${result.totalItems}}"""
                )
            },
            onFailure = {
                McpToolResult.error(
                    it.message ?: "Failed to list pages",
                    code = -32000
                )
            }
        )
    }

    private suspend fun handleAuditPage(
        params: JsonObject
    ): McpToolResult {
        val pageId = params["pageId"]?.jsonPrimitive?.longOrNull
            ?: return McpToolResult.error(
                "Missing required parameter: pageId",
                code = -32602
            )

        return auditPageUseCase(pageId).fold(
            onSuccess = { report ->
                McpToolResult.success(
                    """{"pageUrl":"${report.pageUrl}",""" +
                        """"score":${report.score},""" +
                        """"issueCount":${report.issues.size}}"""
                )
            },
            onFailure = {
                McpToolResult.error(
                    it.message ?: "Audit failed",
                    code = -32000
                )
            }
        )
    }

    private suspend fun handleAuditSite(
        params: JsonObject
    ): McpToolResult {
        val maxPages = params["maxPages"]
            ?.jsonPrimitive?.longOrNull?.toInt() ?: 50

        var lastProgress: SiteAuditProgress? = null
        auditSiteUseCase(maxPages).collect { progress ->
            lastProgress = progress
        }

        return when (val result = lastProgress) {
            is SiteAuditProgress.Complete -> {
                val report = result.report
                McpToolResult.success(
                    """{"siteUrl":"${report.siteUrl}",""" +
                        """"overallScore":${report.overallScore},""" +
                        """"auditedPages":${report.auditedPages},""" +
                        """"critical":${report.summary.critical},""" +
                        """"warnings":${report.summary.warnings}}"""
                )
            }
            is SiteAuditProgress.Error -> {
                McpToolResult.error(
                    result.error.message ?: "Site audit failed",
                    code = -32000
                )
            }
            else -> McpToolResult.error(
                "Audit did not complete", code = -32000
            )
        }
    }

    private suspend fun handleGenerateFix(
        params: JsonObject
    ): McpToolResult {
        // Validate required params
        params["issueId"]?.jsonPrimitive?.content
            ?: return McpToolResult.error(
                "Missing required parameter: issueId",
                code = -32602
            )
        params["originalCode"]?.jsonPrimitive?.content
            ?: return McpToolResult.error(
                "Missing required parameter: originalCode",
                code = -32602
            )

        // Fix generation requires an active audit context
        return McpToolResult.success(
            """{"status":"pending","message":"Fix generation requires active audit context"}"""
        )
    }

    private suspend fun handleApplyFix(
        params: JsonObject
    ): McpToolResult {
        val pageId = params["pageId"]?.jsonPrimitive?.longOrNull
            ?: return McpToolResult.error(
                "Missing required parameter: pageId",
                code = -32602
            )
        val fixedContent = params["fixedContent"]?.jsonPrimitive?.content
            ?: return McpToolResult.error(
                "Missing required parameter: fixedContent",
                code = -32602
            )

        return applyFixUseCase.applyToWordPress(pageId, fixedContent).fold(
            onSuccess = {
                McpToolResult.success(
                    """{"status":"applied","pageId":${it.id}}"""
                )
            },
            onFailure = {
                McpToolResult.error(
                    it.message ?: "Failed to apply fix",
                    code = -32000
                )
            }
        )
    }

    private suspend fun handleDisconnect(
        params: JsonObject
    ): McpToolResult {
        val service = params["service"]?.jsonPrimitive?.content
            ?: return McpToolResult.error(
                "Missing required parameter: service",
                code = -32602
            )

        val serviceType = when (service.lowercase()) {
            "gsc" -> ServiceType.GSC
            "wordpress", "wp" -> ServiceType.WORDPRESS
            else -> return McpToolResult.error(
                "Invalid service: $service. Use 'gsc' or 'wordpress'",
                code = -32602
            )
        }

        credentialRepository.deleteCredential(serviceType.credentialKey)
        return McpToolResult.success(
            """{"status":"disconnected","service":"$service"}"""
        )
    }

    companion object {
        fun buildToolDefinitions(): List<McpToolDefinition> = listOf(
            McpToolDefinition(
                name = "gsc_authenticate",
                description = "Authenticate with Google Search Console via OAuth 2.0",
                inputSchema = buildJsonObject {
                    put("type", "object")
                    put("properties", buildJsonObject { })
                    put("required", buildJsonObject { })
                }
            ),
            McpToolDefinition(
                name = "gsc_list_sites",
                description = "List available sites in Google Search Console",
                inputSchema = buildJsonObject {
                    put("type", "object")
                    put("properties", buildJsonObject { })
                }
            ),
            McpToolDefinition(
                name = "gsc_get_performance",
                description = "Get SEO performance metrics from GSC",
                inputSchema = gscPerformanceSchema()
            ),
            McpToolDefinition(
                name = "wp_authenticate",
                description = "Authenticate with a WordPress site",
                inputSchema = wpAuthSchema()
            ),
            McpToolDefinition(
                name = "wp_list_pages",
                description = "List pages from the WordPress site",
                inputSchema = wpListPagesSchema()
            ),
            McpToolDefinition(
                name = "audit_page",
                description = "Audit a single page for SEO issues",
                inputSchema = auditPageSchema()
            ),
            McpToolDefinition(
                name = "audit_site",
                description = "Audit the entire WordPress site for SEO",
                inputSchema = auditSiteSchema()
            ),
            McpToolDefinition(
                name = "generate_fix",
                description = "Generate code fix for an SEO issue",
                inputSchema = generateFixSchema()
            ),
            McpToolDefinition(
                name = "apply_fix",
                description = "Apply a code fix to a WordPress page",
                inputSchema = applyFixSchema()
            ),
            McpToolDefinition(
                name = "disconnect_account",
                description = "Disconnect a service account (GSC or WordPress)",
                inputSchema = disconnectSchema()
            )
        )

        private fun gscPerformanceSchema() = buildJsonObject {
            put("type", "object")
            put("properties", buildJsonObject {
                put("siteUrl", buildJsonObject {
                    put("type", "string")
                    put("description", "The site URL registered in GSC")
                })
            })
            put("required", buildJsonObject {
                put("0", "siteUrl")
            })
        }

        private fun wpAuthSchema() = buildJsonObject {
            put("type", "object")
            put("properties", buildJsonObject {
                put("siteUrl", buildJsonObject {
                    put("type", "string")
                    put("description", "WordPress site URL")
                })
                put("username", buildJsonObject {
                    put("type", "string")
                    put("description", "WordPress username")
                })
                put("appPassword", buildJsonObject {
                    put("type", "string")
                    put("description", "WordPress Application Password")
                })
            })
            put("required", buildJsonObject {
                put("0", "siteUrl")
                put("1", "username")
                put("2", "appPassword")
            })
        }

        private fun wpListPagesSchema() = buildJsonObject {
            put("type", "object")
            put("properties", buildJsonObject {
                put("perPage", buildJsonObject {
                    put("type", "integer")
                    put("description", "Number of pages per request (default: 20)")
                })
            })
        }

        private fun auditPageSchema() = buildJsonObject {
            put("type", "object")
            put("properties", buildJsonObject {
                put("pageId", buildJsonObject {
                    put("type", "integer")
                    put("description", "WordPress page ID to audit")
                })
            })
            put("required", buildJsonObject {
                put("0", "pageId")
            })
        }

        private fun auditSiteSchema() = buildJsonObject {
            put("type", "object")
            put("properties", buildJsonObject {
                put("maxPages", buildJsonObject {
                    put("type", "integer")
                    put("description", "Maximum pages to audit (default: 50)")
                })
            })
        }

        private fun generateFixSchema() = buildJsonObject {
            put("type", "object")
            put("properties", buildJsonObject {
                put("issueId", buildJsonObject {
                    put("type", "string")
                    put("description", "ID of the audit issue to fix")
                })
                put("originalCode", buildJsonObject {
                    put("type", "string")
                    put("description", "Original HTML code to fix")
                })
            })
            put("required", buildJsonObject {
                put("0", "issueId")
                put("1", "originalCode")
            })
        }

        private fun applyFixSchema() = buildJsonObject {
            put("type", "object")
            put("properties", buildJsonObject {
                put("pageId", buildJsonObject {
                    put("type", "integer")
                    put("description", "WordPress page ID to update")
                })
                put("fixedContent", buildJsonObject {
                    put("type", "string")
                    put("description", "Fixed HTML content to apply")
                })
            })
            put("required", buildJsonObject {
                put("0", "pageId")
                put("1", "fixedContent")
            })
        }

        private fun disconnectSchema() = buildJsonObject {
            put("type", "object")
            put("properties", buildJsonObject {
                put("service", buildJsonObject {
                    put("type", "string")
                    put("description", "Service to disconnect: 'gsc' or 'wordpress'")
                    put("enum", buildJsonObject {
                        put("0", "gsc")
                        put("1", "wordpress")
                    })
                })
            })
            put("required", buildJsonObject {
                put("0", "service")
            })
        }
    }
}
