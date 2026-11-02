package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.IslandStateController
import com.example.model.ColorPalettePreset
import com.example.model.IslandConfiguration
import com.example.model.IslandEventItem
import com.example.model.IslandEventType
import com.example.model.NotificationStylePreset
import com.example.ui.components.DynamicIslandCapsule

@Composable
fun StylesGalleryScreen(
    config: IslandConfiguration,
    activeEvent: IslandEventItem?,
    isExpanded: Boolean,
    onSelectStyle: (NotificationStylePreset) -> Unit,
    onSelectPalette: (ColorPalettePreset) -> Unit,
    onUpdateCustomColor: (accent: Long?, islandBg: Long?, notifBg: Long?, primaryText: Long?) -> Unit,
    onUpdateVisibility: (
        showIcon: Boolean?,
        showAppName: Boolean?,
        showTitle: Boolean?,
        showMessage: Boolean?,
        showTimestamp: Boolean?,
        groupSameApp: Boolean?,
        autoExpand: Boolean?,
        keepIdlePill: Boolean?
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val isAr = config.languageCode != "en"

    val previewEvent = activeEvent ?: remember(isAr) {
        IslandEventItem(
            key = "style_preview_sample",
            packageName = "com.whatsapp",
            appName = if (isAr) "واتساب" else "WhatsApp",
            title = if (isAr) "رسالة واردة • تصميم One UI 9" else "New Message • One UI 9 Preset",
            message = if (isAr) "مرحباً! هذه معاينة حية لشكل الإشعار الحقيقي داخل الجزيرة التفاعلية." else "Live preview of how real incoming notifications appear in your selected style.",
            eventType = IslandEventType.NOTIFICATION,
            groupCount = 2
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.styles_screen_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.styles_screen_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Live Preview of Selected Style
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0B1120))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(24.dp))
                .padding(vertical = 22.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            DynamicIslandCapsule(
                config = config,
                activeEvent = previewEvent,
                queueSize = 2,
                currentQueueIndex = 0,
                isExpanded = isExpanded,
                isDeviceLocked = false,
                onToggleExpand = { IslandStateController.toggleExpanded() },
                onOpenNotificationIntent = {},
                onDismissEvent = {},
                onNextEvent = {},
                onPrevEvent = {},
                isPreviewMode = true
            )
        }

        // 7 Notification Style Cards (with Samsung One UI 9-Inspired Style highlighted)
        val styleEntries = listOf(
            Triple(
                NotificationStylePreset.SAMSUNG_ONE_UI_9,
                R.string.style_oneui9_title,
                R.string.style_oneui9_desc
            ),
            Triple(
                NotificationStylePreset.MINIMAL_BLACK_PILL,
                R.string.style_minimal_black_title,
                R.string.style_minimal_black_desc
            ),
            Triple(
                NotificationStylePreset.PREMIUM_GLASS_PILL,
                R.string.style_glass_pill_title,
                R.string.style_glass_pill_desc
            ),
            Triple(
                NotificationStylePreset.SOFT_GRADIENT_PILL,
                R.string.style_gradient_pill_title,
                R.string.style_gradient_pill_desc
            ),
            Triple(
                NotificationStylePreset.ROUNDED_NOTIFICATION_CARD,
                R.string.style_rounded_card_title,
                R.string.style_rounded_card_desc
            ),
            Triple(
                NotificationStylePreset.COMPACT_ICON_ONLY,
                R.string.style_compact_icon_title,
                R.string.style_compact_icon_desc
            ),
            Triple(
                NotificationStylePreset.EXPANDED_NOTIFICATION_CARD,
                R.string.style_expanded_card_title,
                R.string.style_expanded_card_desc
            )
        )

        styleEntries.forEach { (preset, titleRes, descRes) ->
            val selected = config.stylePreset == preset
            val isOneUi9 = preset == NotificationStylePreset.SAMSUNG_ONE_UI_9
            val borderColor = when {
                selected -> MaterialTheme.colorScheme.primary
                isOneUi9 -> Color(0xFF38BDF8).copy(alpha = 0.45f)
                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(if (selected) 2.dp else 1.dp, borderColor, RoundedCornerShape(20.dp))
                    .clickable { onSelectStyle(preset) }
                    .testTag("style_card_${preset.id}"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                imageVector = if (selected) {
                                    Icons.Default.CheckCircle
                                } else {
                                    Icons.Default.RadioButtonUnchecked
                                },
                                contentDescription = null,
                                tint = if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(titleRes),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (isOneUi9) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0xFF38BDF8).copy(alpha = 0.18f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(R.string.style_oneui9_badge),
                                        color = Color(0xFF38BDF8),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = stringResource(descRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Color Palette Presets & Custom Swatches
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
                    text = stringResource(R.string.palette_section_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                val palettes = listOf(
                    ColorPalettePreset.GRAPHITE_SILVER to R.string.palette_graphite,
                    ColorPalettePreset.SOFT_BLUE to R.string.palette_soft_blue,
                    ColorPalettePreset.TEAL to R.string.palette_teal,
                    ColorPalettePreset.MUTED_VIOLET to R.string.palette_muted_violet,
                    ColorPalettePreset.EMERALD to R.string.palette_emerald,
                    ColorPalettePreset.WARM_NEUTRAL to R.string.palette_warm_neutral
                )

                palettes.forEach { (palette, labelRes) ->
                    val isSelected = config.palettePreset == palette
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                }
                            )
                            .clickable { onSelectPalette(palette) }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("palette_${palette.id}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color(palette.accentHex))
                                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(labelRes),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Quick Custom Accent Swatches
                Text(
                    text = stringResource(R.string.custom_color_accent),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                val accentSwatches = listOf(
                    0xFF38BDF8L,
                    0xFF2DD4BFL,
                    0xFFA78BFAL,
                    0xFF34D399L,
                    0xFFFBBF24L,
                    0xFFF472B6L,
                    0xFF60A5FAL,
                    0xFFE2E8F0L
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    accentSwatches.forEach { hex ->
                        val active = config.accentColorHex == hex
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(hex))
                                .border(
                                    width = if (active) 3.dp else 1.dp,
                                    color = if (active) Color.White else Color.White.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                                .clickable {
                                    onUpdateCustomColor(hex, null, null, null)
                                }
                        )
                    }
                }
            }
        }

        // Shared Element Visibility Toggles
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = stringResource(R.string.style_controls_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                StyleToggleRow(
                    label = stringResource(R.string.toggle_show_app_icon),
                    checked = config.showAppIcon,
                    onCheckedChange = {
                        onUpdateVisibility(it, null, null, null, null, null, null, null)
                    }
                )
                StyleToggleRow(
                    label = stringResource(R.string.toggle_show_app_name),
                    checked = config.showAppName,
                    onCheckedChange = {
                        onUpdateVisibility(null, it, null, null, null, null, null, null)
                    }
                )
                StyleToggleRow(
                    label = stringResource(R.string.toggle_show_title),
                    checked = config.showTitle,
                    onCheckedChange = {
                        onUpdateVisibility(null, null, it, null, null, null, null, null)
                    }
                )
                StyleToggleRow(
                    label = stringResource(R.string.toggle_show_message),
                    checked = config.showMessagePreview,
                    onCheckedChange = {
                        onUpdateVisibility(null, null, null, it, null, null, null, null)
                    }
                )
                StyleToggleRow(
                    label = stringResource(R.string.toggle_show_timestamp),
                    checked = config.showTimestamp,
                    onCheckedChange = {
                        onUpdateVisibility(null, null, null, null, it, null, null, null)
                    }
                )
                StyleToggleRow(
                    label = stringResource(R.string.toggle_group_same_app),
                    checked = config.groupSameApp,
                    onCheckedChange = {
                        onUpdateVisibility(null, null, null, null, null, it, null, null)
                    }
                )
                StyleToggleRow(
                    label = stringResource(R.string.toggle_auto_expand),
                    checked = config.autoExpandNotifications,
                    onCheckedChange = {
                        onUpdateVisibility(null, null, null, null, null, null, it, null)
                    }
                )
                StyleToggleRow(
                    label = stringResource(R.string.toggle_always_show_pill),
                    checked = config.keepIdlePillVisible,
                    onCheckedChange = {
                        onUpdateVisibility(null, null, null, null, null, null, null, it)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun StyleToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
