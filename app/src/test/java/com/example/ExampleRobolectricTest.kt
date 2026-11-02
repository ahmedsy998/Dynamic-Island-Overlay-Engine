package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.model.ServiceDiagnosticsState
import com.example.util.SystemPermissionHelper
import com.example.util.TextDirectionHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34, 36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `MainActivity launches DashboardScreen with localized context and ActivityResultRegistryOwner`() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("live_preview_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("master_enable_switch").assertIsDisplayed()

        // Navigate to Geometry customization tab and verify Fixed Live Preview Panel is pinned at top
        composeTestRule.onNodeWithTag("nav_tab_geometry").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("fixed_live_preview_panel").assertIsDisplayed()
        composeTestRule.onNodeWithTag("live_preview_label").assertIsDisplayed()
        composeTestRule.onNodeWithTag("geometry_scroll_section").assertIsDisplayed()

        // Navigate to Styles customization tab and verify Fixed Live Preview Panel is pinned at top
        composeTestRule.onNodeWithTag("nav_tab_styles").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("fixed_live_preview_panel").assertIsDisplayed()
        composeTestRule.onNodeWithTag("live_preview_label").assertIsDisplayed()
        composeTestRule.onNodeWithTag("styles_scroll_section").assertIsDisplayed()
    }

    @Test
    fun `app name and Arabic-first strings resolve accurately`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val arContext = TextDirectionHelper.createLocalizedContext(context, "ar")
        val enContext = TextDirectionHelper.createLocalizedContext(context, "en")

        assertEquals("Nexa Island", arContext.getString(R.string.app_name))
        assertTrue(arContext.getString(R.string.master_switch_title).contains("الجزيرة"))
        assertTrue(enContext.getString(R.string.master_switch_title).contains("Dynamic Island"))
    }

    @Test
    fun `safe diagnostic report exports status without notification content`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val diagnostics = ServiceDiagnosticsState(
            userPreferenceEnabled = true,
            overlayPermissionGranted = true,
            notificationListenerGranted = false,
            notificationListenerConnected = false,
            overlayServiceRunning = true,
            islandCurrentlyVisible = true,
            batteryOptimizationIgnored = false,
            isXiaomiOrHyperOS = true,
            deviceSummary = "Xiaomi Redmi Note 13 Pro 5G (Android 14)"
        )

        val report = SystemPermissionHelper.buildSafeDiagnosticReport(
            context = context,
            diagnostics = diagnostics,
            styleId = "samsung_one_ui_9",
            languageCode = "ar"
        )

        assertTrue(report.contains("samsung_one_ui_9"))
        assertTrue(report.contains("Xiaomi Redmi Note 13 Pro 5G"))
        assertFalse(report.contains("NotificationListenerConnected: true"))
        assertTrue(report.contains("No notification titles, messages, or personal data included."))
    }
}
