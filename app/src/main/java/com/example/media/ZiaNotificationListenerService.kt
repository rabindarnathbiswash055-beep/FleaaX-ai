package com.example.media

import android.app.Notification
import android.content.Context
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.data.NotificationSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Optional, user-controlled NotificationListenerService.
 * Strictly keeps recent notification summaries in volatile local memory only when enabled by the user.
 * Never logs or uploads notification contents to any server.
 */
class ZiaNotificationListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null || sbn.isOngoing) return
        if (sbn.packageName == packageName) return

        val extras = sbn.notification?.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim().orEmpty()
        if (title.isEmpty() && text.isEmpty()) return

        // Redact any OTP or passcode before holding even in volatile memory
        val safePreview = if (text.contains("otp", ignoreCase = true) ||
            text.contains("verification code", ignoreCase = true) ||
            text.contains("passcode", ignoreCase = true)
        ) {
            "[Sensitive OTP/Verification Notification Hidden by ZIA PrivacyGuard]"
        } else {
            text.take(120)
        }

        val item = NotificationSummary(
            id = "${sbn.packageName}_${sbn.postTime}",
            packageName = sbn.packageName,
            appTitle = title.ifEmpty { sbn.packageName.substringAfterLast('.') },
            contentPreview = safePreview,
            timestamp = sbn.postTime
        )
        NotificationVault.onNewNotification(item)
    }
}

object NotificationVault {
    private val _recentNotifications = MutableStateFlow<List<NotificationSummary>>(emptyList())
    val recentNotifications: StateFlow<List<NotificationSummary>> = _recentNotifications.asStateFlow()

    fun onNewNotification(summary: NotificationSummary) {
        val updated = (listOf(summary) + _recentNotifications.value.filterNot { it.id == summary.id })
            .take(8)
        _recentNotifications.value = updated
    }

    fun clearNotifications() {
        _recentNotifications.value = emptyList()
    }

    fun isNotificationAccessGranted(context: Context): Boolean {
        return try {
            val enabledListeners = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            )
            !enabledListeners.isNullOrBlank() && enabledListeners.contains(context.packageName)
        } catch (e: Exception) {
            false
        }
    }
}
