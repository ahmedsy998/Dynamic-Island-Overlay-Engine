package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.IslandConfiguration
import com.example.model.IslandEventItem

/**
 * Fixed top Live Preview panel for customization screens.
 * Remains pinned at the top of the screen while customization settings scroll independently below.
 * Renders the actual [DynamicIslandCapsule] composable used by the real overlay service.
 */
@Composable
fun FixedLivePreviewPanel(
    config: IslandConfiguration,
    previewEvent: IslandEventItem,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = Color(config.accentColorHex)
    val pulseTransition = rememberInfiniteTransition(label = "live_preview_pulse")
    val dotAlpha by pulseTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_dot_alpha"
    )

    val activeWidthDp = if (isExpanded) config.expandedWidthDp else config.compactWidthDp
    val activeHeightDp = if (isExpanded) config.expandedHeightDp else config.compactHeightDp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 6.dp)
            .testTag("fixed_live_preview_panel")
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = accentColor.copy(alpha = 0.32f),
                    shape = RoundedCornerShape(22.dp)
                ),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF080D1A)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = (config.shadowElevationDp / 90f).coerceIn(0.08f, 0.28f)),
                                Color(0xFF080D1A)
                            )
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row: "LIVE PREVIEW" indicator badge + live geometry telemetry + Expand/Collapse toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(accentColor.copy(alpha = 0.14f))
                                .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(50))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .testTag("live_preview_badge"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .alpha(dotAlpha)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LIVE PREVIEW",
                                color = Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.6.sp,
                                modifier = Modifier.testTag("live_preview_label")
                            )
                        }

                        Text(
                            text = "${activeWidthDp}×${activeHeightDp}dp • R${config.cornerRadiusDp} • ${config.opacityPercent}%",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.08f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onToggleExpand)
                            .testTag("live_preview_mode_toggle")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.UnfoldLess else Icons.Default.UnfoldMore,
                                contentDescription = stringResource(R.string.live_preview_title),
                                tint = accentColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isExpanded) {
                                    if (config.languageCode == "en") "Expanded" else "موسّع"
                                } else {
                                    if (config.languageCode == "en") "Compact" else "مدمج"
                                },
                                color = Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Phone Screen Top-Bezel Viewport with Cutout Reference & Real DynamicIslandCapsule
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF111A2E))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(18.dp))
                        .padding(top = 8.dp, bottom = 16.dp, start = 6.dp, end = 6.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    // Subtle simulated camera punch-hole reference marker at top center
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF050811))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                    )

                    DynamicIslandCapsule(
                        config = config,
                        activeEvent = previewEvent,
                        queueSize = 2,
                        currentQueueIndex = 0,
                        isExpanded = isExpanded,
                        isDeviceLocked = false,
                        onToggleExpand = onToggleExpand,
                        onOpenNotificationIntent = {},
                        onDismissEvent = {},
                        onNextEvent = {},
                        onPrevEvent = {},
                        isPreviewMode = true
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f),
            thickness = 1.dp
        )
    }
}
