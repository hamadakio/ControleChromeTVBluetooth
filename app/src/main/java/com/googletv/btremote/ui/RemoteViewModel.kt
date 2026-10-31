package com.googletv.btremote.ui

import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import com.googletv.btremote.bluetooth.BluetoothHidController
import com.googletv.btremote.bluetooth.HidConnectionState
import com.googletv.btremote.data.BluetoothHidDescriptor
import com.googletv.btremote.data.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class RemoteMode {
    DPAD,
    TOUCHPAD
}

/**
 * ViewModel managing Google TV Remote UI state, remote mode selection,
 * discovery of nearby TVs, and dispatching HID commands through BluetoothHidController.
 */
class RemoteViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application.applicationContext)
    val hidController = BluetoothHidController(application.applicationContext, preferencesManager)

    val connectionState: StateFlow<HidConnectionState> = hidController.connectionState
    val scannedDevices: StateFlow<List<BluetoothDevice>> = hidController.scannedDevices
    val isScanning: StateFlow<Boolean> = hidController.isScanning

    private val _remoteMode = MutableStateFlow(
        if (preferencesManager.remoteMode == PreferencesManager.MODE_TOUCHPAD) {
            RemoteMode.TOUCHPAD
        } else {
            RemoteMode.DPAD
        }
    )
    val remoteMode: StateFlow<RemoteMode> = _remoteMode.asStateFlow()

    private val _bondedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val bondedDevices: StateFlow<List<BluetoothDevice>> = _bondedDevices.asStateFlow()

    init {
        hidController.initialize()
        refreshBondedDevices()
    }

    fun onResume() {
        hidController.onResume()
        refreshBondedDevices()
    }

    fun refreshBondedDevices() {
        _bondedDevices.value = hidController.getBondedDevices()
    }

    fun startScanning() {
        hidController.startDiscovery()
    }

    fun stopScanning() {
        hidController.stopDiscovery()
    }

    fun pairAndConnect(device: BluetoothDevice) {
        hidController.pairAndConnect(device)
    }

    fun setRemoteMode(mode: RemoteMode) {
        _remoteMode.value = mode
        preferencesManager.remoteMode = when (mode) {
            RemoteMode.DPAD -> PreferencesManager.MODE_DPAD
            RemoteMode.TOUCHPAD -> PreferencesManager.MODE_TOUCHPAD
        }
    }

    fun connectDevice(device: BluetoothDevice) {
        hidController.connect(device)
    }

    fun disconnectDevice() {
        hidController.disconnect()
    }

    // =========================================================================
    // D-Pad / Keyboard Navigation Commands (Report ID 1)
    // =========================================================================

    fun onDpadUp() {
        hidController.sendKey(
            BluetoothHidDescriptor.REPORT_ID_KEYBOARD.toInt(),
            BluetoothHidDescriptor.KEY_UP
        )
    }

    fun onDpadDown() {
        hidController.sendKey(
            BluetoothHidDescriptor.REPORT_ID_KEYBOARD.toInt(),
            BluetoothHidDescriptor.KEY_DOWN
        )
    }

    fun onDpadLeft() {
        hidController.sendKey(
            BluetoothHidDescriptor.REPORT_ID_KEYBOARD.toInt(),
            BluetoothHidDescriptor.KEY_LEFT
        )
    }

    fun onDpadRight() {
        hidController.sendKey(
            BluetoothHidDescriptor.REPORT_ID_KEYBOARD.toInt(),
            BluetoothHidDescriptor.KEY_RIGHT
        )
    }

    fun onCenterClick() {
        hidController.sendKey(
            BluetoothHidDescriptor.REPORT_ID_KEYBOARD.toInt(),
            BluetoothHidDescriptor.KEY_ENTER
        )
    }

    // =========================================================================
    // System & Consumer Control Commands (Report ID 2)
    // =========================================================================

    fun onHome() {
        hidController.sendKey(
            BluetoothHidDescriptor.REPORT_ID_CONSUMER.toInt(),
            BluetoothHidDescriptor.CONSUMER_HOME
        )
    }

    fun onBack() {
        hidController.sendKey(
            BluetoothHidDescriptor.REPORT_ID_CONSUMER.toInt(),
            BluetoothHidDescriptor.CONSUMER_BACK
        )
    }

    fun onAssistant() {
        hidController.sendKey(
            BluetoothHidDescriptor.REPORT_ID_CONSUMER.toInt(),
            BluetoothHidDescriptor.CONSUMER_ASSISTANT
        )
    }

    fun onPower() {
        hidController.sendKey(
            BluetoothHidDescriptor.REPORT_ID_CONSUMER.toInt(),
            BluetoothHidDescriptor.CONSUMER_POWER
        )
    }

    fun onMute() {
        hidController.sendKey(
            BluetoothHidDescriptor.REPORT_ID_CONSUMER.toInt(),
            BluetoothHidDescriptor.CONSUMER_MUTE
        )
    }

    fun onVolumeUp() {
        hidController.sendKey(
            BluetoothHidDescriptor.REPORT_ID_CONSUMER.toInt(),
            BluetoothHidDescriptor.CONSUMER_VOLUME_UP
        )
    }

    fun onVolumeDown() {
        hidController.sendKey(
            BluetoothHidDescriptor.REPORT_ID_CONSUMER.toInt(),
            BluetoothHidDescriptor.CONSUMER_VOLUME_DOWN
        )
    }

    override fun onCleared() {
        super.onCleared()
        hidController.destroy()
    }
}
