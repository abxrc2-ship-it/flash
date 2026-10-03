package com.example.hardware

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FlashlightManager private constructor(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var cameraIdWithFlash: String? = null
    var isFlashAvailable: Boolean = false
        private set

    private val _isTorchOn = MutableStateFlow(false)
    val isTorchOn: StateFlow<Boolean> = _isTorchOn.asStateFlow()

    private val _isStrobeActive = MutableStateFlow(false)
    val isStrobeActive: StateFlow<Boolean> = _isStrobeActive.asStateFlow()

    private val _strobeHz = MutableStateFlow(4f)
    val strobeHz: StateFlow<Float> = _strobeHz.asStateFlow()

    private val _isSosActive = MutableStateFlow(false)
    val isSosActive: StateFlow<Boolean> = _isSosActive.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _timerRemainingSeconds = MutableStateFlow<Int?>(null)
    val timerRemainingSeconds: StateFlow<Int?> = _timerRemainingSeconds.asStateFlow()

    private var strobeJob: Job? = null
    private var sosJob: Job? = null
    private var autoOffJob: Job? = null

    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            if (cameraId == cameraIdWithFlash) {
                _isTorchOn.value = enabled
                if (!enabled && (_isStrobeActive.value || _isSosActive.value)) {
                    // If hardware torch was turned off externally, clean up special jobs
                    if (strobeJob?.isActive != true && sosJob?.isActive != true) {
                        _isStrobeActive.value = false
                        _isSosActive.value = false
                    }
                }
            }
        }

        override fun onTorchModeUnavailable(cameraId: String) {
            if (cameraId == cameraIdWithFlash) {
                Log.w(TAG, "Torch mode unavailable for camera $cameraId")
                // Could be in use by camera app
            }
        }
    }

    init {
        detectFlashlight()
        registerTorchCallback()
    }

    private fun detectFlashlight() {
        val hasFeature = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
        if (cameraManager == null) {
            isFlashAvailable = false
            return
        }

        try {
            val cameraIds = cameraManager.cameraIdList
            for (id in cameraIds) {
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    cameraIdWithFlash = id
                    isFlashAvailable = true
                    break
                } else if (hasFlash && cameraIdWithFlash == null) {
                    cameraIdWithFlash = id
                    isFlashAvailable = true
                }
            }
            if (cameraIdWithFlash == null && hasFeature && cameraIds.isNotEmpty()) {
                cameraIdWithFlash = cameraIds[0]
                isFlashAvailable = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting camera flash", e)
            isFlashAvailable = false
        }
    }

    private fun registerTorchCallback() {
        try {
            cameraManager?.registerTorchCallback(torchCallback, Handler(Looper.getMainLooper()))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register torch callback", e)
        }
    }

    fun toggleTorch(): Boolean {
        return setTorch(!_isTorchOn.value)
    }

    fun setTorch(enabled: Boolean): Boolean {
        stopStrobeAndSos()

        if (!isFlashAvailable || cameraIdWithFlash == null) {
            _errorMessage.value = "No flashlight available on this device."
            return false
        }

        return try {
            cameraManager?.setTorchMode(cameraIdWithFlash!!, enabled)
            _isTorchOn.value = enabled
            _errorMessage.value = null

            if (enabled) {
                checkAutoOffTimer()
            } else {
                cancelAutoOffTimer()
            }
            true
        } catch (e: CameraAccessException) {
            Log.e(TAG, "CameraAccessException when setting torch", e)
            _errorMessage.value = "Unable to control the flashlight. Please try again."
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error setting torch mode", e)
            _errorMessage.value = "Unable to control the flashlight. Please try again."
            false
        }
    }

    fun setStrobeFrequency(hz: Float) {
        val clamped = hz.coerceIn(1f, 10f)
        _strobeHz.value = clamped
        if (_isStrobeActive.value) {
            // Restart strobe with new frequency
            startStrobe(clamped)
        }
    }

    fun toggleStrobe() {
        if (_isStrobeActive.value) {
            stopSpecialMode()
        } else {
            startStrobe(_strobeHz.value)
        }
    }

    fun startStrobe(hz: Float) {
        if (!isFlashAvailable || cameraIdWithFlash == null) {
            _errorMessage.value = "No flashlight available on this device."
            return
        }

        stopStrobeAndSos()
        _isStrobeActive.value = true
        _strobeHz.value = hz

        strobeJob = scope.launch(Dispatchers.Default) {
            var state = false
            while (isActive) {
                val cycleMs = (1000f / hz).toLong()
                val halfCycle = (cycleMs / 2).coerceAtLeast(40L)
                state = !state
                rawSetTorch(state)
                delay(halfCycle)
            }
        }
    }

    fun toggleSos() {
        if (_isSosActive.value) {
            stopSpecialMode()
        } else {
            startSos()
        }
    }

    fun startSos() {
        if (!isFlashAvailable || cameraIdWithFlash == null) {
            _errorMessage.value = "No flashlight available on this device."
            return
        }

        stopStrobeAndSos()
        _isSosActive.value = true

        sosJob = scope.launch(Dispatchers.Default) {
            // SOS in Morse code:
            // S: 3 dots (100ms on, 100ms off)
            // O: 3 dashes (300ms on, 100ms off)
            // S: 3 dots (100ms on, 100ms off)
            // Followed by 1000ms pause
            val dot = 150L
            val dash = 450L
            val intraChar = 150L
            val interChar = 450L
            val wordSpace = 1200L

            while (isActive) {
                // S (...)
                repeat(3) {
                    rawSetTorch(true); delay(dot)
                    rawSetTorch(false); delay(intraChar)
                }
                delay(interChar)

                // O (---)
                repeat(3) {
                    rawSetTorch(true); delay(dash)
                    rawSetTorch(false); delay(intraChar)
                }
                delay(interChar)

                // S (...)
                repeat(3) {
                    rawSetTorch(true); delay(dot)
                    rawSetTorch(false); delay(intraChar)
                }
                delay(wordSpace)
            }
        }
    }

    fun stopSpecialMode() {
        stopStrobeAndSos()
        rawSetTorch(false)
        _isTorchOn.value = false
    }

    private fun stopStrobeAndSos() {
        strobeJob?.cancel()
        strobeJob = null
        sosJob?.cancel()
        sosJob = null
        _isStrobeActive.value = false
        _isSosActive.value = false
    }

    private fun rawSetTorch(enabled: Boolean) {
        val id = cameraIdWithFlash ?: return
        try {
            cameraManager?.setTorchMode(id, enabled)
            _isTorchOn.value = enabled
        } catch (_: Exception) {
            // Transient strobe delay or access limitation
        }
    }

    fun startAutoOffTimer(minutes: Int) {
        autoOffJob?.cancel()
        if (minutes <= 0) {
            _timerRemainingSeconds.value = null
            return
        }

        val totalSeconds = minutes * 60
        _timerRemainingSeconds.value = totalSeconds

        autoOffJob = scope.launch {
            var remaining = totalSeconds
            while (remaining > 0 && isActive) {
                delay(1000L)
                remaining--
                _timerRemainingSeconds.value = remaining
            }
            if (isActive) {
                setTorch(false)
                _timerRemainingSeconds.value = null
            }
        }
    }

    fun cancelAutoOffTimer() {
        autoOffJob?.cancel()
        autoOffJob = null
        _timerRemainingSeconds.value = null
    }

    private fun checkAutoOffTimer() {
        // Maintained if timer was already configured or user set duration
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun turnOffAll() {
        stopStrobeAndSos()
        rawSetTorch(false)
        _isTorchOn.value = false
        cancelAutoOffTimer()
    }

    companion object {
        private const val TAG = "FlashlightManager"

        @Volatile
        private var instance: FlashlightManager? = null

        fun getInstance(context: Context): FlashlightManager {
            return instance ?: synchronized(this) {
                instance ?: FlashlightManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
