package com.example.util

import android.Manifest
import android.app.KeyguardManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.service.notification.NotificationListenerService
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.IslandStateController
import com.example.model.InstalledAppInfo
import com.example.model.IslandEventItem
import com.example.model.IslandEventType
import com.example.model.ServiceDiagnosticsState
import com.example.service.NexaNotificationListenerService
import com.example.service.NexaOverlayService

object SystemPermissionHelper {

    private const val TEST_CHANNEL_ID = "nexa_island_test_channel"
    private const val TEST_NOTIFICATION_ID = 9021

    fun hasOverlayPermission(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun hasNotificationListenerPermission(context: Context): Boolean {
        val cn = ComponentName(context, NexaNotificationListenerService::class.java)
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        return !flat.isNullOrEmpty() && flat.contains(cn.flattenToString())
    }

    fun hasPostNotificationsPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun isBatteryOptimizationIgnored(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return false
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun isDeviceLocked(context: Context): Boolean {
        val km = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager ?: return false
        return km.isKeyguardLocked
    }

    fun isXiaomiOrHyperOS(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val brand = Build.BRAND.lowercase()
        return manufacturer.contains("xiaomi") ||
            brand.contains("xiaomi") ||
            brand.contains("redmi") ||
            brand.contains("poco")
    }

    fun getDeviceSummary(): String {
        val brand = Build.BRAND.replaceFirstChar { it.uppercase() }
        val model = Build.MODEL
        val androidVer = Build.VERSION.RELEASE
        val sdk = Build.VERSION.SDK_INT
        val osTag = if (isXiaomiOrHyperOS()) " • Xiaomi / HyperOS" else ""
        return "$brand $model (Android $androidVer / API $sdk)$osTag"
    }

    fun inspectDiagnostics(
        context: Context,
        userPreferenceEnabled: Boolean
    ): ServiceDiagnosticsState {
        return ServiceDiagnosticsState(
            userPreferenceEnabled = userPreferenceEnabled,
            overlayPermissionGranted = hasOverlayPermission(context),
            notificationListenerGranted = hasNotificationListenerPermission(context),
            postNotificationsGranted = hasPostNotificationsPermission(context),
            notificationListenerConnected = IslandStateController.isNotificationListenerConnected.value,
            overlayServiceRunning = IslandStateController.isOverlayServiceRunning.value,
            islandCurrentlyVisible = IslandStateController.isIslandWindowVisible.value,
            batteryOptimizationIgnored = isBatteryOptimizationIgnored(context),
            isXiaomiOrHyperOS = isXiaomiOrHyperOS(),
            deviceSummary = getDeviceSummary()
        )
    }

    fun requestListenerRebind(context: Context) {
        if (!hasNotificationListenerPermission(context)) return
        try {
            NotificationListenerService.requestRebind(
                ComponentName(context, NexaNotificationListenerService::class.java)
            )
        } catch (_: Exception) {
            // Ignore if system refuses rebind while already bound
        }
    }

    fun syncOverlayServiceState(context: Context, masterEnabled: Boolean) {
        if (masterEnabled && hasOverlayPermission(context)) {
            if (hasNotificationListenerPermission(context)) {
                requestListenerRebind(context)
            }
            NexaOverlayService.start(context)
        } else {
            NexaOverlayService.stop(context)
        }
    }

    fun openOverlayPermissionSettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        safeStartActivity(context, intent)
    }

    fun openNotificationListenerSettings(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        safeStartActivity(context, intent)
    }

    fun openBatteryOptimizationSettings(context: Context) {
        try {
            val requestIntent = Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:${context.packageName}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(requestIntent)
        } catch (_: Exception) {
            val fallback = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            safeStartActivity(context, fallback)
        }
    }

    /**
     * Opens Xiaomi HyperOS / MIUI Autostart or App Permission Editor safely,
     * falling back to standard Android App Info if the OEM activity is absent.
     */
    fun openHyperOSAutostartSettings(context: Context) {
        val candidates = listOf(
            Intent("miui.intent.action.OP_AUTO_START").addCategory(Intent.CATEGORY_DEFAULT),
            Intent().setComponent(
                ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
            )
        )
        for (candidate in candidates) {
            try {
                candidate.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(candidate)
                return
            } catch (_: Exception) {
                // Try next candidate
            }
        }
        openAppDetailsSettings(context)
    }

    /**
     * Opens Xiaomi HyperOS / MIUI App Permission Editor (Lock screen / Background pop-up permissions),
     * falling back to standard Android App Info if unavailable.
     */
    fun openHyperOSOtherPermissions(context: Context) {
        val candidates = listOf(
            Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                putExtra("extra_pkgname", context.packageName)
            },
            Intent().apply {
                component = ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.permissions.PermissionsEditorActivity"
                )
                putExtra("extra_pkgname", context.packageName)
            }
        )
        for (candidate in candidates) {
            try {
                candidate.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(candidate)
                return
            } catch (_: Exception) {
                // Try next candidate
            }
        }
        openAppDetailsSettings(context)
    }

    fun openAppDetailsSettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${context.packageName}")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        safeStartActivity(context, intent)
    }

    private fun safeStartActivity(context: Context, intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                context.startActivity(
                    Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } catch (_: Exception) {
            }
        }
    }

    /**
     * Sends a real Android system notification AND routes it to the Dynamic Island controller
     * so the user can test both the system NotificationListenerService pipeline and the live island UI.
     */
    fun triggerRealTestNotification(
        context: Context,
        groupSameApp: Boolean,
        autoExpand: Boolean
    ) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        if (nm != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                TEST_CHANNEL_ID,
                context.getString(R.string.test_notif_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            nm.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = context.getString(R.string.test_notif_title)
        val body = context.getString(R.string.test_notif_body)

        if (nm != null && hasPostNotificationsPermission(context)) {
            try {
                val notification = NotificationCompat.Builder(context, TEST_CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build()
                nm.notify(TEST_NOTIFICATION_ID, notification)
            } catch (_: Exception) {
            }
        }

        // Also ensure immediate display if NotificationListenerService hasn't bound yet in preview
        val appIcon = try {
            context.packageManager.getApplicationIcon(context.packageName)
        } catch (_: Exception) {
            null
        }
        IslandStateController.postOrUpdateEvent(
            item = IslandEventItem(
                key = "nexa_test_${System.currentTimeMillis() / 2000}",
                packageName = context.packageName,
                appName = context.getString(R.string.app_name),
                title = title,
                message = body,
                eventType = IslandEventType.NOTIFICATION,
                contentIntent = pendingIntent,
                appIcon = appIcon
            ),
            groupSameApp = groupSameApp,
            autoExpand = autoExpand
        )
    }

    fun getLaunchableInstalledApps(
        context: Context,
        blockedPackages: Set<String>
    ): List<InstalledAppInfo> {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        return try {
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
            resolveInfos
                .mapNotNull { info ->
                    val pkg = info.activityInfo?.packageName ?: return@mapNotNull null
                    val label = info.loadLabel(pm)?.toString()?.takeIf { it.isNotBlank() } ?: pkg
                    InstalledAppInfo(
                        packageName = pkg,
                        appName = label,
                        isBlocked = blockedPackages.contains(pkg)
                    )
                }
                .distinctBy { it.packageName }
                .sortedBy { it.appName.lowercase() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Generates a safe, privacy-preserving diagnostic summary without any notification content.
     */
    fun buildSafeDiagnosticReport(
        context: Context,
        diagnostics: ServiceDiagnosticsState,
        styleId: String,
        languageCode: String
    ): String {
        return buildString {
            appendLine("=== Nexa Island Safe Diagnostic Report ===")
            appendLine("Package: ${context.packageName}")
            appendLine("Device: ${diagnostics.deviceSummary}")
            appendLine("HyperOS/Xiaomi Detected: ${diagnostics.isXiaomiOrHyperOS}")
            appendLine("Language: $languageCode")
            appendLine("Selected Style: $styleId")
            appendLine("--- Real Service & Permission Status ---")
            appendLine("1. User Master Switch Enabled: ${diagnostics.userPreferenceEnabled}")
            appendLine("2. Overlay Permission Granted: ${diagnostics.overlayPermissionGranted}")
            appendLine("3. Notification Listener Granted: ${diagnostics.notificationListenerGranted}")
            appendLine("4. Notification Listener Connected: ${diagnostics.notificationListenerConnected}")
            appendLine("5. Overlay Service Running: ${diagnostics.overlayServiceRunning}")
            appendLine("6. Island Currently Visible: ${diagnostics.islandCurrentlyVisible}")
            appendLine("7. Battery Optimization Ignored: ${diagnostics.batteryOptimizationIgnored}")
            appendLine("Privacy Guarantee: No notification titles, messages, or personal data included.")
        }
    }
}
