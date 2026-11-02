package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AppThemeMode
import com.example.model.ColorPalettePreset
import com.example.model.IslandConfiguration
import com.example.model.NotificationStylePreset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.nexaDataStore: DataStore<Preferences> by preferencesDataStore(name = "nexa_island_settings")

class IslandPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    private object Keys {
        val MASTER_ENABLED = booleanPreferencesKey("master_enabled")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val STYLE_PRESET = stringPreferencesKey("style_preset")
        val PALETTE_PRESET = stringPreferencesKey("palette_preset")

        val ACCENT_COLOR = longPreferencesKey("accent_color")
        val ISLAND_BG_COLOR = longPreferencesKey("island_bg_color")
        val NOTIF_BG_COLOR = longPreferencesKey("notif_bg_color")
        val PRIMARY_TEXT_COLOR = longPreferencesKey("primary_text_color")
        val SECONDARY_TEXT_COLOR = longPreferencesKey("secondary_text_color")
        val BORDER_COLOR = longPreferencesKey("border_color")

        val COMPACT_WIDTH = intPreferencesKey("compact_width_dp")
        val COMPACT_HEIGHT = intPreferencesKey("compact_height_dp")
        val EXPANDED_WIDTH = intPreferencesKey("expanded_width_dp")
        val EXPANDED_HEIGHT = intPreferencesKey("expanded_height_dp")
        val OFFSET_X = intPreferencesKey("offset_x_dp")
        val OFFSET_Y = intPreferencesKey("offset_y_dp")
        val CORNER_RADIUS = intPreferencesKey("corner_radius_dp")
        val OUTER_MARGIN = intPreferencesKey("outer_margin_dp")
        val ICON_SIZE = intPreferencesKey("icon_size_dp")
        val TEXT_SCALE = intPreferencesKey("text_scale_percent")
        val BORDER_WIDTH = floatPreferencesKey("border_width_dp")
        val SHADOW_ELEVATION = intPreferencesKey("shadow_elevation_dp")
        val OPACITY_PERCENT = intPreferencesKey("opacity_percent")
        val ANIM_DURATION_MS = intPreferencesKey("anim_duration_ms")
        val DISPLAY_DURATION_SEC = intPreferencesKey("display_duration_sec")

