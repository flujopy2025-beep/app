package com.seoaudit.app.background

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat

object NotificationHelper {
    const val SYNC_NOTIFICATION_ID = 1001
    const val AUDIT_NOTIFICATION_ID = 1002
    private const val SYNC_CHANNEL_ID = "gsc_sync_channel"
    private const val AUDIT_CHANNEL_ID = "audit_progress_channel"

    fun createNotificationChannels(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val syncChannel = NotificationChannel(
            SYNC_CHANNEL_ID,
            "Sincronización GSC",
            NotificationManager.IMPORTANCE_LOW
        )
        syncChannel.description =
            "Sincronización periódica de datos de Google Search Console"
        notificationManager.createNotificationChannel(syncChannel)

        val auditChannel = NotificationChannel(
            AUDIT_CHANNEL_ID,
            "Progreso de Auditoría",
            NotificationManager.IMPORTANCE_LOW
        )
        auditChannel.description = "Progreso de auditorías SEO en curso"
        notificationManager.createNotificationChannel(auditChannel)
    }

    fun createSyncNotification(context: Context): Notification {
        return NotificationCompat.Builder(context, SYNC_CHANNEL_ID)
            .setContentTitle("Sincronizando datos SEO")
            .setContentText("Actualizando métricas de Google Search Console")
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setOngoing(true)
            .setProgress(0, 0, true)
            .build()
    }

    fun createAuditProgressNotification(
        context: Context,
        current: Int,
        total: Int
    ): Notification {
        return NotificationCompat.Builder(context, AUDIT_CHANNEL_ID)
            .setContentTitle("Auditoría SEO en progreso")
            .setContentText("Analizando página $current de $total")
            .setSmallIcon(android.R.drawable.ic_menu_search)
            .setOngoing(true)
            .setProgress(total, current, false)
            .build()
    }
}
