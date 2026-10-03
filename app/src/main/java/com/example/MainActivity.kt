package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppScreen
import com.example.ui.FlashlightViewModel
import com.example.ui.screens.MainFlashlightScreen
import com.example.ui.screens.ScreenLightScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.FlashLightTheme

class MainActivity : ComponentActivity() {

    private val viewModel: FlashlightViewModel by viewModels {
        val app = application as FlashlightApp
        FlashlightViewModel.Factory(
            application = app,
            flashlightManager = app.flashlightManager,
            settingsRepository = app.settingsRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val context = LocalContext.current

            // Keep screen on while flashlight or screen light is on if enabled
            LaunchedEffect(uiState.isTorchOn, uiState.isScreenLightActive, uiState.userSettings.keepScreenOn) {
                val shouldKeepScreenOn = uiState.userSettings.keepScreenOn &&
                        (uiState.isTorchOn || uiState.isScreenLightActive || uiState.isStrobeActive || uiState.isSosActive)
                if (shouldKeepScreenOn) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            // Window brightness for screen light mode
            LaunchedEffect(uiState.isScreenLightActive, uiState.screenLightBrightness) {
                val layoutParams = window.attributes
                if (uiState.isScreenLightActive) {
                    layoutParams.screenBrightness = uiState.screenLightBrightness.coerceIn(0.2f, 1.0f)
                } else {
                    layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                }
                window.attributes = layoutParams
            }

            FlashLightTheme(
                isOledMode = uiState.userSettings.oledPureBlack,
                forceDark = true
            ) {
                when {
                    uiState.isScreenLightActive -> {
                        ScreenLightScreen(
                            brightness = uiState.screenLightBrightness,
                            selectedColorHex = uiState.screenLightColorHex,
                            onBrightnessChange = { viewModel.setScreenLightBrightness(it) },
                            onColorSelect = { viewModel.setScreenLightColor(it) },
                            onClose = { viewModel.setScreenLight(false) }
                        )
                    }

                    uiState.currentScreen == AppScreen.SETTINGS -> {
                        SettingsScreen(
                            userSettings = uiState.userSettings,
                            onHapticChange = { viewModel.updateHapticSetting(it) },
                            onOledChange = { viewModel.updateOledSetting(it) },
                            onAutoLaunchChange = { viewModel.updateAutoLaunchSetting(it) },
                            onKeepScreenOnChange = { viewModel.updateKeepScreenOnSetting(it) },
                            onDefaultTimerChange = { viewModel.updateDefaultTimerSetting(it) },
                            onBack = { viewModel.navigateTo(AppScreen.MAIN) }
                        )
                    }

                    else -> {
                        MainFlashlightScreen(
                            uiState = uiState,
                            onToggleTorch = { viewModel.toggleFlashlight(context) },
                            onToggleStrobe = { viewModel.toggleStrobe(context) },
                            onStrobeHzChange = { viewModel.setStrobeHz(it) },
                            onToggleSos = { viewModel.toggleSos(context) },
                            onSelectTimerMinutes = { viewModel.setAutoOffTimer(it) },
                            onCancelTimer = { viewModel.cancelAutoOffTimer() },
                            onOpenScreenLight = { viewModel.toggleScreenLight() },
                            onOpenSettings = { viewModel.navigateTo(AppScreen.SETTINGS) },
                            onDismissError = { viewModel.dismissError() }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshBattery()
    }
}
