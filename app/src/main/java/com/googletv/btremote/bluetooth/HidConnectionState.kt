package com.googletv.btremote.bluetooth

import android.bluetooth.BluetoothDevice

/**
 * Represents the connection status of the Bluetooth HID device profile.
 */
sealed interface HidConnectionState {
    /** HID profile is disconnected and idle. */
    object Disconnected : HidConnectionState

    /** Registering HID app SDP settings with Android OS Bluetooth service. */
    object Registering : HidConnectionState

    /** Connecting to a target host device (Chromecast / Google TV). */
    data class Connecting(val deviceName: String?) : HidConnectionState

    /** Actively connected to host device and ready to send HID reports. */
    data class Connected(
        val device: BluetoothDevice,
        val deviceName: String,
        val deviceAddress: String
    ) : HidConnectionState

    /** An error occurred or Bluetooth is disabled / unavailable. */
    data class Error(val message: String) : HidConnectionState
}

