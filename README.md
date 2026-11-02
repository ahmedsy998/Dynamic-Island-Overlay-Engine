# Nexa Island — الجزيرة التفاعلية المتكاملة لنظام أندرويد

**Nexa Island** (`com.nexaisland.dynamicisland`) is a complete, native Android Dynamic Island application built with **Kotlin**, **Jetpack Compose**, **Material 3**, **DataStore Preferences**, **NotificationListenerService**, and **WindowManager Overlay Services**.

Designed with an **Arabic-First RTL experience** (`العربية` default on first launch, with instant switching to `English`), **7 visual notification styles** including a dedicated **Samsung One UI 9-Inspired Style**, and specialized **Xiaomi HyperOS / Redmi Note 13 Pro 5G** background persistence diagnostics.

---

## Key Features

1. **Real Dynamic Island Overlay & Notification Listener**
   - Uses `NexaNotificationListenerService` (`android.service.notification.NotificationListenerService`) to receive real notifications from WhatsApp, Telegram, Messages, Dialer, Media players, and all installed apps.
   - Renders a real floating `ComposeView` window via `NexaOverlayService` (`WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY` with `LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS`).
   - Supports **8 Island States**: Idle pill, Compact notification pill, Expanded notification card, Incoming call, Ongoing call, Charging & battery status, Music playback waveform, and Active countdown timer.
   - Deduplicates notification updates in-place, groups multiple notifications from the same application, and supports tap-to-open (`PendingIntent`), expand/collapse, queue navigation, and horizontal swipe-to-dismiss.

2. **Mandatory Arabic-First Experience & Independent Notification Direction**
   - Opens in **Arabic (`العربية`) with full RTL layout** on first launch regardless of system locale.
   - Instant runtime language switching between **العربية** and **English** in Settings without losing user preferences.
   - Automatically detects the text direction of each notification's title and body (`TextDirectionHelper`) so Arabic notifications render RTL and English notifications render LTR independently—even inside the same notification card.

3. **7 Notification Styles — Including Samsung One UI 9-Inspired Preset**
   - **Samsung One UI 9-Inspired Style**: Stadium-to-squircle curvature, squircle app icon container, One UI header chip, generous padding, and smooth spring transitions.
   - **Minimal Black Pill**
   - **Premium Glass-Inspired Pill**
   - **Soft Gradient Pill**
   - **Rounded Notification Card**
   - **Compact Icon-Only Style**
   - **Expanded Notification Card**

4. **Xiaomi HyperOS & Redmi Note 13 Pro 5G Compatibility**
   - Dedicated **HyperOS & System** tab distinguishing programmatically inspectable permissions (`Settings.canDrawOverlays`, `enabled_notification_listeners`, `isIgnoringBatteryOptimizations`) from manual OEM settings (Autostart, Background Pop-up Windows, Lock-Screen display, Recents lock) with safe intent fallbacks.

---

## Building the APK

### Local Gradle Build
```bash
gradle :app:assembleDebug
```
The generated debug APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

### Running Automated Unit & Robolectric Tests
```bash
gradle :app:testDebugUnitTest
```

---

## Manual Acceptance Testing Checklist (Xiaomi Redmi Note 13 Pro 5G / HyperOS)

1. **First Launch Arabic RTL**: Install and open Nexa Island; verify the interface opens in Arabic with RTL alignment.
2. **Permissions Setup**: From the Dashboard checklist, grant **Display Over Other Apps** and **Notification Access**, and set Battery Saver to **No Restrictions**.
3. **Master Switch**: Toggle **تفعيل الجزيرة التفاعلية** ON; verify the floating Dynamic Island appears around the front camera cutout and all 6 diagnostic rows turn green.
4. **Real Notification Verification**: Tap **إرسال إشعار اختباري حقيقي** or send a real WhatsApp message; verify it appears in the island without duplicates, expands on tap, and opens the target app when clicking **فتح التطبيق**.
5. **Style Gallery & One UI 9 Preset**: Switch between all 7 styles in the **الأنماط (Styles)** tab and verify live updates.
6. **Lock-Screen Privacy**: Enable **إخفاء محتوى الرسائل الحساسة على شاشة القفل** in Settings and lock the screen; verify private message previews are masked.
