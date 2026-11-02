package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.IslandConfiguration
import com.example.model.IslandEventItem
import com.example.model.IslandEventType
import com.example.model.NotificationStylePreset
import com.example.util.TextDirectionHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun DynamicIslandCapsule(
    config: IslandConfiguration,
    activeEvent: IslandEventItem?,
    queueSize: Int,
    currentQueueIndex: Int,
    isExpanded: Boolean,
    isDeviceLocked: Boolean,
    onToggleExpand: () -> Unit,
    onOpenNotificationIntent: (IslandEventItem) -> Unit,
    onDismissEvent: () -> Unit,
    onNextEvent: () -> Unit,
    onPrevEvent: () -> Unit,
    modifier: Modifier = Modifier,
    isPreviewMode: Boolean = false
) {
    val view = LocalView.current
    val style = config.stylePreset
    val effectiveExpanded = isExpanded || style == NotificationStylePreset.EXPANDED_NOTIFICATION_CARD

    val targetWidth = when {
        activeEvent == null -> (config.compactWidthDp * 0.82f).dp
        effectiveExpanded -> config.expandedWidthDp.dp
        style == NotificationStylePreset.COMPACT_ICON_ONLY -> (config.compactHeightDp + 26).dp
        style == NotificationStylePreset.ROUNDED_NOTIFICATION_CARD -> (config.expandedWidthDp * 0.88f).dp
        style == NotificationStylePreset.SAMSUNG_ONE_UI_9 -> (config.compactWidthDp + 48).dp
        else -> config.compactWidthDp.dp
    }

    val targetHeight = when {
        activeEvent == null -> config.compactHeightDp.dp
        effectiveExpanded -> config.expandedHeightDp.dp
        style == NotificationStylePreset.ROUNDED_NOTIFICATION_CARD -> (config.compactHeightDp + 28).dp
        style == NotificationStylePreset.SAMSUNG_ONE_UI_9 -> (config.compactHeightDp + 14).dp
        else -> config.compactHeightDp.dp
    }

    val animDuration = config.animationDurationMs.coerceIn(120, 600)
    val animatedWidth by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = tween(
            durationMillis = animDuration,
            easing = FastOutSlowInEasing
        ),
        label = "island_width"
    )
    val animatedHeight by animateDpAsState(
        targetValue = targetHeight,
        animationSpec = tween(
            durationMillis = animDuration,
            easing = FastOutSlowInEasing
        ),
        label = "island_height"
    )

    val cornerRadius = when (style) {
        NotificationStylePreset.ROUNDED_NOTIFICATION_CARD -> (config.cornerRadiusDp - 4).coerceIn(10, 44).dp
        else -> config.cornerRadiusDp.coerceIn(12, 48).dp
    }

    val alphaFactor = (config.opacityPercent / 100f).coerceIn(0.7f, 1f)
    val accentColor = Color(config.accentColorHex)
    val baseBgColor = when (style) {
        NotificationStylePreset.MINIMAL_BLACK_PILL -> Color(config.islandBgColorHex).copy(alpha = alphaFactor)
        NotificationStylePreset.SAMSUNG_ONE_UI_9 -> Color(config.notifBgColorHex).copy(alpha = alphaFactor)
        else -> if (effectiveExpanded) {
            Color(config.notifBgColorHex).copy(alpha = alphaFactor)
        } else {
            Color(config.islandBgColorHex).copy(alpha = alphaFactor)
        }
    }

    val animatedBgColor by animateColorAsState(
        targetValue = baseBgColor,
        animationSpec = tween(animDuration, easing = FastOutSlowInEasing),
        label = "island_bg"
    )

    val primaryTextColor = Color(config.primaryTextColorHex)
    val secondaryTextColor = Color(config.secondaryTextColorHex)
    val borderColor = when {
        config.palettePreset == com.example.model.ColorPalettePreset.CUSTOM -> Color(config.borderColorHex)
        style == NotificationStylePreset.MINIMAL_BLACK_PILL -> Color(config.borderColorHex)
        style == NotificationStylePreset.PREMIUM_GLASS_PILL -> accentColor.copy(alpha = 0.55f)
        style == NotificationStylePreset.SAMSUNG_ONE_UI_9 -> accentColor.copy(alpha = 0.45f)
        else -> Color(config.borderColorHex)
    }

    var dragOffsetX by remember { mutableFloatStateOf(0f) }

    val shape = RoundedCornerShape(cornerRadius)
    val textScale = (config.textScalePercent / 100f).coerceIn(0.8f, 1.3f)

    // Privacy check for sensitive content
    val hideMessageForPrivacy = activeEvent != null && (
        config.alwaysHideSensitiveContent ||
            (isDeviceLocked && config.hideSensitiveOnLockScreen) ||
            (activeEvent.isSensitive && config.hideSensitiveOnLockScreen)
        )

    val previewVerticalOffsetDp = (config.offsetYDp * 0.4f).coerceIn(0f, 32f).dp
    val glowAlpha = (config.shadowElevationDp / 24f).coerceIn(0.15f, 0.7f)

    Box(
        modifier = modifier
            .padding(horizontal = config.outerMarginDp.dp)
            .offset {
                IntOffset(
                    x = (config.offsetXDp.dp.toPx() + dragOffsetX).roundToInt(),
                    y = if (isPreviewMode) {
                        previewVerticalOffsetDp.roundToPx()
                    } else {
                        config.offsetYDp.dp.roundToPx()
                    }
                )
            }
            .width(animatedWidth)
            .heightIn(min = animatedHeight)
            .shadow(
                elevation = config.shadowElevationDp.dp,
                shape = shape,
                ambientColor = accentColor.copy(alpha = glowAlpha * 0.65f),
                spotColor = accentColor.copy(alpha = glowAlpha)
            )
            .clip(shape)
            .background(
                brush = when (style) {
                    NotificationStylePreset.SOFT_GRADIENT_PILL -> Brush.linearGradient(
                        colors = listOf(
                            animatedBgColor,
                            accentColor.copy(alpha = 0.28f),
                            animatedBgColor
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(600f, 240f)
                    )
                    NotificationStylePreset.PREMIUM_GLASS_PILL -> Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.14f),
                            animatedBgColor.copy(alpha = (alphaFactor * 0.92f).coerceAtLeast(0.75f)),
                            animatedBgColor
                        )
                    )
                    NotificationStylePreset.SAMSUNG_ONE_UI_9 -> Brush.linearGradient(
                        colors = listOf(
                            animatedBgColor,
                            Color(0xFF182234).copy(alpha = alphaFactor)
                        )
                    )
                    else -> Brush.verticalGradient(listOf(animatedBgColor, animatedBgColor))
                }
            )
            .border(
                width = config.borderWidthDp.dp,
                color = borderColor,
                shape = shape
            )
            .drawWithCache {
                val topHighlight = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        accentColor.copy(alpha = if (style == NotificationStylePreset.PREMIUM_GLASS_PILL) 0.5f else 0.22f),
                        Color.Transparent
                    )
                )
                onDrawWithContent {
                    drawContent()
                    drawRect(
                        brush = topHighlight,
                        topLeft = Offset(0f, 0f),
                        size = size.copy(height = 1.5.dp.toPx())
                    )
                }
            }
            .pointerInput(config.swipeToDismiss, activeEvent?.key) {
                if (!config.swipeToDismiss || activeEvent == null) return@pointerInput
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (abs(dragOffsetX) > 110f) {
                            dragOffsetX = 0f
                            onDismissEvent()
                        } else {
                            dragOffsetX = 0f
                        }
                    },
                    onDragCancel = { dragOffsetX = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        dragOffsetX = (dragOffsetX + dragAmount).coerceIn(-220f, 220f)
                    }
                )
            }
            .clickable {
                if (config.hapticFeedback) {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                }
                onToggleExpand()
            }
            .testTag("dynamic_island_capsule"),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = Triple(activeEvent, effectiveExpanded, style),
            transitionSpec = {
                fadeIn(tween(animDuration / 2)) togetherWith fadeOut(tween(animDuration / 2))
            },
            label = "island_content_transition"
        ) { (event, expanded, currentStyle) ->
            if (event == null) {
                IdleIslandContent(
                    accentColor = accentColor,
                    primaryTextColor = primaryTextColor,
                    textScale = textScale
                )
            } else if (!expanded) {
                CompactIslandContent(
                    config = config,
                    style = currentStyle,
                    event = event,
                    accentColor = accentColor,
                    primaryTextColor = primaryTextColor,
                    secondaryTextColor = secondaryTextColor,
                    textScale = textScale,
                    hideMessageForPrivacy = hideMessageForPrivacy
                )
            } else {
                ExpandedIslandContent(
                    config = config,
                    style = currentStyle,
                    event = event,
                    queueSize = queueSize,
                    currentQueueIndex = currentQueueIndex,
                    accentColor = accentColor,
                    primaryTextColor = primaryTextColor,
                    secondaryTextColor = secondaryTextColor,
                    textScale = textScale,
                    hideMessageForPrivacy = hideMessageForPrivacy,
                    onOpenIntent = { onOpenNotificationIntent(event) },
                    onDismiss = onDismissEvent,
                    onNext = onNextEvent,
                    onPrev = onPrevEvent
                )
            }
        }
    }
}

