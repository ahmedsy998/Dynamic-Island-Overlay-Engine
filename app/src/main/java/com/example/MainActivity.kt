package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.IslandPreferencesRepository
import com.example.data.nexaDataStore
import com.example.ui.NexaIslandViewModel
import com.example.ui.screens.CompleteSettingsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GeometryPositionScreen
import com.example.ui.screens.HyperOsCompatibilityScreen
import com.example.ui.screens.StylesGalleryScreen
import com.example.ui.theme.NexaIslandTheme
import com.example.util.TextDirectionHelper

enum class NexaTab(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD("dashboard", R.string.nav_dashboard, Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    STYLES("styles", R.string.nav_styles, Icons.Filled.Palette, Icons.Outlined.Palette),
    GEOMETRY("geometry", R.string.nav_geometry, Icons.Filled.Tune, Icons.Outlined.Tune),
    HYPEROS("hyperos", R.string.nav_hyperos, Icons.Filled.Security, Icons.Outlined.Security),
    SETTINGS("settings", R.string.nav_settings, Icons.Filled.Settings, Icons.Outlined.Settings)
}

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Default to Arabic on first launch regardless of system locale
        val arabicDefaultContext = TextDirectionHelper.createLocalizedContext(newBase, "ar")
        super.attachBaseContext(arabicDefaultContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = (application as? NexaIslandApp)?.preferencesRepository
            ?: IslandPreferencesRepository(applicationContext.nexaDataStore)

        setContent {
            val viewModel: NexaIslandViewModel = viewModel(
                factory = NexaIslandViewModel.Factory(repository)
            )
            NexaIslandRootApp(viewModel = viewModel)
        }
    }
}

