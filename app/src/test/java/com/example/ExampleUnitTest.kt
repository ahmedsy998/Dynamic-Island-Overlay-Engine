package com.example

import androidx.compose.ui.unit.LayoutDirection
import com.example.data.IslandStateController
import com.example.model.ColorPalettePreset
import com.example.model.IslandConfiguration
import com.example.model.IslandEventItem
import com.example.model.IslandEventType
import com.example.model.NotificationStylePreset
import com.example.util.TextDirectionHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExampleUnitTest {

    @Before
    fun setUp() {
        IslandStateController.clearAllEvents()
    }

    @Test
    fun `default configuration is Arabic-first with One UI 9 style`() {
        val config = IslandConfiguration()
        assertEquals("ar", config.languageCode)
        assertEquals(NotificationStylePreset.SAMSUNG_ONE_UI_9, config.stylePreset)
        assertFalse(config.masterEnabled)
    }

    @Test
    fun `all 7 notification styles and 6 color palettes resolve accurately`() {
        assertEquals(7, NotificationStylePreset.entries.size)
        assertEquals(
            NotificationStylePreset.SAMSUNG_ONE_UI_9,
            NotificationStylePreset.fromId("samsung_one_ui_9")
        )
        assertEquals(
            NotificationStylePreset.MINIMAL_BLACK_PILL,
            NotificationStylePreset.fromId("minimal_black_pill")
        )
        assertEquals(
            ColorPalettePreset.EMERALD,
            ColorPalettePreset.fromId("emerald")
        )
    }

    @Test
    fun `notification updates with same key do not create duplicates`() {
        val now = System.currentTimeMillis()
        val first = IslandEventItem(
            key = "whatsapp_msg_1",
            packageName = "com.whatsapp",
            appName = "WhatsApp",
            title = "أحمد",
            message = "السلام عليكم",
            timestamp = now
        )
        val updated = first.copy(
            message = "السلام عليكم ورحمة الله",
            timestamp = now + 3000L
        )

        IslandStateController.postOrUpdateEvent(first, groupSameApp = true)
        IslandStateController.postOrUpdateEvent(updated, groupSameApp = true)

        val queue = IslandStateController.eventQueue.value
        assertEquals(1, queue.size)
        assertEquals("السلام عليكم ورحمة الله", queue.first().message)
    }

    @Test
    fun `grouping notifications from same app increments groupCount`() {
        val now = System.currentTimeMillis()
        val msg1 = IslandEventItem(
            key = "wa_1",
            packageName = "com.whatsapp",
            appName = "WhatsApp",
            title = "سارة",
            message = "الرسالة الأولى",
            timestamp = now
        )
        val msg2 = IslandEventItem(
            key = "wa_2",
            packageName = "com.whatsapp",
            appName = "WhatsApp",
            title = "سارة",
            message = "الرسالة الثانية",
            timestamp = now + 3000L
        )

        IslandStateController.postOrUpdateEvent(msg1, groupSameApp = true)
        IslandStateController.postOrUpdateEvent(msg2, groupSameApp = true)

        val queue = IslandStateController.eventQueue.value
        assertEquals(1, queue.size)
        assertEquals(2, queue.first().groupCount)
        assertEquals("الرسالة الثانية", queue.first().message)
    }

    @Test
    fun `Arabic RTL and English LTR notification text directions are detected independently`() {
        val arabicDir = TextDirectionHelper.detectTextLayoutDirection("مرحباً بك في Nexa Island")
        val englishDir = TextDirectionHelper.detectTextLayoutDirection("Hello from WhatsApp")

        assertEquals(LayoutDirection.Rtl, arabicDir)
        assertEquals(LayoutDirection.Ltr, englishDir)
        assertTrue(TextDirectionHelper.isRtlText("إشعار جديد"))
        assertFalse(TextDirectionHelper.isRtlText("Incoming Call"))
    }

    @Test
    fun `device lock state collapses expanded island for privacy`() {
        val event = IslandEventItem(
            key = "call_1",
            packageName = "com.android.dialer",
            appName = "Phone",
            title = "Incoming Call",
            message = "Mobile",
            eventType = IslandEventType.INCOMING_CALL
        )
        IslandStateController.postOrUpdateEvent(event, autoExpand = true)
        assertTrue(IslandStateController.isExpanded.value)

        IslandStateController.setDeviceLocked(true)
        assertFalse(IslandStateController.isExpanded.value)
    }
}
