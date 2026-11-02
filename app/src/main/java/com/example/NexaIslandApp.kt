package com.example

import android.app.Application
import com.example.data.IslandPreferencesRepository
import com.example.data.nexaDataStore
import com.example.util.SystemPermissionHelper
import com.example.util.TextDirectionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NexaIslandApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    lateinit var preferencesRepository: IslandPreferencesRepository
        private set

    override fun onCreate() {
        super.onCreate()
        preferencesRepository = IslandPreferencesRepository(applicationContext.nexaDataStore)

        appScope.launch {
            val config = preferencesRepository.getSnapshot()
            TextDirectionHelper.createLocalizedContext(this@NexaIslandApp, config.languageCode)
            if (config.masterEnabled) {
                SystemPermissionHelper.syncOverlayServiceState(
                    context = this@NexaIslandApp,
                    masterEnabled = true
                )
            }
        }
    }
}
