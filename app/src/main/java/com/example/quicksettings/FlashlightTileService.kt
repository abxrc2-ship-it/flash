package com.example.quicksettings

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.example.R
import com.example.hardware.FlashlightManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FlashlightTileService : TileService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var stateCollectJob: Job? = null
    private lateinit var flashlightManager: FlashlightManager

    override fun onCreate() {
        super.onCreate()
        flashlightManager = FlashlightManager.getInstance(this)
    }

    override fun onStartListening() {
        super.onStartListening()
        stateCollectJob?.cancel()
        stateCollectJob = serviceScope.launch {
            flashlightManager.isTorchOn.collect { isTorchOn ->
                updateTileState(isTorchOn)
            }
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        stateCollectJob?.cancel()
        stateCollectJob = null
    }

    override fun onClick() {
        super.onClick()
        val newState = !flashlightManager.isTorchOn.value
        flashlightManager.setTorch(newState)
        updateTileState(newState)
    }

    private fun updateTileState(isTorchOn: Boolean) {
        val tile = qsTile ?: return
        tile.state = if (isTorchOn) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(R.string.quick_settings_tile_label)
        tile.contentDescription = if (isTorchOn) {
            getString(R.string.flashlight_on)
        } else {
            getString(R.string.flashlight_off)
        }
        tile.icon = Icon.createWithResource(this, R.drawable.ic_flashlight_tile)
        tile.updateTile()
    }

    override fun onDestroy() {
        super.onDestroy()
        stateCollectJob?.cancel()
    }
}