@Composable
private fun IdleIslandContent(
    accentColor: Color,
    primaryTextColor: Color,
    textScale: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Camera lens indicator
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E293B))
                .border(1.dp, accentColor.copy(alpha = 0.45f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
        }

        Text(
            text = stringResource(R.string.island_idle_label),
            color = primaryTextColor.copy(alpha = 0.88f),
            fontSize = (11.sp * textScale),
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Subtle status dot
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(accentColor)
        )
    }
}

@Composable
private fun CompactIslandContent(
    config: IslandConfiguration,
    style: NotificationStylePreset,
    event: IslandEventItem,
    accentColor: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    textScale: Float,
    hideMessageForPrivacy: Boolean
) {
    val titleDir = remember(event.title) {
        TextDirectionHelper.detectTextLayoutDirection(event.title)
    }
    val msgDir = remember(event.message) {
        TextDirectionHelper.detectTextLayoutDirection(event.message)
    }

    when (style) {
        NotificationStylePreset.COMPACT_ICON_ONLY -> {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                EventLeadingBadge(
                    event = event,
                    iconSizeDp = config.iconSizeDp.coerceIn(18, 28),
                    accentColor = accentColor,
                    isOneUiSquircle = false
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
            }
        }

        NotificationStylePreset.SAMSUNG_ONE_UI_9 -> {
            // Distinct One UI 9 Compact Pill: Squircle icon container + App pill + Title + Live status indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (config.showAppIcon) {
                    EventLeadingBadge(
                        event = event,
                        iconSizeDp = (config.iconSizeDp + 2).coerceIn(20, 32),
                        accentColor = accentColor,
                        isOneUiSquircle = true
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    if (config.showAppName && event.appName.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = event.appName,
                                color = accentColor,
                                fontSize = (10.sp * textScale),
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (event.groupCount > 1) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(accentColor.copy(alpha = 0.2f))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "+${event.groupCount}",
                                        color = accentColor,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    if (config.showTitle) {
                        CompositionLocalProvider(LocalLayoutDirection provides titleDir) {
                            Text(
                                text = event.title.ifBlank { event.appName },
                                color = primaryTextColor,
                                fontSize = (12.5f.sp * textScale),
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))
                EventTrailingIndicator(
                    event = event,
                    accentColor = accentColor,
                    secondaryTextColor = secondaryTextColor
                )
            }
        }

        NotificationStylePreset.ROUNDED_NOTIFICATION_CARD -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (config.showAppIcon) {
                    EventLeadingBadge(
                        event = event,
                        iconSizeDp = config.iconSizeDp,
                        accentColor = accentColor,
                        isOneUiSquircle = false
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    if (config.showTitle) {
                        CompositionLocalProvider(LocalLayoutDirection provides titleDir) {
                            Text(
                                text = event.title.ifBlank { event.appName },
                                color = primaryTextColor,
                                fontSize = (12.sp * textScale),
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    if (config.showMessagePreview && event.message.isNotBlank()) {
                        val previewText = if (hideMessageForPrivacy) {
                            stringResource(R.string.island_sensitive_hidden)
                        } else {
                            event.message
                        }
                        CompositionLocalProvider(LocalLayoutDirection provides msgDir) {
                            Text(
                                text = previewText,
                                color = secondaryTextColor,
                                fontSize = (11.sp * textScale),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                EventTrailingIndicator(
                    event = event,
                    accentColor = accentColor,
                    secondaryTextColor = secondaryTextColor
                )
            }
        }

        else -> {
            // Minimal Black Pill, Glass Pill, Soft Gradient Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (config.showAppIcon) {
                        EventLeadingBadge(
                            event = event,
                            iconSizeDp = config.iconSizeDp.coerceAtMost(24),
                            accentColor = accentColor,
                            isOneUiSquircle = false
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    if (config.showTitle) {
                        CompositionLocalProvider(LocalLayoutDirection provides titleDir) {
                            Text(
                                text = event.title.ifBlank { event.appName },
                                color = primaryTextColor,
                                fontSize = (12.sp * textScale),
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                EventTrailingIndicator(
                    event = event,
                    accentColor = accentColor,
                    secondaryTextColor = secondaryTextColor
                )
            }
        }
    }
}

@Composable
private fun ExpandedIslandContent(
    config: IslandConfiguration,
    style: NotificationStylePreset,
    event: IslandEventItem,
    queueSize: Int,
    currentQueueIndex: Int,
    accentColor: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    textScale: Float,
    hideMessageForPrivacy: Boolean,
    onOpenIntent: () -> Unit,
    onDismiss: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit
) {
    val titleDir = remember(event.title) {
        TextDirectionHelper.detectTextLayoutDirection(event.title)
    }
    val msgDir = remember(event.message) {
        TextDirectionHelper.detectTextLayoutDirection(event.message)
    }
    val isOneUi9 = style == NotificationStylePreset.SAMSUNG_ONE_UI_9
    val timeFormatted = remember(event.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(event.timestamp))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = if (isOneUi9) 18.dp else 14.dp,
                vertical = if (isOneUi9) 14.dp else 12.dp
            ),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header Row: App Icon, App Name, Timestamp, Queue badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (config.showAppIcon) {
                    EventLeadingBadge(
                        event = event,
                        iconSizeDp = (config.iconSizeDp + 6).coerceIn(24, 38),
                        accentColor = accentColor,
                        isOneUiSquircle = isOneUi9
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (config.showAppName) {
                            Text(
                                text = event.appName,
                                color = if (isOneUi9) accentColor else secondaryTextColor,
                                fontSize = (11.5f.sp * textScale),
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (isOneUi9) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(accentColor.copy(alpha = 0.16f))
                                    .padding(horizontal = 7.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "One UI 9",
                                    color = accentColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    if (config.showTimestamp) {
                        Text(
                            text = timeFormatted,
                            color = secondaryTextColor.copy(alpha = 0.75f),
                            fontSize = (10.sp * textScale)
                        )
                    }
                }
            }

            // Queue indicator & navigation when multiple notifications exist
            if (queueSize > 1) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = onPrev,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = stringResource(R.string.island_action_prev),
                            tint = primaryTextColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.island_queue_count, currentQueueIndex + 1, queueSize),
                        color = primaryTextColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    IconButton(
                        onClick = onNext,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = stringResource(R.string.island_action_next),
                            tint = primaryTextColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else {
                EventTrailingIndicator(
                    event = event,
                    accentColor = accentColor,
                    secondaryTextColor = secondaryTextColor
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Body: Title & Message with independent RTL/LTR text alignment
        Column(modifier = Modifier.fillMaxWidth()) {
            if (config.showTitle && event.title.isNotBlank()) {
                CompositionLocalProvider(LocalLayoutDirection provides titleDir) {
                    Text(
                        text = event.title,
                        color = primaryTextColor,
                        fontSize = (14.sp * textScale),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (config.showMessagePreview) {
                Spacer(modifier = Modifier.height(3.dp))
                if (hideMessageForPrivacy) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.island_sensitive_hidden),
                            color = secondaryTextColor,
                            fontSize = (12.sp * textScale),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else if (event.message.isNotBlank()) {
                    CompositionLocalProvider(LocalLayoutDirection provides msgDir) {
                        Text(
                            text = event.message,
                            color = secondaryTextColor,
                            fontSize = (12.5f.sp * textScale),
                            lineHeight = (17.sp * textScale),
                            maxLines = if (isOneUi9) 3 else 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action Bar: Open App + Dismiss
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (event.contentIntent != null) {
                Surface(
                    onClick = onOpenIntent,
                    shape = RoundedCornerShape(50),
                    color = accentColor.copy(alpha = 0.2f),
                    modifier = Modifier.testTag("island_open_action")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = stringResource(R.string.island_action_open),
                            color = accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Surface(
                onClick = onDismiss,
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.testTag("island_dismiss_action")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = primaryTextColor.copy(alpha = 0.85f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.island_action_dismiss),
                        color = primaryTextColor.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun EventLeadingBadge(
    event: IslandEventItem,
    iconSizeDp: Int,
    accentColor: Color,
    isOneUiSquircle: Boolean
) {
    val containerShape = if (isOneUiSquircle) RoundedCornerShape(12.dp) else CircleShape
    val badgeColor = when (event.eventType) {
        IslandEventType.INCOMING_CALL, IslandEventType.ONGOING_CALL -> Color(0xFF10B981)
        IslandEventType.CHARGING_BATTERY -> Color(0xFF38BDF8)
        IslandEventType.MUSIC_PLAYBACK -> Color(0xFFA78BFA)
        IslandEventType.TIMER_COUNTDOWN -> Color(0xFFF59E0B)
        else -> accentColor
    }

    Box(
        modifier = Modifier
            .size((iconSizeDp + 8).dp)
            .clip(containerShape)
            .background(badgeColor.copy(alpha = 0.18f))
            .border(1.dp, badgeColor.copy(alpha = 0.4f), containerShape),
        contentAlignment = Alignment.Center
    ) {
        val drawableBitmap = remember(event.appIcon) {
            event.appIcon?.toSafeBitmap()
        }
        if (drawableBitmap != null && event.eventType == IslandEventType.NOTIFICATION) {
            Image(
                bitmap = drawableBitmap.asImageBitmap(),
                contentDescription = event.appName,
                modifier = Modifier
                    .size(iconSizeDp.dp)
                    .clip(containerShape)
            )
        } else {
            val iconVector = when (event.eventType) {
                IslandEventType.INCOMING_CALL -> Icons.Default.Call
                IslandEventType.ONGOING_CALL -> Icons.Default.PhoneInTalk
                IslandEventType.CHARGING_BATTERY -> Icons.Default.BatteryChargingFull
                IslandEventType.MUSIC_PLAYBACK -> Icons.Default.MusicNote
                IslandEventType.TIMER_COUNTDOWN -> Icons.Default.Timer
                else -> Icons.Default.Notifications
            }
            Icon(
                imageVector = iconVector,
                contentDescription = event.appName,
                tint = badgeColor,
                modifier = Modifier.size((iconSizeDp * 0.78f).dp)
            )
        }
    }
}

@Composable
private fun EventTrailingIndicator(
    event: IslandEventItem,
    accentColor: Color,
    secondaryTextColor: Color
) {
    when (event.eventType) {
        IslandEventType.MUSIC_PLAYBACK,
        IslandEventType.INCOMING_CALL,
        IslandEventType.ONGOING_CALL -> {
            AudioWaveformBars(
                color = if (event.eventType == IslandEventType.MUSIC_PLAYBACK) accentColor else Color(0xFF10B981)
            )
        }

        IslandEventType.CHARGING_BATTERY -> {
            Text(
                text = "${event.batteryLevel}%",
                color = Color(0xFF34D399),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        IslandEventType.TIMER_COUNTDOWN -> {
            Text(
                text = event.timerRemainingText.ifBlank { "02:45" },
                color = Color(0xFFFBBF24),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        else -> {
            if (event.groupCount > 1) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.22f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${event.groupCount}",
                        color = accentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.85f))
                )
            }
        }
    }
}

@Composable
private fun AudioWaveformBars(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val bar1 by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(420), RepeatMode.Reverse),
        label = "b1"
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(360), RepeatMode.Reverse),
        label = "b2"
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "b3"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        modifier = Modifier.widthIn(min = 16.dp)
    ) {
        listOf(bar1, bar2, bar3).forEach { fraction ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((14 * fraction).dp)
                    .clip(RoundedCornerShape(50))
                    .background(color)
            )
        }
    }
}

private fun Drawable.toSafeBitmap(): Bitmap? {
    return try {
        if (this is BitmapDrawable && bitmap != null) {
            return bitmap
        }
        val w = intrinsicWidth.coerceIn(1, 128)
        val h = intrinsicHeight.coerceIn(1, 128)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        bmp
    } catch (_: Exception) {
        null
    }
}
