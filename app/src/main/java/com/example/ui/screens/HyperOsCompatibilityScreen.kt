package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.ServiceDiagnosticsState
import com.example.util.SystemPermissionHelper

@Composable
fun HyperOsCompatibilityScreen(
    diagnostics: ServiceDiagnosticsState,
    onRefreshDiagnostics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.hyperos_screen_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.hyperos_screen_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Detected Device Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hyperos_device_card"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.hyperos_detected_banner),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = diagnostics.deviceSummary.ifBlank { SystemPermissionHelper.getDeviceSummary() },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.hyperos_honest_note),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Step 1: Autostart (Manual OEM Setting)
        HyperOsStepCard(
            icon = Icons.Default.PlayCircleOutline,
            title = stringResource(R.string.hyperos_step_autostart_title),
            description = stringResource(R.string.hyperos_step_autostart_desc),
            tagLabel = stringResource(R.string.hyperos_manual_tag),
            isProgrammatic = false,
            buttonText = stringResource(R.string.hyperos_btn_autostart),
            onAction = { SystemPermissionHelper.openHyperOSAutostartSettings(context) },
            testTag = "hyperos_autostart_btn"
        )

        // Step 2: Battery Optimization (Programmatically Inspectable)
        HyperOsStepCard(
            icon = Icons.Default.BatteryChargingFull,
            title = stringResource(R.string.hyperos_step_battery_title),
            description = stringResource(R.string.hyperos_step_battery_desc),
            tagLabel = "${stringResource(R.string.hyperos_programmatic_tag)} • " +
                if (diagnostics.batteryOptimizationIgnored) {
                    stringResource(R.string.status_unrestricted)
                } else {
                    stringResource(R.string.status_restricted)
                },
            isProgrammatic = true,
            isHealthy = diagnostics.batteryOptimizationIgnored,
            buttonText = stringResource(R.string.hyperos_btn_battery),
            onAction = {
                SystemPermissionHelper.openBatteryOptimizationSettings(context)
                onRefreshDiagnostics()
            },
            testTag = "hyperos_battery_btn"
        )

        // Step 3: Lock-Screen & Background Pop-up Permissions (Manual OEM Setting)
        HyperOsStepCard(
            icon = Icons.Default.Security,
            title = stringResource(R.string.hyperos_step_lockscreen_title),
            description = stringResource(R.string.hyperos_step_lockscreen_desc),
            tagLabel = stringResource(R.string.hyperos_manual_tag),
            isProgrammatic = false,
            buttonText = stringResource(R.string.hyperos_btn_other_perms),
            onAction = { SystemPermissionHelper.openHyperOSOtherPermissions(context) },
            testTag = "hyperos_other_perms_btn"
        )

        // Step 4: Lock App in Recents Overview + Rebind Listener
        HyperOsStepCard(
            icon = Icons.Default.Lock,
            title = stringResource(R.string.hyperos_step_recents_title),
            description = stringResource(R.string.hyperos_step_recents_desc),
            tagLabel = stringResource(R.string.hyperos_manual_tag),
            isProgrammatic = false,
            buttonText = stringResource(R.string.hyperos_btn_notif_listener),
            onAction = {
                SystemPermissionHelper.openNotificationListenerSettings(context)
            },
            testTag = "hyperos_notif_listener_btn"
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun HyperOsStepCard(
    icon: ImageVector,
    title: String,
    description: String,
    tagLabel: String,
    isProgrammatic: Boolean,
    isHealthy: Boolean = true,
    buttonText: String,
    onAction: () -> Unit,
    testTag: String
) {
    val badgeColor = when {
        !isProgrammatic -> Color(0xFF38BDF8)
        isHealthy -> Color(0xFF10B981)
        else -> Color(0xFFF59E0B)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(badgeColor.copy(alpha = 0.16f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = tagLabel,
                    color = badgeColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedButton(
                onClick = onAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(testTag),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = buttonText, fontWeight = FontWeight.Bold)
            }
        }
    }
}
