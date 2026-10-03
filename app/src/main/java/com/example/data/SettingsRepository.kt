package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserSettings(
    val hapticEnabled: Boolean = true,
    val oledPureBlack: Boolean = true,
    val autoTurnOnOnLaunch: Boolean = false,
    val keepScreenOn: Boolean = true,
    val autoOffMinutes: Int = 0, // 0 = Never
    val screenLightColorHex: Long = 0xFFFFFDF5, // Warm cozy light default
    val screenLightBrightness: Float = 1.0f
)

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private fun loadSettings(): UserSettings {
        return UserSettings(
            hapticEnabled = prefs.getBoolean(KEY_HAPTIC, true),
            oledPureBlack = prefs.getBoolean(KEY_OLED, true),
            autoTurnOnOnLaunch = prefs.getBoolean(KEY_AUTO_LAUNCH, false),
            keepScreenOn = prefs.getBoolean(KEY_KEEP_SCREEN_ON, true),
            autoOffMinutes = prefs.getInt(KEY_AUTO_OFF_MINUTES, 0),
            screenLightColorHex = prefs.getLong(KEY_SCREEN_COLOR, 0xFFFFFDF5),
            screenLightBrightness = prefs.getFloat(KEY_SCREEN_BRIGHTNESS, 1.0f)
        )
    }

    fun setHapticEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC, enabled).apply()
        _settings.value = _settings.value.copy(hapticEnabled = enabled)
    }

    fun setOledPureBlack(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_OLED, enabled).apply()
        _settings.value = _settings.value.copy(oledPureBlack = enabled)
    }

    fun setAutoTurnOnOnLaunch(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_LAUNCH, enabled).apply()
        _settings.value = _settings.value.copy(autoTurnOnOnLaunch = enabled)
    }

    fun setKeepScreenOn(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_KEEP_SCREEN_ON, enabled).apply()
        _settings.value = _settings.value.copy(keepScreenOn = enabled)
    }

    fun setAutoOffMinutes(minutes: Int) {
        prefs.edit().putInt(KEY_AUTO_OFF_MINUTES, minutes).apply()
        _settings.value = _settings.value.copy(autoOffMinutes = minutes)
    }

    fun setScreenLightColor(colorHex: Long) {
        prefs.edit().putLong(KEY_SCREEN_COLOR, colorHex).apply()
        _settings.value = _settings.value.copy(screenLightColorHex = colorHex)
    }

    fun setScreenLightBrightness(brightness: Float) {
        prefs.edit().putFloat(KEY_SCREEN_BRIGHTNESS, brightness).apply()
        _settings.value = _settings.value.copy(screenLightBrightness = brightness)
    }

    companion object {
        private const val PREFS_NAME = "flashlight_pro_prefs"
        private const val KEY_HAPTIC = "pref_haptic"
        private const val KEY_OLED = "pref_oled"
        private const val KEY_AUTO_LAUNCH = "pref_auto_launch"
        private const val KEY_KEEP_SCREEN_ON = "pref_keep_screen_on"
        private const val KEY_AUTO_OFF_MINUTES = "pref_auto_off_minutes"
        private const val KEY_SCREEN_COLOR = "pref_screen_color"
        private const val KEY_SCREEN_BRIGHTNESS = "pref_screen_brightness"

        @Volatile
        private var instance: SettingsRepository? = null

        fun getInstance(context: Context): SettingsRepository {
            return instance ?: synchronized(this) {
                instance ?: SettingsRepository(context).also { instance = it }
            }
        }
    }
}
