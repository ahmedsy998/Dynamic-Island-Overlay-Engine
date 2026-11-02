package com.example.model

import android.app.PendingIntent
import android.graphics.drawable.Drawable

enum class NotificationStylePreset(val id: String) {
    MINIMAL_BLACK_PILL("minimal_black_pill"),
    PREMIUM_GLASS_PILL("premium_glass_pill"),
    SOFT_GRADIENT_PILL("soft_gradient_pill"),
    ROUNDED_NOTIFICATION_CARD("rounded_notification_card"),
    COMPACT_ICON_ONLY("compact_icon_only"),
    EXPANDED_NOTIFICATION_CARD("expanded_notification_card"),
    SAMSUNG_ONE_UI_9("samsung_one_ui_9");

    companion object {
        fun fromId(id: String?): NotificationStylePreset =
            entries.firstOrNull { it.id == id } ?: SAMSUNG_ONE_UI_9
    }
}

enum class ColorPalettePreset(
    val id: String,
    val accentHex: Long,
    val islandBgHex: Long,
    val notifBgHex: Long,
    val primaryTextHex: Long,
    val secondaryTextHex: Long,
    val borderHex: Long
) {
    GRAPHITE_SILVER(
        id = "graphite_silver",
        accentHex = 0xFFE2E8F0,
        islandBgHex = 0xFF0A0D12,
        notifBgHex = 0xFF141923,
        primaryTextHex = 0xFFF8FAFC,
        secondaryTextHex = 0xFF94A3B8,
        borderHex = 0xFF334155
    ),
    SOFT_BLUE(
        id = "soft_blue",
        accentHex = 0xFF38BDF8,
        islandBgHex = 0xFF080C16,
        notifBgHex = 0xFF111827,
        primaryTextHex = 0xFFF1F5F9,
        secondaryTextHex = 0xFF93C5FD,
        borderHex = 0xFF1E3A8A
    ),
    TEAL(
        id = "teal",
        accentHex = 0xFF2DD4BF,
        islandBgHex = 0xFF061214,
        notifBgHex = 0xFF0D2124,
        primaryTextHex = 0xFFF0FDFA,
        secondaryTextHex = 0xFF99F6E4,
        borderHex = 0xFF115E59
    ),
    MUTED_VIOLET(
        id = "muted_violet",
        accentHex = 0xFFA78BFA,
        islandBgHex = 0xFF0D0A1A,
        notifBgHex = 0xFF1A142E,
        primaryTextHex = 0xFFF5F3FF,
        secondaryTextHex = 0xFFC4B5FD,
        borderHex = 0xFF4C1D95
    ),
    EMERALD(
        id = "emerald",
        accentHex = 0xFF34D399,
        islandBgHex = 0xFF06120E,
        notifBgHex = 0xFF0F241D,
        primaryTextHex = 0xFFECFDF5,
        secondaryTextHex = 0xFFA7F3D0,
        borderHex = 0xFF065F46
    ),
    WARM_NEUTRAL(
        id = "warm_neutral",
        accentHex = 0xFFFBBF24,
        islandBgHex = 0xFF12100E,
        notifBgHex = 0xFF211D1A,
        primaryTextHex = 0xFFFAFAF9,
        secondaryTextHex = 0xFFD6D3D1,
        borderHex = 0xFF57534E
    ),
    CUSTOM(
        id = "custom",
        accentHex = 0xFF38BDF8,
        islandBgHex = 0xFF080C16,
        notifBgHex = 0xFF111827,
        primaryTextHex = 0xFFF8FAFC,
        secondaryTextHex = 0xFF94A3B8,
        borderHex = 0xFF334155
    );

    companion object {
        fun fromId(id: String?): ColorPalettePreset =
            entries.firstOrNull { it.id == id } ?: SOFT_BLUE
    }
}

enum class AppThemeMode(val id: String) {
    SYSTEM("system"),
    DARK("dark"),
    LIGHT("light");