        val SHOW_APP_ICON = booleanPreferencesKey("show_app_icon")
        val SHOW_APP_NAME = booleanPreferencesKey("show_app_name")
        val SHOW_TITLE = booleanPreferencesKey("show_title")
        val SHOW_MESSAGE = booleanPreferencesKey("show_message")
        val SHOW_TIMESTAMP = booleanPreferencesKey("show_timestamp")
        val GROUP_SAME_APP = booleanPreferencesKey("group_same_app")
        val AUTO_EXPAND = booleanPreferencesKey("auto_expand")
        val KEEP_IDLE_PILL = booleanPreferencesKey("keep_idle_pill")
        val RESTORE_ON_BOOT = booleanPreferencesKey("restore_on_boot")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val SWIPE_TO_DISMISS = booleanPreferencesKey("swipe_to_dismiss")
        val SHOW_ON_LOCKSCREEN = booleanPreferencesKey("show_on_lockscreen")
        val HIDE_SENSITIVE_LOCKSCREEN = booleanPreferencesKey("hide_sensitive_lockscreen")
        val ALWAYS_HIDE_SENSITIVE = booleanPreferencesKey("always_hide_sensitive")
        val BLOCKED_PACKAGES = stringSetPreferencesKey("blocked_packages")
    }

    val configurationFlow: Flow<IslandConfiguration> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            val defaults = IslandConfiguration()
            val palette = ColorPalettePreset.fromId(prefs[Keys.PALETTE_PRESET])
            IslandConfiguration(
                masterEnabled = prefs[Keys.MASTER_ENABLED] ?: defaults.masterEnabled,
                languageCode = prefs[Keys.LANGUAGE_CODE] ?: "ar", // Mandatory Arabic default
                themeMode = AppThemeMode.fromId(prefs[Keys.THEME_MODE]),
                stylePreset = NotificationStylePreset.fromId(prefs[Keys.STYLE_PRESET]),
                palettePreset = palette,
                accentColorHex = prefs[Keys.ACCENT_COLOR] ?: palette.accentHex,
                islandBgColorHex = prefs[Keys.ISLAND_BG_COLOR] ?: palette.islandBgHex,
                notifBgColorHex = prefs[Keys.NOTIF_BG_COLOR] ?: palette.notifBgHex,
                primaryTextColorHex = prefs[Keys.PRIMARY_TEXT_COLOR] ?: palette.primaryTextHex,
                secondaryTextColorHex = prefs[Keys.SECONDARY_TEXT_COLOR] ?: palette.secondaryTextHex,
                borderColorHex = prefs[Keys.BORDER_COLOR] ?: palette.borderHex,
                compactWidthDp = (prefs[Keys.COMPACT_WIDTH] ?: defaults.compactWidthDp).coerceIn(96, 260),
                compactHeightDp = (prefs[Keys.COMPACT_HEIGHT] ?: defaults.compactHeightDp).coerceIn(28, 64),
                expandedWidthDp = (prefs[Keys.EXPANDED_WIDTH] ?: defaults.expandedWidthDp).coerceIn(260, 420),
                expandedHeightDp = (prefs[Keys.EXPANDED_HEIGHT] ?: defaults.expandedHeightDp).coerceIn(88, 220),
                offsetXDp = (prefs[Keys.OFFSET_X] ?: defaults.offsetXDp).coerceIn(-120, 120),
                offsetYDp = (prefs[Keys.OFFSET_Y] ?: defaults.offsetYDp).coerceIn(0, 80),
                cornerRadiusDp = (prefs[Keys.CORNER_RADIUS] ?: defaults.cornerRadiusDp).coerceIn(12, 48),
                outerMarginDp = (prefs[Keys.OUTER_MARGIN] ?: defaults.outerMarginDp).coerceIn(0, 32),
                iconSizeDp = (prefs[Keys.ICON_SIZE] ?: defaults.iconSizeDp).coerceIn(16, 40),
                textScalePercent = (prefs[Keys.TEXT_SCALE] ?: defaults.textScalePercent).coerceIn(80, 130),
                borderWidthDp = (prefs[Keys.BORDER_WIDTH] ?: defaults.borderWidthDp).coerceIn(0f, 4f),
                shadowElevationDp = (prefs[Keys.SHADOW_ELEVATION] ?: defaults.shadowElevationDp).coerceIn(0, 24),
                opacityPercent = (prefs[Keys.OPACITY_PERCENT] ?: defaults.opacityPercent).coerceIn(70, 100),
                animationDurationMs = (prefs[Keys.ANIM_DURATION_MS] ?: defaults.animationDurationMs).coerceIn(120, 600),
                displayDurationSec = (prefs[Keys.DISPLAY_DURATION_SEC] ?: defaults.displayDurationSec).coerceIn(3, 15),
                showAppIcon = prefs[Keys.SHOW_APP_ICON] ?: defaults.showAppIcon,
                showAppName = prefs[Keys.SHOW_APP_NAME] ?: defaults.showAppName,
                showTitle = prefs[Keys.SHOW_TITLE] ?: defaults.showTitle,
                showMessagePreview = prefs[Keys.SHOW_MESSAGE] ?: defaults.showMessagePreview,
                showTimestamp = prefs[Keys.SHOW_TIMESTAMP] ?: defaults.showTimestamp,
                groupSameApp = prefs[Keys.GROUP_SAME_APP] ?: defaults.groupSameApp,
                autoExpandNotifications = prefs[Keys.AUTO_EXPAND] ?: defaults.autoExpandNotifications,
                keepIdlePillVisible = prefs[Keys.KEEP_IDLE_PILL] ?: defaults.keepIdlePillVisible,
                restoreOnBoot = prefs[Keys.RESTORE_ON_BOOT] ?: defaults.restoreOnBoot,
                hapticFeedback = prefs[Keys.HAPTIC_FEEDBACK] ?: defaults.hapticFeedback,
                swipeToDismiss = prefs[Keys.SWIPE_TO_DISMISS] ?: defaults.swipeToDismiss,
                showOnLockScreen = prefs[Keys.SHOW_ON_LOCKSCREEN] ?: defaults.showOnLockScreen,
                hideSensitiveOnLockScreen = prefs[Keys.HIDE_SENSITIVE_LOCKSCREEN] ?: defaults.hideSensitiveOnLockScreen,
                alwaysHideSensitiveContent = prefs[Keys.ALWAYS_HIDE_SENSITIVE] ?: defaults.alwaysHideSensitiveContent,
                blockedPackages = prefs[Keys.BLOCKED_PACKAGES] ?: defaults.blockedPackages
            )
        }

    suspend fun getSnapshot(): IslandConfiguration = configurationFlow.first()

    suspend fun setMasterEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.MASTER_ENABLED] = enabled }
    }

    suspend fun setLanguageCode(lang: String) {
        val normalized = if (lang == "en") "en" else "ar"
        dataStore.edit { it[Keys.LANGUAGE_CODE] = normalized }
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.id }
    }

    suspend fun setStylePreset(style: NotificationStylePreset) {
        dataStore.edit { it[Keys.STYLE_PRESET] = style.id }
    }

    suspend fun applyPalettePreset(palette: ColorPalettePreset) {
        dataStore.edit { prefs ->
            prefs[Keys.PALETTE_PRESET] = palette.id
            prefs[Keys.ACCENT_COLOR] = palette.accentHex
            prefs[Keys.ISLAND_BG_COLOR] = palette.islandBgHex
            prefs[Keys.NOTIF_BG_COLOR] = palette.notifBgHex
            prefs[Keys.PRIMARY_TEXT_COLOR] = palette.primaryTextHex
            prefs[Keys.SECONDARY_TEXT_COLOR] = palette.secondaryTextHex
            prefs[Keys.BORDER_COLOR] = palette.borderHex
        }
    }

    suspend fun updateCustomColor(
        accent: Long? = null,
        islandBg: Long? = null,
        notifBg: Long? = null,
        primaryText: Long? = null,
        secondaryText: Long? = null,
        border: Long? = null
    ) {
        dataStore.edit { prefs ->
            prefs[Keys.PALETTE_PRESET] = ColorPalettePreset.CUSTOM.id
            accent?.let { prefs[Keys.ACCENT_COLOR] = it }
            islandBg?.let { prefs[Keys.ISLAND_BG_COLOR] = it }
            notifBg?.let { prefs[Keys.NOTIF_BG_COLOR] = it }
            primaryText?.let { prefs[Keys.PRIMARY_TEXT_COLOR] = it }
            secondaryText?.let { prefs[Keys.SECONDARY_TEXT_COLOR] = it }
            border?.let { prefs[Keys.BORDER_COLOR] = it }
        }
    }

    suspend fun updateGeometry(
        compactWidthDp: Int? = null,
        compactHeightDp: Int? = null,
        expandedWidthDp: Int? = null,
        expandedHeightDp: Int? = null,
        offsetXDp: Int? = null,
        offsetYDp: Int? = null,
        cornerRadiusDp: Int? = null,
        outerMarginDp: Int? = null,
        iconSizeDp: Int? = null,
        textScalePercent: Int? = null,
        borderWidthDp: Float? = null,
        shadowElevationDp: Int? = null,
        opacityPercent: Int? = null,
        animationDurationMs: Int? = null,
        displayDurationSec: Int? = null
    ) {
        dataStore.edit { prefs ->
            compactWidthDp?.let { prefs[Keys.COMPACT_WIDTH] = it.coerceIn(96, 260) }
            compactHeightDp?.let { prefs[Keys.COMPACT_HEIGHT] = it.coerceIn(28, 64) }
            expandedWidthDp?.let { prefs[Keys.EXPANDED_WIDTH] = it.coerceIn(260, 420) }
            expandedHeightDp?.let { prefs[Keys.EXPANDED_HEIGHT] = it.coerceIn(88, 220) }
            offsetXDp?.let { prefs[Keys.OFFSET_X] = it.coerceIn(-120, 120) }
            offsetYDp?.let { prefs[Keys.OFFSET_Y] = it.coerceIn(0, 80) }
            cornerRadiusDp?.let { prefs[Keys.CORNER_RADIUS] = it.coerceIn(12, 48) }
            outerMarginDp?.let { prefs[Keys.OUTER_MARGIN] = it.coerceIn(0, 32) }
            iconSizeDp?.let { prefs[Keys.ICON_SIZE] = it.coerceIn(16, 40) }
            textScalePercent?.let { prefs[Keys.TEXT_SCALE] = it.coerceIn(80, 130) }
            borderWidthDp?.let { prefs[Keys.BORDER_WIDTH] = it.coerceIn(0f, 4f) }
            shadowElevationDp?.let { prefs[Keys.SHADOW_ELEVATION] = it.coerceIn(0, 24) }
            opacityPercent?.let { prefs[Keys.OPACITY_PERCENT] = it.coerceIn(70, 100) }
            animationDurationMs?.let { prefs[Keys.ANIM_DURATION_MS] = it.coerceIn(120, 600) }
            displayDurationSec?.let { prefs[Keys.DISPLAY_DURATION_SEC] = it.coerceIn(3, 15) }
        }
    }

    suspend fun updateVisibilityAndBehavior(
        showAppIcon: Boolean? = null,
        showAppName: Boolean? = null,
        showTitle: Boolean? = null,
        showMessagePreview: Boolean? = null,
        showTimestamp: Boolean? = null,
        groupSameApp: Boolean? = null,
        autoExpandNotifications: Boolean? = null,
        keepIdlePillVisible: Boolean? = null,
        restoreOnBoot: Boolean? = null,
        hapticFeedback: Boolean? = null,
        swipeToDismiss: Boolean? = null,
        showOnLockScreen: Boolean? = null,
        hideSensitiveOnLockScreen: Boolean? = null,
        alwaysHideSensitiveContent: Boolean? = null
    ) {
        dataStore.edit { prefs ->
            showAppIcon?.let { prefs[Keys.SHOW_APP_ICON] = it }
            showAppName?.let { prefs[Keys.SHOW_APP_NAME] = it }
            showTitle?.let { prefs[Keys.SHOW_TITLE] = it }
            showMessagePreview?.let { prefs[Keys.SHOW_MESSAGE] = it }
            showTimestamp?.let { prefs[Keys.SHOW_TIMESTAMP] = it }
            groupSameApp?.let { prefs[Keys.GROUP_SAME_APP] = it }
            autoExpandNotifications?.let { prefs[Keys.AUTO_EXPAND] = it }
            keepIdlePillVisible?.let { prefs[Keys.KEEP_IDLE_PILL] = it }
            restoreOnBoot?.let { prefs[Keys.RESTORE_ON_BOOT] = it }
            hapticFeedback?.let { prefs[Keys.HAPTIC_FEEDBACK] = it }
            swipeToDismiss?.let { prefs[Keys.SWIPE_TO_DISMISS] = it }
            showOnLockScreen?.let { prefs[Keys.SHOW_ON_LOCKSCREEN] = it }
            hideSensitiveOnLockScreen?.let { prefs[Keys.HIDE_SENSITIVE_LOCKSCREEN] = it }
            alwaysHideSensitiveContent?.let { prefs[Keys.ALWAYS_HIDE_SENSITIVE] = it }
        }
    }

    suspend fun togglePackageBlocked(packageName: String, blocked: Boolean) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.BLOCKED_PACKAGES] ?: emptySet()
            prefs[Keys.BLOCKED_PACKAGES] = if (blocked) {
                current + packageName
            } else {
                current - packageName
            }
        }
    }

    suspend fun clearAllBlockedPackages() {
        dataStore.edit { prefs ->
            prefs[Keys.BLOCKED_PACKAGES] = emptySet()
        }
    }

    suspend fun resetGeometryToDefaults() {
        val defaults = IslandConfiguration()
        dataStore.edit { prefs ->
            prefs[Keys.COMPACT_WIDTH] = defaults.compactWidthDp
            prefs[Keys.COMPACT_HEIGHT] = defaults.compactHeightDp
            prefs[Keys.EXPANDED_WIDTH] = defaults.expandedWidthDp
            prefs[Keys.EXPANDED_HEIGHT] = defaults.expandedHeightDp
            prefs[Keys.OFFSET_X] = defaults.offsetXDp
            prefs[Keys.OFFSET_Y] = defaults.offsetYDp
            prefs[Keys.CORNER_RADIUS] = defaults.cornerRadiusDp
            prefs[Keys.OUTER_MARGIN] = defaults.outerMarginDp
            prefs[Keys.ICON_SIZE] = defaults.iconSizeDp
            prefs[Keys.TEXT_SCALE] = defaults.textScalePercent
            prefs[Keys.BORDER_WIDTH] = defaults.borderWidthDp
            prefs[Keys.SHADOW_ELEVATION] = defaults.shadowElevationDp
            prefs[Keys.OPACITY_PERCENT] = defaults.opacityPercent
            prefs[Keys.ANIM_DURATION_MS] = defaults.animationDurationMs
            prefs[Keys.DISPLAY_DURATION_SEC] = defaults.displayDurationSec
        }
    }

    suspend fun resetAllToDefaults() {
        val currentMaster = getSnapshot().masterEnabled
        dataStore.edit { prefs ->
            prefs.clear()
            prefs[Keys.MASTER_ENABLED] = currentMaster
            prefs[Keys.LANGUAGE_CODE] = "ar"
        }
    }
}
