package com.example.service

import android.app.Notification
import android.content.ComponentName
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.data.IslandPreferencesRepository
import com.example.data.IslandStateController
import com.example.data.nexaDataStore
import com.example.model.IslandEventItem
import com.example.model.IslandEventType
import com.example.util.SystemPermissionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class NexaNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var preferencesRepository: IslandPreferencesRepository

    override fun onCreate() {
        super.onCreate()
        preferencesRepository = IslandPreferencesRepository(applicationContext.nexaDataStore)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        IslandStateController.setListenerConnected(true)
        IslandStateController.onDismissSystemNotification = { key ->
            try {
                cancelNotification(key)
            } catch (_: Exception) {
                // Ignore if notification cannot be dismissed by listener
            }
        }

        serviceScope.launch {
            val config = preferencesRepository.getSnapshot()
            if (config.masterEnabled && SystemPermissionHelper.hasOverlayPermission(applicationContext)) {
                NexaOverlayService.start(applicationContext)
            }
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        IslandStateController.setListenerConnected(false)
        IslandStateController.onDismissSystemNotification = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                requestRebind(ComponentName(this, NexaNotificationListenerService::class.java))
            } catch (_: Exception) {
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val posted = sbn ?: return

        // Ignore our own foreground service status notification
        if (posted.packageName == packageName && posted.id == NexaOverlayService.FGS_NOTIFICATION_ID) {
            return
        }

        val notification = posted.notification ?: return

        // Skip empty group summary containers
        if ((notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0) {
            return
        }

        serviceScope.launch {
            val config = preferencesRepository.getSnapshot()
            if (!config.masterEnabled) return@launch

            // Filter by user-blocked applications
            if (config.blockedPackages.contains(posted.packageName)) {
                return@launch
            }

            val extras = notification.extras
            val rawTitle = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim()
                ?: extras?.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)?.toString()?.trim()
                ?: ""
            val rawText = extras?.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()?.trim()
                ?: extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim()
                ?: extras?.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()?.trim()
                ?: ""

            if (rawTitle.isBlank() && rawText.isBlank()) {
                return@launch
            }

            val pm = applicationContext.packageManager
            val appName = try {
                val appInfo = pm.getApplicationInfo(posted.packageName, 0)
                pm.getApplicationLabel(appInfo).toString()
            } catch (_: Exception) {
                posted.packageName.substringAfterLast('.')
            }

            val appIcon = try {
                pm.getApplicationIcon(posted.packageName)
            } catch (_: Exception) {
                null
            }

            val category = notification.category
            val hasMediaSession = extras?.containsKey(Notification.EXTRA_MEDIA_SESSION) == true
            val eventType = when {
                category == Notification.CATEGORY_CALL && posted.isOngoing -> IslandEventType.ONGOING_CALL
                category == Notification.CATEGORY_CALL -> IslandEventType.INCOMING_CALL
                category == Notification.CATEGORY_TRANSPORT || hasMediaSession -> IslandEventType.MUSIC_PLAYBACK
                category == Notification.CATEGORY_ALARM || category == Notification.CATEGORY_STOPWATCH -> IslandEventType.TIMER_COUNTDOWN
                else -> IslandEventType.NOTIFICATION
            }

            val isSensitive = notification.visibility == Notification.VISIBILITY_PRIVATE ||
                notification.visibility == Notification.VISIBILITY_SECRET

            val item = IslandEventItem(
                key = posted.key ?: "${posted.packageName}_${posted.id}",
                packageName = posted.packageName,
                appName = appName,
                title = rawTitle.ifBlank { appName },
                message = rawText,
                timestamp = posted.postTime.takeIf { it > 0 } ?: System.currentTimeMillis(),
                eventType = eventType,
                isSensitive = isSensitive,
                isOngoing = posted.isOngoing,
                contentIntent = notification.contentIntent,
                appIcon = appIcon
            )

            IslandStateController.postOrUpdateEvent(
                item = item,
                groupSameApp = config.groupSameApp,
                autoExpand = config.autoExpandNotifications
            )

            if (SystemPermissionHelper.hasOverlayPermission(applicationContext) &&
                !IslandStateController.isOverlayServiceRunning.value
            ) {
                NexaOverlayService.start(applicationContext)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        val removed = sbn ?: return
        val key = removed.key ?: "${removed.packageName}_${removed.id}"
        IslandStateController.removeEventByKey(key)
    }

    override fun onDestroy() {
        IslandStateController.setListenerConnected(false)
        IslandStateController.onDismissSystemNotification = null
        serviceScope.cancel()
        super.onDestroy()
    }
}