    companion object {
        fun fromId(id: String?): AppThemeMode =
            entries.firstOrNull { it.id == id } ?: DARK
    }
}

enum class IslandEventType {
    IDLE,
    NOTIFICATION,
    INCOMING_CALL,
    ONGOING_CALL,
    CHARGING_BATTERY,
    MUSIC_PLAYBACK,
    TIMER_COUNTDOWN
}

data class IslandEventItem(
    val key: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: IslandEventType = IslandEventType.NOTIFICATION,
    val isSensitive: Boolean = false,
    val isOngoing: Boolean = false,
    val groupCount: Int = 1,
    val batteryLevel: Int = 100,
    val isCharging: Boolean = false,
    val timerRemainingText: String = "",
    val mediaArtist: String = "",
    val contentIntent: PendingIntent? = null,
    val appIcon: Drawable? = null
)

data class IslandConfiguration(
    val masterEnabled: Boolean = false,
    val languageCode: String = "ar",
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val stylePreset: NotificationStylePreset = NotificationStylePreset.SAMSUNG_ONE_UI_9,
    val palettePreset: ColorPalettePreset = ColorPalettePreset.SOFT_BLUE,

    // Colors
    val accentColorHex: Long = ColorPalettePreset.SOFT_BLUE.accentHex,
    val islandBgColorHex: Long = ColorPalettePreset.SOFT_BLUE.islandBgHex,
    val notifBgColorHex: Long = ColorPalettePreset.SOFT_BLUE.notifBgHex,
    val primaryTextColorHex: Long = ColorPalettePreset.SOFT_BLUE.primaryTextHex,
    val secondaryTextColorHex: Long = ColorPalettePreset.SOFT_BLUE.secondaryTextHex,
    val borderColorHex: Long = ColorPalettePreset.SOFT_BLUE.borderHex,

    // Geometry & Dimensions
    val compactWidthDp: Int = 156,
    val compactHeightDp: Int = 36,
    val expandedWidthDp: Int = 352,
    val expandedHeightDp: Int = 128,
    val offsetXDp: Int = 0,
    val offsetYDp: Int = 6,
    val cornerRadiusDp: Int = 28,
    val outerMarginDp: Int = 12,
    val iconSizeDp: Int = 24,
    val textScalePercent: Int = 100,
    val borderWidthDp: Float = 1.2f,
    val shadowElevationDp: Int = 10,
    val opacityPercent: Int = 96,
    val animationDurationMs: Int = 280,
    val displayDurationSec: Int = 6,

    // Element Visibility & Behavior
    val showAppIcon: Boolean = true,
    val showAppName: Boolean = true,
    val showTitle: Boolean = true,
    val showMessagePreview: Boolean = true,
    val showTimestamp: Boolean = true,
    val groupSameApp: Boolean = true,
    val autoExpandNotifications: Boolean = false,
    val keepIdlePillVisible: Boolean = true,
    val restoreOnBoot: Boolean = true,
    val hapticFeedback: Boolean = true,
    val swipeToDismiss: Boolean = true,
    val showOnLockScreen: Boolean = true,
    val hideSensitiveOnLockScreen: Boolean = true,
    val alwaysHideSensitiveContent: Boolean = false,
    val blockedPackages: Set<String> = emptySet()
)

data class ServiceDiagnosticsState(
    val userPreferenceEnabled: Boolean = false,
    val overlayPermissionGranted: Boolean = false,
    val notificationListenerGranted: Boolean = false,
    val postNotificationsGranted: Boolean = true,
    val notificationListenerConnected: Boolean = false,
    val overlayServiceRunning: Boolean = false,
    val islandCurrentlyVisible: Boolean = false,
    val batteryOptimizationIgnored: Boolean = false,
    val isXiaomiOrHyperOS: Boolean = false,
    val deviceSummary: String = ""
) {
    val allRequiredPermissionsGranted: Boolean
        get() = overlayPermissionGranted && notificationListenerGranted
}

data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val isBlocked: Boolean
)
