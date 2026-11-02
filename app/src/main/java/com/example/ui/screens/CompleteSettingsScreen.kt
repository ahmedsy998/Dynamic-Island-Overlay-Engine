package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.AppThemeMode
import com.example.model.InstalledAppInfo
import com.example.model.IslandConfiguration
import com.example.model.ServiceDiagnosticsState
import com.example.util.SystemPermissionHelper

@Composable
fun CompleteSettingsScreen(
    config: IslandConfiguration,
    diagnostics: ServiceDiagnosticsState,
    installedApps: List<InstalledAppInfo>,
    onToggleMaster: (Boolean) -> Unit,
    onSetLanguage: (String) -> Unit,
    onSetThemeMode: (AppThemeMode) -> Unit,
    onUpdateBehavior: (
        restoreOnBoot: Boolean?,
        hapticFeedback: Boolean?,
        swipeToDismiss: Boolean?,
        showOnLockScreen: Boolean?,
        hideSensitiveLockScreen: Boolean?,
        alwaysHideSensitive: Boolean?
    ) -> Unit,
    onTogglePackageBlocked: (String, Boolean) -> Unit,
    onAllowAllPackages: () -> Unit,
    onResetAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showAppFilterDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_screen_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // 1. Appearance & Language Section (Arabic-First & English Selector)
        SettingsCategoryCard(title = stringResource(R.string.settings_cat_appearance)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.setting_language_label),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = config.languageCode != "en",
                    onClick = { onSetLanguage("ar") },
                    label = {
                        Text(
                            text = stringResource(R.string.lang_arabic),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("lang_ar_btn")
                )
                FilterChip(
                    selected = config.languageCode == "en",
                    onClick = { onSetLanguage("en") },
                    label = {
                        Text(
                            text = stringResource(R.string.lang_english),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("lang_en_btn")
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            Text(
                text = stringResource(R.string.setting_theme_label),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val modes = listOf(
                    AppThemeMode.DARK to R.string.theme_dark,
                    AppThemeMode.LIGHT to R.string.theme_light,
                    AppThemeMode.SYSTEM to R.string.theme_system
                )
                modes.forEach { (mode, labelRes) ->
                    FilterChip(
                        selected = config.themeMode == mode,
                        onClick = { onSetThemeMode(mode) },
                        label = { Text(stringResource(labelRes)) },
                        modifier = Modifier.testTag("theme_${mode.id}")
                    )
                }
            }
        }

        // 2. General Settings
        SettingsCategoryCard(title = stringResource(R.string.settings_cat_general)) {
            SettingToggleItem(
                label = stringResource(R.string.master_switch_title),
                checked = config.masterEnabled,
                onCheckedChange = onToggleMaster
            )
            SettingToggleItem(
                label = stringResource(R.string.setting_restore_on_boot),
                checked = config.restoreOnBoot,
                onCheckedChange = { onUpdateBehavior(it, null, null, null, null, null) }
            )
            SettingToggleItem(
                label = stringResource(R.string.setting_haptic_feedback),
                checked = config.hapticFeedback,
                onCheckedChange = { onUpdateBehavior(null, it, null, null, null, null) }
            )
            SettingToggleItem(
                label = stringResource(R.string.setting_swipe_to_dismiss),
                checked = config.swipeToDismiss,
                onCheckedChange = { onUpdateBehavior(null, null, it, null, null, null) }
            )
        }

        // 3. Privacy, Lock Screen & App Filtering
        SettingsCategoryCard(title = stringResource(R.string.settings_cat_privacy)) {
            SettingToggleItem(
                label = stringResource(R.string.setting_show_on_lockscreen),
                checked = config.showOnLockScreen,
                onCheckedChange = { onUpdateBehavior(null, null, null, it, null, null) }
            )
            SettingToggleItem(
                label = stringResource(R.string.setting_hide_sensitive_lockscreen),
                checked = config.hideSensitiveOnLockScreen,
                onCheckedChange = { onUpdateBehavior(null, null, null, null, it, null) }
            )
            SettingToggleItem(
                label = stringResource(R.string.setting_hide_all_sensitive),
                checked = config.alwaysHideSensitiveContent,
                onCheckedChange = { onUpdateBehavior(null, null, null, null, null, it) }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            Text(
                text = stringResource(R.string.app_filter_title),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.app_filter_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            FilledTonalButton(
                onClick = { showAppFilterDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_manage_app_filters"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.FilterList, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(
                        R.string.app_filter_manage_btn,
                        config.blockedPackages.size
                    ),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 4. Battery Efficiency Engineering Note
        SettingsCategoryCard(title = stringResource(R.string.settings_cat_battery)) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.BatterySaver,
                    contentDescription = null,
                    tint = Color(0xFF10B981)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.battery_efficiency_note),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 5. Advanced Diagnostics & Reset
        SettingsCategoryCard(title = stringResource(R.string.settings_cat_advanced)) {
            val copiedToastText = stringResource(R.string.diagnostics_copied_toast)
            OutlinedButton(
                onClick = {
                    val report = SystemPermissionHelper.buildSafeDiagnosticReport(
                        context = context,
                        diagnostics = diagnostics,
                        styleId = config.stylePreset.id,
                        languageCode = config.languageCode
                    )
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                    clipboard?.setPrimaryClip(ClipData.newPlainText("Nexa Island Diagnostics", report))
                    Toast.makeText(context, copiedToastText, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_export_diagnostics"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.btn_export_diagnostics),
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = { showResetConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_reset_all_settings"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFEF4444)
                )
            ) {
                Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.btn_reset_all),
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = stringResource(R.string.version_info_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showAppFilterDialog) {
        AlertDialog(
            onDismissRequest = { showAppFilterDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.app_filter_dialog_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                if (installedApps.isEmpty()) {
                    Text(text = stringResource(R.string.app_filter_empty))
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 340.dp)
                    ) {
                        items(installedApps, key = { it.packageName }) { app ->
                            val isAllowed = !config.blockedPackages.contains(app.packageName)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        onTogglePackageBlocked(app.packageName, isAllowed)
                                    }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = app.appName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = app.packageName,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Checkbox(
                                    checked = isAllowed,
                                    onCheckedChange = { allowed ->
                                        onTogglePackageBlocked(app.packageName, !allowed)
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAppFilterDialog = false }) {
                    Text(stringResource(R.string.app_filter_close), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                if (config.blockedPackages.isNotEmpty()) {
                    TextButton(onClick = onAllowAllPackages) {
                        Text(stringResource(R.string.app_filter_allow_all))
                    }
                }
            }
        )
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.reset_confirm_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(text = stringResource(R.string.reset_confirm_desc))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetAll()
                        showResetConfirmDialog = false
                    },
                    modifier = Modifier.testTag("confirm_reset_btn")
                ) {
                    Text(
                        text = stringResource(R.string.action_confirm),
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text(text = stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun SettingsCategoryCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            content()
        }
    }
}

@Composable
private fun SettingToggleItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
