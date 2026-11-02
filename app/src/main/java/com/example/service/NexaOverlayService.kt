package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.R
import com.example.data.IslandPreferencesRepository
import com.example.data.IslandStateController
import com.example.data.nexaDataStore
import com.example.model.IslandEventItem
import com.example.model.IslandEventType
import com.example.ui.components.DynamicIslandCapsule
import com.example.ui.theme.NexaIslandTheme
import com.example.util.SystemPermissionHelper
import com.example.util.TextDirectionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Foreground Service managing the real floating Dynamic Island overlay window via WindowManager.
 * Implements LifecycleOwner, ViewModelStoreOwner, and SavedStateRegistryOwner to host Jetpack Compose
 * smoothly and battery-efficiently.
 */
class NexaOverlayService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var preferencesRepository: IslandPreferencesRepository
    private var windowManager: WindowManager? = null
    private var composeOverlayView: ComposeView? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var lastChargingState: Boolean? = null

    private val systemEventsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action ?: return
            when (action) {
                Intent.ACTION_SCREEN_OFF -> {
                    IslandStateController.setDeviceLocked(true)
                    if (lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                        lifecycleRegistry.currentState = Lifecycle.State.CREATED
                    }
                }

                Intent.ACTION_SCREEN_ON -> {
                    val locked = SystemPermissionHelper.isDeviceLocked(applicationContext)
                    IslandStateController.setDeviceLocked(locked)
                    if (lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.CREATED)) {
                        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
                    }
                }

                Intent.ACTION_USER_PRESENT -> {
                    IslandStateController.setDeviceLocked(false)
                    if (lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.CREATED)) {
                        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
                    }
                }

                Intent.ACTION_POWER_CONNECTED, Intent.ACTION_BATTERY_CHANGED -> {
                    handleBatteryEvent(intent)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED

        preferencesRepository = IslandPreferencesRepository(applicationContext.nexaDataStore)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager

        IslandStateController.setDeviceLocked(SystemPermissionHelper.isDeviceLocked(this))
        registerSystemReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_OVERLAY) {
            stopOverlayAndSelf()
            return START_NOT_STICKY
        }

        if (!SystemPermissionHelper.hasOverlayPermission(this)) {
            stopOverlayAndSelf()
            return START_NOT_STICKY
        }

        startForegroundSafely()
        IslandStateController.setOverlayServiceRunning(true)
        ensureOverlayWindowAttached()

        serviceScope.launch {
            preferencesRepository.configurationFlow.collectLatest { config ->
                if (!config.masterEnabled || !SystemPermissionHelper.hasOverlayPermission(this@NexaOverlayService)) {
                    stopOverlayAndSelf()
                }
            }
        }

        return START_STICKY
    }

    private fun startForegroundSafely() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        if (nm != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                FGS_CHANNEL_ID,
                getString(R.string.fgs_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setShowBadge(false)
            }
            nm.createNotificationChannel(channel)
        }

        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, FGS_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(getString(R.string.fgs_notif_title))
            .setContentText(getString(R.string.fgs_notif_body))
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent)
            .build()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    FGS_NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(FGS_NOTIFICATION_ID, notification)
            }
        } catch (_: Exception) {
            // Continue if OS restricts foreground promotion in specific states
        }
    }

    @Suppress("DEPRECATION")
    private fun ensureOverlayWindowAttached() {
        if (composeOverlayView != null) return
        val wm = windowManager ?: return

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val windowFlags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            windowFlags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            }
        }
        layoutParams = params

        val view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@NexaOverlayService)
            setViewTreeViewModelStoreOwner(this@NexaOverlayService)
            setViewTreeSavedStateRegistryOwner(this@NexaOverlayService)

            setContent {
                val config by preferencesRepository.configurationFlow.collectAsState(
                    initial = com.example.model.IslandConfiguration(masterEnabled = true)
                )
                val queue by IslandStateController.eventQueue.collectAsState()
                val currentIdx by IslandStateController.selectedQueueIndex.collectAsState()
                val isExpanded by IslandStateController.isExpanded.collectAsState()
                val isLocked by IslandStateController.isDeviceLocked.collectAsState()

                val activeEvent = queue.getOrNull(currentIdx.coerceIn(0, (queue.size - 1).coerceAtLeast(0)))

                // Auto-collapse or auto-dismiss transient notifications after configured duration
                LaunchedEffect(activeEvent?.key, activeEvent?.timestamp, isExpanded, config.displayDurationSec) {
                    val event = activeEvent ?: return@LaunchedEffect
                    if (!event.isOngoing) {
                        delay(config.displayDurationSec * 1000L)
                        if (isExpanded) {
                            IslandStateController.setExpanded(false)
                            delay(2500L)
                        }
                        IslandStateController.removeEventByKey(event.key)
                    }
                }

                val shouldShowIsland = config.masterEnabled &&
                    (!isLocked || config.showOnLockScreen) &&
                    (activeEvent != null || config.keepIdlePillVisible)

                LaunchedEffect(shouldShowIsland) {
                    IslandStateController.setIslandWindowVisible(shouldShowIsland)
                    this@apply.visibility = if (shouldShowIsland) View.VISIBLE else View.GONE
                }

                if (shouldShowIsland) {
                    val baseCtx = LocalContext.current
                    val localizedCtx = remember(baseCtx, config.languageCode) {
                        TextDirectionHelper.createLocalizedContext(baseCtx, config.languageCode)
                    }
                    val layoutDir = if (config.languageCode == "en") LayoutDirection.Ltr else LayoutDirection.Rtl

                    CompositionLocalProvider(
                        LocalContext provides localizedCtx,
                        LocalLayoutDirection provides layoutDir
                    ) {
                        NexaIslandTheme(themeMode = config.themeMode) {
                            DynamicIslandCapsule(
                                config = config,
                                activeEvent = activeEvent,
                                queueSize = queue.size,
                                currentQueueIndex = currentIdx,
                                isExpanded = isExpanded,
                                isDeviceLocked = isLocked,
                                onToggleExpand = {
                                    if (activeEvent != null) {
                                        IslandStateController.toggleExpanded()
                                    }
                                },
                                onOpenNotificationIntent = { item ->
                                    try {
                                        item.contentIntent?.send()
                                        IslandStateController.setExpanded(false)
                                    } catch (_: Exception) {
                                    }
                                },
                                onDismissEvent = {
                                    IslandStateController.dismissCurrentEvent(notifySystemListener = true)
                                },
                                onNextEvent = { IslandStateController.selectNextEvent() },
                                onPrevEvent = { IslandStateController.selectPreviousEvent() },
                                isPreviewMode = false
                            )
                        }
                    }
                }
            }
        }

        try {
            wm.addView(view, params)
            composeOverlayView = view
            lifecycleRegistry.currentState = Lifecycle.State.RESUMED
            IslandStateController.setIslandWindowVisible(true)
        } catch (_: Exception) {
            IslandStateController.setIslandWindowVisible(false)
        }
    }

    private fun handleBatteryEvent(intent: Intent) {
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val pct = if (level >= 0 && scale > 0) (level * 100) / scale else 100

        // Only emit charging pill when charging state transitions to true or on explicit power connect
        if (intent.action == Intent.ACTION_POWER_CONNECTED ||
            (isCharging && lastChargingState == false)
        ) {
            lastChargingState = isCharging
            serviceScope.launch {
                val config = preferencesRepository.getSnapshot()
                if (!config.masterEnabled) return@launch
                val localized = TextDirectionHelper.createLocalizedContext(applicationContext, config.languageCode)
                IslandStateController.postOrUpdateEvent(
                    item = IslandEventItem(
                        key = "system_battery_charging",
                        packageName = packageName,
                        appName = localized.getString(R.string.app_name),
                        title = localized.getString(R.string.island_charging_label),
                        message = "${localized.getString(R.string.island_battery_label)} $pct%",
                        eventType = IslandEventType.CHARGING_BATTERY,
                        batteryLevel = pct,
                        isCharging = true
                    ),
                    groupSameApp = false,
                    autoExpand = false
                )
            }
        } else {
            lastChargingState = isCharging
        }
    }

    private fun registerSystemReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(systemEventsReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(systemEventsReceiver, filter)
        }
    }

    private fun removeOverlayWindowSafely() {
        val view = composeOverlayView ?: return
        try {
            windowManager?.removeViewImmediate(view)
        } catch (_: Exception) {
        }
        composeOverlayView = null
        IslandStateController.setIslandWindowVisible(false)
    }

    private fun stopOverlayAndSelf() {
        removeOverlayWindowSafely()
        IslandStateController.setOverlayServiceRunning(false)
        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (_: Exception) {
        }
        stopSelf()
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(systemEventsReceiver)
        } catch (_: Exception) {
        }
        removeOverlayWindowSafely()
        IslandStateController.setOverlayServiceRunning(false)
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        store.clear()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val FGS_CHANNEL_ID = "nexa_island_overlay_fgs"
        const val FGS_NOTIFICATION_ID = 7101
        private const val ACTION_STOP_OVERLAY = "com.nexaisland.dynamicisland.STOP_OVERLAY"

        fun start(context: Context) {
            if (!SystemPermissionHelper.hasOverlayPermission(context)) return
            val intent = Intent(context, NexaOverlayService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (_: Exception) {
            }
        }

        fun stop(context: Context) {
            try {
                val stopIntent = Intent(context, NexaOverlayService::class.java).apply {
                    action = ACTION_STOP_OVERLAY
                }
                context.stopService(stopIntent)
            } catch (_: Exception) {
            }
            IslandStateController.setOverlayServiceRunning(false)
            IslandStateController.setIslandWindowVisible(false)
        }
    }
}
