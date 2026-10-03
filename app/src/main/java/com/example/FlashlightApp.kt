package com.example

import android.app.Application
import com.example.data.SettingsRepository
import com.example.hardware.FlashlightManager

class FlashlightApp : Application() {

    lateinit var flashlightManager: FlashlightManager
        private set

    lateinit var settingsRepository: SettingsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        flashlightManager = FlashlightManager.getInstance(this)
        settingsRepository = SettingsRepository.getInstance(this)

        // If user configured "auto turn on at launch"
        if (settingsRepository.settings.value.autoTurnOnOnLaunch) {
            flashlightManager.setTorch(true)
        }
    }
}
