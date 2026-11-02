package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.IslandStateController
import com.example.model.IslandConfiguration
import com.example.model.IslandEventItem
import com.example.model.IslandEventType
import com.example.ui.components.FixedLivePreviewPanel
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun GeometryPositionScreen(
    config: IslandConfiguration,
    activeEvent: IslandEventItem?,
    isExpanded: Boolean,
    onUpdateGeometry: (
        compactWidthDp: Int?,
        compactHeightDp: Int?,
        expandedWidthDp: Int?,
        expandedHeightDp: Int?,
        offsetXDp: Int?,
        offsetYDp: Int?,
        cornerRadiusDp: Int?,
        outerMarginDp: Int?,
        iconSizeDp: Int?,
        textScalePercent: Int?,
        borderWidthDp: Float?,
        shadowElevationDp: Int?,
        opacityPercent: Int?,
        animationDurationMs: Int?,
        displayDurationSec: Int?
    ) -> Unit,
    onResetGeometry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val isAr = config.languageCode != "en"

    val previewEvent = activeEvent ?: remember(isAr) {
        IslandEventItem(
            key = "geometry_preview",
            packageName = "com.nexaisland.dynamicisland",
            appName = "Nexa Island",
            title = if (isAr) "معاينة الأبعاد الحية" else "Live Geometry Preview",
            message = if (isAr) "اضغط لتجربة الحجم المدمج والموسع" else "Tap to toggle compact and expanded geometry",
            eventType = IslandEventType.NOTIFICATION
        )
    }

    // Fixed parent layout: non-scrolling top Live Preview panel + independently scrollable settings section below
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        FixedLivePreviewPanel(
            config = config,
            previewEvent = previewEvent,
            isExpanded = isExpanded,
            onToggleExpand = { IslandStateController.toggleExpanded() }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("geometry_scroll_section"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.geometry_screen_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.geometry_screen_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    GeometryIntSlider(
                        label = stringResource(R.string.slider_compact_width),
                        value = config.compactWidthDp,
                        range = 96f..260f,
                        unit = "dp",
                        onValueChange = {
                            onUpdateGeometry(it, null, null, null, null, null, null, null, null, null, null, null, null, null, null)
                        }
                    )
                    GeometryIntSlider(
                        label = stringResource(R.string.slider_compact_height),
                        value = config.compactHeightDp,
                        range = 28f..64f,
                        unit = "dp",
                        onValueChange = {
                            onUpdateGeometry(null, it, null, null, null, null, null, null, null, null, null, null, null, null, null)
                        }
                    )
                    GeometryIntSlider(
                        label = stringResource(R.string.slider_expanded_width),
                        value = config.expandedWidthDp,
                        range = 260f..420f,
                        unit = "dp",
                        onValueChange = {
                            onUpdateGeometry(null, null, it, null, null, null, null, null, null, null, null, null, null, null, null)
                        }
                    )
                    GeometryIntSlider(
                        label = stringResource(R.string.slider_expanded_height),
                        value = config.expandedHeightDp,
                        range = 88f..220f,
                        unit = "dp",
                        onValueChange = {
                            onUpdateGeometry(null, null, null, it, null, null, null, null, null, null, null, null, null, null, null)
                        }
                    )
                    GeometryIntSlider(
                        label = stringResource(R.string.slider_offset_x),
                        value = config.offsetXDp,
                        range = -120f..120f,
                        unit = "dp",
                        onValueChange = {
                            onUpdateGeometry(null, null, null, null, it, null, null, null, null, null, null, null, null, null, null)
                        }
                    )
                    GeometryIntSlider(
                        label = stringResource(R.string.slider_offset_y),
                        value = config.offsetYDp,
                        range = 0f..80f,
                        unit = "dp",
                        onValueChange = {
                            onUpdateGeometry(null, null, null, null, null, it, null, null, null, null, null, null, null, null, null)
                        }
                    )
                    GeometryIntSlider(
                        label = stringResource(R.string.slider_corner_radius),
                        value = config.cornerRadiusDp,
                        range = 12f..48f,
                        unit = "dp",
                        onValueChange = {
                            onUpdateGeometry(null, null, null, null, null, null, it, null, null, null, null, null, null, null, null)
                        }
                    )
                    GeometryIntSlider(
                        label = stringResource(R.string.slider_outer_margin),
                        value = config.outerMarginDp,
                        range = 0f..32f,
                        unit = "dp",
                        onValueChange = {
                            onUpdateGeometry(null, null, null, null, null, null, null, it, null, null, null, null, null, null, null)
                        }
                    )
                    GeometryIntSlider(
                        label = stringResource(R.string.slider_icon_size),
                        value = config.iconSizeDp,
                        range = 16f..40f,
                        unit = "dp",
                        onValueChange = {
                            onUpdateGeometry(null, null, null, null, null, null, null, null, it, null, null, null, null, null, null)
                        }
                    )
                    GeometryIntSlider(
                        label = stringResource(R.string.slider_text_scale),
                        value = config.textScalePercent,
                        range = 80f..130f,
                        unit = "%",
                        onValueChange = {
                            onUpdateGeometry(null, null, null, null, null, null, null, null, null, it, null, null, null, null, null)
                        }
                    )
                    GeometryFloatSlider(
                        label = stringResource(R.string.slider_border_width),
                        value = config.borderWidthDp,
                        range = 0f..4f,
                        unit = "dp",
                        onValueChange = {
                            onUpdateGeometry(null, null, null, null, null, null, null, null, null, null, it, null, null, null, null)
                        }
                    )
                    GeometryIntSlider(
                        label = stringResource(R.string.slider_shadow_elevation),
                        value = config.shadowElevationDp,
                        range = 0f..24f,
                        unit = "dp",
                        onValueChange = {
                            onUpdateGeometry(null, null, null, null, null, null, null, null, null, null, null, it, null, null, null)
                        }
                    )
                    GeometryIntSlider(
                        label = stringResource(R.string.slider_opacity),
                        value = config.opacityPercent,
                        range = 70f..100f,
                        unit = "%",
                        onValueChange = {
                            onUpdateGeometry(null, null, null, null, null, null, null, null, null, null, null, null, it, null, null)
                        }
                    )
                    GeometryIntSlider(
                        label = stringResource(R.string.slider_anim_duration),
                        value = config.animationDurationMs,
                        range = 120f..600f,
                        unit = "ms",
                        onValueChange = {
                            onUpdateGeometry(null, null, null, null, null, null, null, null, null, null, null, null, null, it, null)
                        }
                    )
                    GeometryIntSlider(
                        label = stringResource(R.string.slider_display_duration),
                        value = config.displayDurationSec,
                        range = 3f..15f,
                        unit = "s",
                        onValueChange = {
                            onUpdateGeometry(null, null, null, null, null, null, null, null, null, null, null, null, null, null, it)
                        }
                    )
                }
            }

            OutlinedButton(
                onClick = onResetGeometry,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_reset_geometry"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.btn_reset_geometry),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun GeometryIntSlider(
    label: String,
    value: Int,
    range: ClosedFloatingPointRange<Float>,
    unit: String,
    onValueChange: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "$value $unit",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.roundToInt()) },
            valueRange = range
        )
    }
}

@Composable
private fun GeometryFloatSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    unit: String,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = String.format(Locale.US, "%.1f %s", value, unit),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = { raw ->
                val roundedTenth = (raw * 10f).roundToInt() / 10f
                onValueChange(roundedTenth)
            },
            valueRange = range
        )
    }
}
