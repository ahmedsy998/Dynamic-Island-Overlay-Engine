package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.IslandStateController
import com.example.model.IslandConfiguration
import com.example.model.IslandEventItem
import com.example.model.IslandEventType
import com.example.model.ServiceDiagnosticsState
import com.example.ui.components.DynamicIslandCapsule
import com.example.util.SystemPermissionHelper

@Composable
fun DashboardScreen(
    config: IslandConfiguration,
    diagnostics: ServiceDiagnosticsState,
    activeEvent: IslandEventItem?,
    queueSize: Int,
    currentQueueIndex: Int,
    isExpanded: Boolean,
    isDeviceLocked: Boolean,
    onToggleMaster: (Boolean) -> Unit,
    onRefreshDiagnostics: () -> Unit,
    onSendTestNotification: () -> Unit,
    onTriggerStateDemo: (IslandEventType) -> Unit,
    onClearQueue: () -> Unit,
    onNavigateToStyles: () -> Unit,
    onNavigateToGeometry: () -> Unit,
    onNavigateToHyperOs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val notifPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        onRefreshDiagnostics()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Live Interactive Dynamic Island Showcase Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("live_preview_card"),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0A101E)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(config.accentColorHex).copy(alpha = 0.2f),
                                Color(0xFF0A101E)
                            )
                        )
                    )
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.app_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(config.accentColorHex).copy(alpha = 0.16f))
                            .border(
                                1.dp,
                                Color(config.accentColorHex).copy(alpha = 0.4f),
                                RoundedCornerShape(50)
                            )
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.live_preview_title),
                            color = Color(config.accentColorHex),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Interactive Phone Bezel Frame with Live DynamicIslandCapsule
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFF131C31))
                        .border(1.dp, Color(0xFF263554), RoundedCornerShape(22.dp))
                        .padding(vertical = 20.dp, horizontal = 8.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    DynamicIslandCapsule(
                        config = config,
                        activeEvent = activeEvent,
                        queueSize = queueSize,
                        currentQueueIndex = currentQueueIndex,
                        isExpanded = isExpanded,
                        isDeviceLocked = isDeviceLocked,
                        onToggleExpand = { IslandStateController.toggleExpanded() },
                        onOpenNotificationIntent = { item ->
                            try {
                                item.contentIntent?.send()
                            } catch (_: Exception) {
                            }
                        },
                        onDismissEvent = {
                            IslandStateController.dismissCurrentEvent(notifySystemListener = true)
                        },
                        onNextEvent = { IslandStateController.selectNextEvent() },
                        onPrevEvent = { IslandStateController.selectPreviousEvent() },
                        isPreviewMode = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.live_preview_hint),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        // 2. Prominent Master Enable/Disable Switch Card
        val isFullyOperational = diagnostics.userPreferenceEnabled &&
            diagnostics.allRequiredPermissionsGranted &&
            diagnostics.overlayServiceRunning
        val masterBorderColor = when {
            isFullyOperational -> Color(0xFF10B981)
            diagnostics.userPreferenceEnabled -> Color(0xFFF59E0B)
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, masterBorderColor, RoundedCornerShape(22.dp))
                .testTag("master_switch_card"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(masterBorderColor.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = null,
                            tint = masterBorderColor,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.master_switch_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        val subtitleRes = when {
                            isFullyOperational -> R.string.master_switch_active_desc
                            diagnostics.userPreferenceEnabled -> R.string.master_switch_missing_perms_desc
                            else -> R.string.master_switch_inactive_desc
                        }
                        Text(
                            text = stringResource(subtitleRes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Switch(
                    checked = config.masterEnabled,
                    onCheckedChange = { enabled ->
                        onToggleMaster(enabled)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF10B981)
                    ),
                    modifier = Modifier.testTag("master_enable_switch")
                )
            }
        }

        // 3. Real-Time Test Notification & Island State Triggers
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.test_actions_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = onSendTestNotification,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_test_notification"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.btn_test_notification),
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StateTriggerChip(
                        icon = Icons.Default.Call,
                        label = stringResource(R.string.btn_test_call),
                        onClick = { onTriggerStateDemo(IslandEventType.INCOMING_CALL) },
                        tag = "btn_test_call"
                    )
                    StateTriggerChip(
                        icon = Icons.Default.MusicNote,
                        label = stringResource(R.string.btn_test_music),
                        onClick = { onTriggerStateDemo(IslandEventType.MUSIC_PLAYBACK) },
                        tag = "btn_test_music"
                    )
                    StateTriggerChip(
                        icon = Icons.Default.BatteryChargingFull,
                        label = stringResource(R.string.btn_test_charging),
                        onClick = { onTriggerStateDemo(IslandEventType.CHARGING_BATTERY) },
                        tag = "btn_test_charging"
                    )
                    StateTriggerChip(
                        icon = Icons.Default.Timer,
                        label = stringResource(R.string.btn_test_timer),
                        onClick = { onTriggerStateDemo(IslandEventType.TIMER_COUNTDOWN) },
                        tag = "btn_test_timer"
                    )
                    if (queueSize > 0) {
                        StateTriggerChip(
                            icon = Icons.Default.DeleteSweep,
                            label = stringResource(R.string.btn_clear_queue),
                            onClick = onClearQueue,
                            tag = "btn_clear_queue"
                        )
                    }
                }
            }
        }

        // 4. Setup Checklist & Actionable Permissions
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("setup_checklist"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.checklist_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.checklist_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                PermissionChecklistRow(
                    title = stringResource(R.string.perm_overlay_title),
                    description = stringResource(R.string.perm_overlay_desc),
                    isGranted = diagnostics.overlayPermissionGranted,
                    onAction = { SystemPermissionHelper.openOverlayPermissionSettings(context) },
                    testTag = "perm_overlay_btn"
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                PermissionChecklistRow(
                    title = stringResource(R.string.perm_notification_listener_title),
                    description = stringResource(R.string.perm_notification_listener_desc),
                    isGranted = diagnostics.notificationListenerGranted,
                    onAction = { SystemPermissionHelper.openNotificationListenerSettings(context) },
                    testTag = "perm_listener_btn"
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    PermissionChecklistRow(
                        title = stringResource(R.string.perm_post_notifications_title),
                        description = stringResource(R.string.perm_post_notifications_desc),
                        isGranted = diagnostics.postNotificationsGranted,
                        onAction = {
                            notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        },
                        testTag = "perm_post_notif_btn"
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                PermissionChecklistRow(
                    title = stringResource(R.string.perm_battery_title),
                    description = stringResource(R.string.perm_battery_desc),
                    isGranted = diagnostics.batteryOptimizationIgnored,
                    onAction = { SystemPermissionHelper.openBatteryOptimizationSettings(context) },
                    testTag = "perm_battery_btn"
                )
            }
        }

        // 5. Real 6-Point Service Diagnostics Card (Never Faked)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("diagnostics_card"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.diagnostics_card_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = {
                            SystemPermissionHelper.requestListenerRebind(context)
                            onRefreshDiagnostics()
                        },
                        modifier = Modifier.testTag("refresh_diagnostics_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.action_reconnect)
                        )
                    }
                }

                DiagnosticStatusRow(
                    label = stringResource(R.string.diag_user_pref),
                    valueText = if (diagnostics.userPreferenceEnabled) {
                        stringResource(R.string.status_enabled)
                    } else {
                        stringResource(R.string.status_disabled)
                    },
                    isHealthy = diagnostics.userPreferenceEnabled
                )
                DiagnosticStatusRow(
                    label = stringResource(R.string.diag_permissions_granted),
                    valueText = if (diagnostics.allRequiredPermissionsGranted) {
                        stringResource(R.string.status_granted)
                    } else {
                        stringResource(R.string.status_missing)
                    },
                    isHealthy = diagnostics.allRequiredPermissionsGranted
                )
                DiagnosticStatusRow(
                    label = stringResource(R.string.diag_listener_connected),
                    valueText = if (diagnostics.notificationListenerConnected) {
                        stringResource(R.string.status_connected)
                    } else {
                        stringResource(R.string.status_disconnected)
                    },
                    isHealthy = diagnostics.notificationListenerConnected
                )
                DiagnosticStatusRow(
                    label = stringResource(R.string.diag_overlay_service),
                    valueText = if (diagnostics.overlayServiceRunning) {
                        stringResource(R.string.status_running)
                    } else {
                        stringResource(R.string.status_stopped)
                    },
                    isHealthy = diagnostics.overlayServiceRunning
                )
                DiagnosticStatusRow(
                    label = stringResource(R.string.diag_island_visible),
                    valueText = if (diagnostics.islandCurrentlyVisible) {
                        stringResource(R.string.status_visible)
                    } else {
                        stringResource(R.string.status_hidden)
                    },
                    isHealthy = diagnostics.islandCurrentlyVisible
                )
                DiagnosticStatusRow(
                    label = stringResource(R.string.diag_os_restrictions),
                    valueText = if (diagnostics.batteryOptimizationIgnored) {
                        stringResource(R.string.status_unrestricted)
                    } else {
                        stringResource(R.string.status_restricted)
                    },
                    isHealthy = diagnostics.batteryOptimizationIgnored
                )
            }
        }

        // 6. Quick Navigation Shortcuts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickShortcutCard(
                icon = Icons.Default.Palette,
                title = stringResource(R.string.nav_styles),
                onClick = onNavigateToStyles,
                modifier = Modifier.weight(1f)
            )
            QuickShortcutCard(
                icon = Icons.Default.Tune,
                title = stringResource(R.string.nav_geometry),
                onClick = onNavigateToGeometry,
                modifier = Modifier.weight(1f)
            )
            QuickShortcutCard(
                icon = Icons.Default.Security,
                title = stringResource(R.string.nav_hyperos),
                onClick = onNavigateToHyperOs,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun StateTriggerChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tag: String
) {
    FilledTonalButton(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag(tag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun PermissionChecklistRow(
    title: String,
    description: String,
    isGranted: Boolean,
    onAction: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = if (isGranted) Color(0xFF10B981) else Color(0xFFF59E0B),
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        OutlinedButton(
            onClick = onAction,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = if (isGranted) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.testTag(testTag)
        ) {
            Text(
                text = if (isGranted) {
                    stringResource(R.string.action_configured)
                } else {
                    stringResource(R.string.action_grant)
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DiagnosticStatusRow(
    label: String,
    valueText: String,
    isHealthy: Boolean
) {
    val badgeColor = if (isHealthy) Color(0xFF10B981) else Color(0xFFF59E0B)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(badgeColor.copy(alpha = 0.15f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = valueText,
                color = badgeColor,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun QuickShortcutCard(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}