@Composable
fun NexaIslandRootApp(viewModel: NexaIslandViewModel) {
    val baseContext = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val config by viewModel.configuration.collectAsStateWithLifecycle()
    val diagnostics by viewModel.diagnostics.collectAsStateWithLifecycle()
    val queue by viewModel.eventQueue.collectAsStateWithLifecycle()
    val selectedIndex by viewModel.selectedQueueIndex.collectAsStateWithLifecycle()
    val isExpanded by viewModel.isExpanded.collectAsStateWithLifecycle()
    val isDeviceLocked by viewModel.isDeviceLocked.collectAsStateWithLifecycle()
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()

    // Refresh real permission and service status whenever user returns from Settings
    DisposableEffect(lifecycleOwner, baseContext) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshDiagnosticsAndApps(baseContext)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val localizedContext = remember(baseContext, config.languageCode) {
        TextDirectionHelper.createLocalizedContext(baseContext, config.languageCode)
    }
    val layoutDirection = if (config.languageCode == "en") {
        LayoutDirection.Ltr
    } else {
        LayoutDirection.Rtl
    }

    var currentTab by rememberSaveable { mutableStateOf(NexaTab.DASHBOARD) }

    BackHandler(enabled = currentTab != NexaTab.DASHBOARD) {
        currentTab = NexaTab.DASHBOARD
    }

    val activeEvent = queue.getOrNull(selectedIndex.coerceIn(0, (queue.size - 1).coerceAtLeast(0)))

    val activityResultOwner = androidx.activity.compose.LocalActivityResultRegistryOwner.current
    val backPressedOwner = androidx.activity.compose.LocalOnBackPressedDispatcherOwner.current

    val providedValues = buildList {
        add(LocalContext provides localizedContext)
        add(LocalLayoutDirection provides layoutDirection)
        if (activityResultOwner != null) {
            add(androidx.activity.compose.LocalActivityResultRegistryOwner provides activityResultOwner)
        }
        if (backPressedOwner != null) {
            add(androidx.activity.compose.LocalOnBackPressedDispatcherOwner provides backPressedOwner)
        }
    }.toTypedArray()

    CompositionLocalProvider(*providedValues) {
        NexaIslandTheme(
            themeMode = config.themeMode,
            languageCode = config.languageCode,
            accentColorOverride = config.accentColorHex
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                contentWindowInsets = WindowInsets.safeDrawing,
                bottomBar = {
                    NavigationBar(modifier = Modifier.testTag("nexa_bottom_nav")) {
                        NexaTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = stringResource(tab.labelRes)
                                    )
                                },
                                label = {
                                    Text(
                                        text = stringResource(tab.labelRes),
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier.testTag("nav_tab_${tab.route}")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                when (currentTab) {
                    NexaTab.DASHBOARD -> DashboardScreen(
                        config = config,
                        diagnostics = diagnostics,
                        activeEvent = activeEvent,
                        queueSize = queue.size,
                        currentQueueIndex = selectedIndex,
                        isExpanded = isExpanded,
                        isDeviceLocked = isDeviceLocked,
                        onToggleMaster = { enabled ->
                            viewModel.setMasterEnabled(localizedContext, enabled)
                        },
                        onRefreshDiagnostics = {
                            viewModel.refreshDiagnosticsAndApps(localizedContext)
                        },
                        onSendTestNotification = {
                            viewModel.sendRealTestNotification(localizedContext)
                        },
                        onTriggerStateDemo = { stateType ->
                            viewModel.triggerStateDemo(localizedContext, stateType)
                        },
                        onClearQueue = { viewModel.clearQueue() },
                        onNavigateToStyles = { currentTab = NexaTab.STYLES },
                        onNavigateToGeometry = { currentTab = NexaTab.GEOMETRY },
                        onNavigateToHyperOs = { currentTab = NexaTab.HYPEROS },
                        modifier = Modifier.padding(innerPadding)
                    )

                    NexaTab.STYLES -> StylesGalleryScreen(
                        config = config,
                        activeEvent = activeEvent,
                        isExpanded = isExpanded,
                        onSelectStyle = { viewModel.setStylePreset(it) },
                        onSelectPalette = { viewModel.applyPalettePreset(it) },
                        onUpdateCustomColor = { accent, islandBg, notifBg, primaryText ->
                            viewModel.updateCustomColor(
                                accent = accent,
                                islandBg = islandBg,
                                notifBg = notifBg,
                                primaryText = primaryText
                            )
                        },
                        onUpdateVisibility = { icon, appName, title, msg, time, group, expand, idlePill ->
                            viewModel.updateVisibilityAndBehavior(
                                showAppIcon = icon,
                                showAppName = appName,
                                showTitle = title,
                                showMessagePreview = msg,
                                showTimestamp = time,
                                groupSameApp = group,
                                autoExpandNotifications = expand,
                                keepIdlePillVisible = idlePill
                            )
                        },
                        modifier = Modifier.padding(innerPadding)
                    )

                    NexaTab.GEOMETRY -> GeometryPositionScreen(
                        config = config,
                        activeEvent = activeEvent,
                        isExpanded = isExpanded,
                        onUpdateGeometry = { cw, ch, ew, eh, ox, oy, cr, om, isz, ts, bw, se, op, ad, dd ->
                            viewModel.updateGeometry(
                                compactWidthDp = cw,
                                compactHeightDp = ch,
                                expandedWidthDp = ew,
                                expandedHeightDp = eh,
                                offsetXDp = ox,
                                offsetYDp = oy,
                                cornerRadiusDp = cr,
                                outerMarginDp = om,
                                iconSizeDp = isz,
                                textScalePercent = ts,
                                borderWidthDp = bw,
                                shadowElevationDp = se,
                                opacityPercent = op,
                                animationDurationMs = ad,
                                displayDurationSec = dd
                            )
                        },
                        onResetGeometry = { viewModel.resetGeometryToDefaults() },
                        modifier = Modifier.padding(innerPadding)
                    )

                    NexaTab.HYPEROS -> HyperOsCompatibilityScreen(
                        diagnostics = diagnostics,
                        onRefreshDiagnostics = {
                            viewModel.refreshDiagnosticsAndApps(localizedContext)
                        },
                        modifier = Modifier.padding(innerPadding)
                    )

                    NexaTab.SETTINGS -> CompleteSettingsScreen(
                        config = config,
                        diagnostics = diagnostics,
                        installedApps = installedApps,
                        onToggleMaster = { enabled ->
                            viewModel.setMasterEnabled(localizedContext, enabled)
                        },
                        onSetLanguage = { lang -> viewModel.setLanguageCode(lang) },
                        onSetThemeMode = { mode -> viewModel.setThemeMode(mode) },
                        onUpdateBehavior = { boot, haptic, swipe, lockShow, lockHideSens, alwaysHide ->
                            viewModel.updateVisibilityAndBehavior(
                                restoreOnBoot = boot,
                                hapticFeedback = haptic,
                                swipeToDismiss = swipe,
                                showOnLockScreen = lockShow,
                                hideSensitiveOnLockScreen = lockHideSens,
                                alwaysHideSensitiveContent = alwaysHide
                            )
                        },
                        onTogglePackageBlocked = { pkg, blocked ->
                            viewModel.togglePackageBlocked(localizedContext, pkg, blocked)
                        },
                        onAllowAllPackages = {
                            viewModel.allowAllPackages(localizedContext)
                        },
                        onResetAll = { viewModel.resetAllToDefaults() },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
