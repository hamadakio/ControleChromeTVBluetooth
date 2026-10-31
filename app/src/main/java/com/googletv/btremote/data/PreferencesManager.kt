package com.googletv.btremote.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages persistent preferences for the Google TV BT Remote,
 * including the MAC address of the last connected device and preferred navigation mode.
 */
class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var lastConnectedAddress: String?
        get() = prefs.getString(KEY_LAST_MAC, null)
        set(value) {
            prefs.edit().putString(KEY_LAST_MAC, value).apply()
        }

    var lastConnectedName: String?
        get() = prefs.getString(KEY_LAST_NAME, null)
        set(value) {
            prefs.edit().putString(KEY_LAST_NAME, value).apply()
        }

    var remoteMode: String
        get() = prefs.getString(KEY_REMOTE_MODE, MODE_DPAD) ?: MODE_DPAD
        set(value) {
            prefs.edit().putString(KEY_REMOTE_MODE, value).apply()
        }

    fun clearLastDevice() {
        prefs.edit().remove(KEY_LAST_MAC).remove(KEY_LAST_NAME).apply()
    }

    companion object {
        private const val PREFS_NAME = "google_tv_bt_remote_prefs"
        private const val KEY_LAST_MAC = "last_connected_mac"
        private const val KEY_LAST_NAME = "last_connected_name"
        private const val KEY_REMOTE_MODE = "remote_mode"

        const val MODE_DPAD = "dpad"
        const val MODE_TOUCHPAD = "touchpad"
    }
}

