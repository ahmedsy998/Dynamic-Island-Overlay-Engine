package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.IslandPreferencesRepository
import com.example.data.nexaDataStore
import com.example.service.NexaOverlayService
import com.example.util.SystemPermissionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val appContext = context?.applicationContext ?: return
        val action = intent?.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_LOCKED_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val repo = IslandPreferencesRepository(appContext.nexaDataStore)
                val config = repo.getSnapshot()
                if (config.masterEnabled && config.restoreOnBoot) {
                    if (SystemPermissionHelper.hasNotificationListenerPermission(appContext)) {
                        SystemPermissionHelper.requestListenerRebind(appContext)
                    }
                    if (SystemPermissionHelper.hasOverlayPermission(appContext)) {
                        NexaOverlayService.start(appContext)
                    }
                }
            } catch (_: Exception) {
            } finally {
                pendingResult.finish()
            }
        }
    }
}
