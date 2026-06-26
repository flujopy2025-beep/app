package com.seoaudit.app.background

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

@AndroidEntryPoint
class AuditForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isRunning = false

    companion object {
        const val ACTION_START = "com.seoaudit.app.ACTION_START_AUDIT"
        const val ACTION_STOP = "com.seoaudit.app.ACTION_STOP_AUDIT"
        const val EXTRA_SITE_URL = "site_url"
        const val EXTRA_MAX_PAGES = "max_pages"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startAudit(
                siteUrl = intent.getStringExtra(EXTRA_SITE_URL) ?: "",
                maxPages = intent.getIntExtra(EXTRA_MAX_PAGES, 50)
            )
            ACTION_STOP -> stopAudit()
        }
        return START_STICKY
    }

    private fun startAudit(siteUrl: String, maxPages: Int) {
        if (isRunning) return
        isRunning = true

        val notification = NotificationHelper.createAuditProgressNotification(
            this, 0, maxPages
        )
        startForeground(NotificationHelper.AUDIT_NOTIFICATION_ID, notification)

        serviceScope.launch {
            try {
                // The actual audit logic would be injected via Hilt
                // Progress updates received via Flow from AuditSiteUseCase
            } catch (e: CancellationException) {
                // Save checkpoint on cancellation
                saveCheckpoint()
            } finally {
                stopSelf()
            }
        }
    }

    private fun stopAudit() {
        isRunning = false
        serviceScope.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun saveCheckpoint() {
        // Checkpoint saving logic - saves progress to Room DB
        // Will be fully implemented in Phase 4 (task 7.4)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
