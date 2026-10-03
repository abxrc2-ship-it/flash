package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.SettingsRepository
import com.example.data.UserSettings
import com.example.hardware.FlashlightManager
import com.example.util.DeviceCapabilityChecker
import com.example.util.HapticFeedbackHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    MAIN,
    SETTINGS,
    SCREEN_LIGHT
}

data class FlashlightUiState(
    val isTorchOn: Boolean = false,
    val isFlashAvailable: Boolean = true,
    val isStrobeActive: Boolean = false,
    val strobeHz: Float = 4f,
    val isSosActive: Boolean = false,
    val isScreenLightActive: Boolean = false,
    val screenLightBrightness: Float = 1.0f,
    val screenLightColorHex: Long = 0xFFFFFDF5,
    val timerRemainingSeconds: Int? = null,
    val batteryLevel: Int = -1,
    val errorMessage: String? = null,
    val userSettings: UserSettings = UserSettings(),
    val currentScreen: AppScreen = AppScreen.MAIN
)

class FlashlightViewModel(
    application: Application,
    private val flashlightManager: FlashlightManager,
    private val settingsRepository: SettingsRepository
) : AndroidViewModel(application) {

    private val _isScreenLightActive = MutableStateFlow(false)
    private val _currentScreen = MutableStateFlow(AppScreen.MAIN)
    private val _batteryLevel = MutableStateFlow(DeviceCapabilityChecker.getBatteryLevel(application))

    val uiState: StateFlow<FlashlightUiState> = combine(
        flashlightManager.isTorchOn,
        flashlightManager.isStrobeActive,
        flashlightManager.strobeHz,
        flashlightManager.isSosActive,
        flashlightManager.errorMessage,
        flashlightManager.timerRemainingSeconds,
        settingsRepository.settings,
        _isScreenLightActive,
        _currentScreen,
        _batteryLevel
    ) { params ->
        val isTorchOn = params[0] as Boolean
        val isStrobe = params[1] as Boolean
        val strobeHz = params[2] as Float
        val isSos = params[3] as Boolean
        val errorMsg = params[4] as String?
        val timerSec = params[5] as Int?
        val settings = params[6] as UserSettings
        val screenLight = params[7] as Boolean
        val screen = params[8] as AppScreen
        val battery = params[9] as Int

        FlashlightUiState(
            isTorchOn = isTorchOn,
            isFlashAvailable = flashlightManager.isFlashAvailable,
            isStrobeActive = isStrobe,
            strobeHz = strobeHz,
            isSosActive = isSos,
            isScreenLightActive = screenLight,
            screenLightBrightness = settings.screenLightBrightness,
            screenLightColorHex = settings.screenLightColorHex,
            timerRemainingSeconds = timerSec,
            batteryLevel = battery,
            errorMessage = errorMsg,
            userSettings = settings,
            currentScreen = screen
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FlashlightUiState(
            isFlashAvailable = flashlightManager.isFlashAvailable,
            batteryLevel = DeviceCapabilityChecker.getBatteryLevel(application)
        )
    )

    fun toggleFlashlight(context: Context) {
        if (uiState.value.userSettings.hapticEnabled) {
            HapticFeedbackHelper.performClickHaptic(context)
        }
        flashlightManager.toggleTorch()
    }

    fun setTorch(enabled: Boolean, context: Context) {
        if (uiState.value.userSettings.hapticEnabled) {
            HapticFeedbackHelper.performClickHaptic(context)
        }
        flashlightManager.setTorch(enabled)
    }

    fun toggleStrobe(context: Context) {
        if (uiState.value.userSettings.hapticEnabled) {
            HapticFeedbackHelper.performClickHaptic(context)
        }
        flashlightManager.toggleStrobe()
    }

    fun setStrobeHz(hz: Float) {
        flashlightManager.setStrobeFrequency(hz)
    }

    fun toggleSos(context: Context) {
        if (uiState.value.userSettings.hapticEnabled) {
            HapticFeedbackHelper.performHeavyHaptic(context)
        }
        flashlightManager.toggleSos()
    }

    fun stopSpecialModes(context: Context) {
        if (uiState.value.userSettings.hapticEnabled) {
            HapticFeedbackHelper.performClickHaptic(context)
        }
        flashlightManager.stopSpecialMode()
    }

    fun setAutoOffTimer(minutes: Int) {
        flashlightManager.startAutoOffTimer(minutes)
    }

    fun cancelAutoOffTimer() {
        flashlightManager.cancelAutoOffTimer()
    }

    fun toggleScreenLight() {
        _isScreenLightActive.value = !_isScreenLightActive.value
    }

    fun setScreenLight(active: Boolean) {
        _isScreenLightActive.value = active
    }

    fun setScreenLightColor(colorHex: Long) {
        settingsRepository.setScreenLightColor(colorHex)
    }

    fun setScreenLightBrightness(brightness: Float) {
        settingsRepository.setScreenLightBrightness(brightness)
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun refreshBattery() {
        _batteryLevel.value = DeviceCapabilityChecker.getBatteryLevel(getApplication())
    }

    fun dismissError() {
        flashlightManager.clearError()
    }

    fun updateHapticSetting(enabled: Boolean) {
        settingsRepository.setHapticEnabled(enabled)
    }

    fun updateOledSetting(enabled: Boolean) {
        settingsRepository.setOledPureBlack(enabled)
    }

    fun updateAutoLaunchSetting(enabled: Boolean) {
        settingsRepository.setAutoTurnOnOnLaunch(enabled)
    }

    fun updateKeepScreenOnSetting(enabled: Boolean) {
        settingsRepository.setKeepScreenOn(enabled)
    }

    fun updateDefaultTimerSetting(minutes: Int) {
        settingsRepository.setAutoOffMinutes(minutes)
    }

    class Factory(
        private val application: Application,
        private val flashlightManager: FlashlightManager,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FlashlightViewModel::class.java)) {
                return FlashlightViewModel(application, flashlightManager, settingsRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
