package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.IslandPreferencesRepository
import com.example.data.IslandStateController
import com.example.model.AppThemeMode
import com.example.model.ColorPalettePreset
import com.example.model.InstalledAppInfo
import com.example.model.IslandConfiguration
import com.example.model.IslandEventItem
import com.example.model.IslandEventType
import com.example.model.NotificationStylePreset
import com.example.model.ServiceDiagnosticsState
import com.example.util.SystemPermissionHelper
import com.example.util.TextDirectionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NexaIslandViewModel(
    private val repository: IslandPreferencesRepository
) : ViewModel() {

    val configuration: StateFlow<IslandConfiguration> = repository.configurationFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = IslandConfiguration()
        )

    val eventQueue: StateFlow<List<IslandEventItem>> = IslandStateController.eventQueue
    val selectedQueueIndex: StateFlow<Int> = IslandStateController.selectedQueueIndex
    val isExpanded: StateFlow<Boolean> = IslandStateController.isExpanded
    val isDeviceLocked: StateFlow<Boolean> = IslandStateController.isDeviceLocked

    private val _baseDiagnostics = MutableStateFlow(ServiceDiagnosticsState())

    val diagnostics: StateFlow<ServiceDiagnosticsState> = combine(
        _baseDiagnostics,
        configuration,
        IslandStateController.isNotificationListenerConnected,
        IslandStateController.isOverlayServiceRunning,
        IslandStateController.isIslandWindowVisible
    ) { base, config, listenerConnected, overlayRunning, islandVisible ->
        base.copy(
            userPreferenceEnabled = config.masterEnabled,
            notificationListenerConnected = listenerConnected,
            overlayServiceRunning = overlayRunning,
            islandCurrentlyVisible = islandVisible
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ServiceDiagnosticsState()
    )

    private val _installedApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppInfo>> = _installedApps.asStateFlow()

    fun refreshDiagnosticsAndApps(context: Context) {
        viewModelScope.launch {
            val config = configuration.value
            _baseDiagnostics.value = SystemPermissionHelper.inspectDiagnostics(
                context = context,
                userPreferenceEnabled = config.masterEnabled
            )
            SystemPermissionHelper.syncOverlayServiceState(
                context = context,
                masterEnabled = config.masterEnabled
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            val blocked = configuration.value.blockedPackages
            _installedApps.value = SystemPermissionHelper.getLaunchableInstalledApps(context, blocked)
        }
    }

    fun setMasterEnabled(context: Context, enabled: Boolean) {
        viewModelScope.launch {
            repository.setMasterEnabled(enabled)
            SystemPermissionHelper.syncOverlayServiceState(context, enabled)
            _baseDiagnostics.value = SystemPermissionHelper.inspectDiagnostics(context, enabled)
        }
    }

    fun setLanguageCode(lang: String) {
        viewModelScope.launch {
            repository.setLanguageCode(lang)
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    fun setStylePreset(style: NotificationStylePreset) {
        viewModelScope.launch {
            repository.setStylePreset(style)
        }
    }

    fun applyPalettePreset(palette: ColorPalettePreset) {
        viewModelScope.launch {
            repository.applyPalettePreset(palette)
        }
    }

    fun updateCustomColor(
        accent: Long? = null,
        islandBg: Long? = null,
        notifBg: Long? = null,
        primaryText: Long? = null,
        secondaryText: Long? = null,
        border: Long? = null
    ) {
        viewModelScope.launch {
            repository.updateCustomColor(
                accent = accent,
                islandBg = islandBg,
                notifBg = notifBg,
                primaryText = primaryText,
                secondaryText = secondaryText,
                border = border
            )
        }
    }

    fun updateGeometry(
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
        viewModelScope.launch {
            repository.updateGeometry(
                compactWidthDp = compactWidthDp,
                compactHeightDp = compactHeightDp,
                expandedWidthDp = expandedWidthDp,
                expandedHeightDp = expandedHeightDp,
                offsetXDp = offsetXDp,
                offsetYDp = offsetYDp,
                cornerRadiusDp = cornerRadiusDp,
                outerMarginDp = outerMarginDp,
                iconSizeDp = iconSizeDp,
                textScalePercent = textScalePercent,
                borderWidthDp = borderWidthDp,
                shadowElevationDp = shadowElevationDp,
                opacityPercent = opacityPercent,
                animationDurationMs = animationDurationMs,
                displayDurationSec = displayDurationSec
            )
        }
    }

    fun updateVisibilityAndBehavior(
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
        viewModelScope.launch {
            repository.updateVisibilityAndBehavior(
                showAppIcon = showAppIcon,
                showAppName = showAppName,
                showTitle = showTitle,
                showMessagePreview = showMessagePreview,
                showTimestamp = showTimestamp,
                groupSameApp = groupSameApp,
                autoExpandNotifications = autoExpandNotifications,
                keepIdlePillVisible = keepIdlePillVisible,
                restoreOnBoot = restoreOnBoot,
                hapticFeedback = hapticFeedback,
                swipeToDismiss = swipeToDismiss,
                showOnLockScreen = showOnLockScreen,
                hideSensitiveOnLockScreen = hideSensitiveOnLockScreen,
                alwaysHideSensitiveContent = alwaysHideSensitiveContent
            )
        }
    }

    fun togglePackageBlocked(context: Context, packageName: String, blocked: Boolean) {
        viewModelScope.launch {
            repository.togglePackageBlocked(packageName, blocked)
            val updatedSet = repository.getSnapshot().blockedPackages
            _installedApps.value = SystemPermissionHelper.getLaunchableInstalledApps(context, updatedSet)
        }
    }

    fun allowAllPackages(context: Context) {
        viewModelScope.launch {
            repository.clearAllBlockedPackages()
            _installedApps.value = SystemPermissionHelper.getLaunchableInstalledApps(context, emptySet())
        }
    }

    fun resetGeometryToDefaults() {
        viewModelScope.launch {
            repository.resetGeometryToDefaults()
        }
    }

    fun resetAllToDefaults() {
        viewModelScope.launch {
            repository.resetAllToDefaults()
        }
    }

    fun sendRealTestNotification(context: Context) {
        val config = configuration.value
        val localized = TextDirectionHelper.createLocalizedContext(context, config.languageCode)
        SystemPermissionHelper.triggerRealTestNotification(
            context = localized,
            groupSameApp = config.groupSameApp,
            autoExpand = config.autoExpandNotifications
        )
    }

    fun triggerStateDemo(context: Context, eventType: IslandEventType) {
        val config = configuration.value
        val localized = TextDirectionHelper.createLocalizedContext(context, config.languageCode)
        val isAr = config.languageCode != "en"

        val item = when (eventType) {
            IslandEventType.INCOMING_CALL -> IslandEventItem(
                key = "state_incoming_call",
                packageName = "com.android.dialer",
                appName = localized.getString(R.string.island_incoming_call),
                title = if (isAr) "أحمد سلام • مكالمة واردة" else "Ahmed Salam • Incoming Call",
                message = if (isAr) "جاري الاتصال عبر الهاتف المحمول..." else "Mobile Voice Call...",
                eventType = IslandEventType.INCOMING_CALL
            )

            IslandEventType.MUSIC_PLAYBACK -> IslandEventItem(
                key = "state_music_playback",
                packageName = "com.nexaisland.music",
                appName = localized.getString(R.string.island_music_playing),
                title = if (isAr) "أصداء الصحراء • موسيقى هادئة" else "Midnight Horizon • Ambient Mix",
                message = if (isAr) "جلسة وسائط نشطة • جودة عالية" else "Active Media Session • Lossless",
                eventType = IslandEventType.MUSIC_PLAYBACK
            )

            IslandEventType.CHARGING_BATTERY -> IslandEventItem(
                key = "state_charging_battery",
                packageName = context.packageName,
                appName = localized.getString(R.string.island_charging_label),
                title = if (isAr) "الشحن السريع نشط • 88%" else "Turbo Charging • 88%",
                message = if (isAr) "متصل بالشاحن السريع" else "Connected to fast power adapter",
                eventType = IslandEventType.CHARGING_BATTERY,
                batteryLevel = 88,
                isCharging = true
            )

            IslandEventType.TIMER_COUNTDOWN -> IslandEventItem(
                key = "state_timer_countdown",
                packageName = "com.android.deskclock",
                appName = localized.getString(R.string.island_timer_active),
                title = if (isAr) "مؤقت التركيز النشط" else "Focus Session Timer",
                message = if (isAr) "الوقت المتبقي 04:12" else "Remaining 04:12",
                eventType = IslandEventType.TIMER_COUNTDOWN,
                timerRemainingText = "04:12"
            )

            else -> return
        }

        IslandStateController.postOrUpdateEvent(
            item = item,
            groupSameApp = false,
            autoExpand = eventType == IslandEventType.INCOMING_CALL
        )
    }

    fun clearQueue() {
        IslandStateController.clearAllEvents()
    }

    class Factory(private val repository: IslandPreferencesRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NexaIslandViewModel(repository) as T
        }
    }
}
