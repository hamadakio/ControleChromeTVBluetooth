package com.googletv.btremote.data

/**
 * Composite HID Report Descriptor and keycode mappings for Google TV / Android TV.
 *
 * Supports two distinct Report IDs:
 * - Report ID 1: Standard HID Keyboard (Usage Page 0x01, Usage 0x06)
 *   Used for instantaneous directional navigation (D-Pad) and selection (Enter/OK).
 *
 * - Report ID 2: Consumer Control (Usage Page 0x0C, Usage 0x01)
 *   Used for Android TV system keys (Home, Back, Assistant, Volume, Mute, Power)
 *   encoded as 16-bit Little Endian usage codes.
 */
object BluetoothHidDescriptor {

    const val REPORT_ID_KEYBOARD: Byte = 1
    const val REPORT_ID_CONSUMER: Byte = 2

    // =========================================================================
    // Report ID 1: Keyboard Keycodes (USB HID Usage Table - Page 0x07)
    // =========================================================================
    const val KEY_ENTER = 0x28       // Keyboard Return (ENTER) / Select
    const val KEY_RIGHT = 0x4F       // Keyboard RightArrow
    const val KEY_LEFT = 0x50        // Keyboard LeftArrow
    const val KEY_DOWN = 0x51        // Keyboard DownArrow
    const val KEY_UP = 0x52          // Keyboard UpArrow

    // =========================================================================
    // Report ID 2: Consumer Control Usages (USB HID Usage Table - Page 0x0C)
    // =========================================================================
    const val CONSUMER_POWER = 0x0030          // Power / Sleep
    const val CONSUMER_MUTE = 0x00E2           // Mute
    const val CONSUMER_VOLUME_UP = 0x00E9      // Volume Increment
    const val CONSUMER_VOLUME_DOWN = 0x00EA    // Volume Decrement
    const val CONSUMER_ASSISTANT = 0x0221      // AC Search / Google Assistant
    const val CONSUMER_VOICE_COMMAND = 0x00CF  // Voice Search / Voice Command
    const val CONSUMER_HOME = 0x0223           // AC Home
    const val CONSUMER_BACK = 0x0224           // AC Back

    /**
     * Composite HID Report Descriptor bytes.
     * Compliant with USB HID 1.11 and Android Bluetooth HID profile.
     */
    val DESCRIPTOR: ByteArray = byteArrayOf(
        // ---------------------------------------------------------------------
        // Report ID 1: Standard Keyboard (8-byte Input Report)
        // [Modifier (1B), Reserved (1B), Key1..Key6 (6B)]
        // ---------------------------------------------------------------------
        0x05.toByte(), 0x01.toByte(),       // USAGE_PAGE (Generic Desktop)
        0x09.toByte(), 0x06.toByte(),       // USAGE (Keyboard)
        0xA1.toByte(), 0x01.toByte(),       // COLLECTION (Application)
        0x85.toByte(), 0x01.toByte(),       //   REPORT_ID (1)
        0x05.toByte(), 0x07.toByte(),       //   USAGE_PAGE (Keyboard/Keypad)
        0x19.toByte(), 0xE0.toByte(),       //   USAGE_MINIMUM (Keyboard LeftControl)
        0x29.toByte(), 0xE7.toByte(),       //   USAGE_MAXIMUM (Keyboard Right GUI)
        0x15.toByte(), 0x00.toByte(),       //   LOGICAL_MINIMUM (0)
        0x25.toByte(), 0x01.toByte(),       //   LOGICAL_MAXIMUM (1)
        0x75.toByte(), 0x01.toByte(),       //   REPORT_SIZE (1)
        0x95.toByte(), 0x08.toByte(),       //   REPORT_COUNT (8)
        0x81.toByte(), 0x02.toByte(),       //   INPUT (Data,Var,Abs) - Modifier byte
        0x95.toByte(), 0x01.toByte(),       //   REPORT_COUNT (1)
        0x75.toByte(), 0x08.toByte(),       //   REPORT_SIZE (8)
        0x81.toByte(), 0x03.toByte(),       //   INPUT (Cnst,Var,Abs) - Reserved byte
        0x95.toByte(), 0x06.toByte(),       //   REPORT_COUNT (6)
        0x75.toByte(), 0x08.toByte(),       //   REPORT_SIZE (8)
        0x15.toByte(), 0x00.toByte(),       //   LOGICAL_MINIMUM (0)
        0x25.toByte(), 0x65.toByte(),       //   LOGICAL_MAXIMUM (101)
        0x05.toByte(), 0x07.toByte(),       //   USAGE_PAGE (Keyboard/Keypad)
        0x19.toByte(), 0x00.toByte(),       //   USAGE_MINIMUM (0)
        0x29.toByte(), 0x65.toByte(),       //   USAGE_MAXIMUM (101)
        0x81.toByte(), 0x00.toByte(),       //   INPUT (Data,Ary,Abs) - 6 Key slots
        0xC0.toByte(),                      // END_COLLECTION

        // ---------------------------------------------------------------------
        // Report ID 2: Consumer Control (2-byte Little Endian 16-bit Input Report)
        // [UsageLow (1B), UsageHigh (1B)]
        // ---------------------------------------------------------------------
        0x05.toByte(), 0x0C.toByte(),       // USAGE_PAGE (Consumer Devices)
        0x09.toByte(), 0x01.toByte(),       // USAGE (Consumer Control)
        0xA1.toByte(), 0x01.toByte(),       // COLLECTION (Application)
        0x85.toByte(), 0x02.toByte(),       //   REPORT_ID (2)
        0x15.toByte(), 0x00.toByte(),       //   LOGICAL_MINIMUM (0)
        0x26.toByte(), 0xFF.toByte(), 0x03.toByte(), // LOGICAL_MAXIMUM (1023 / 0x03FF)
        0x19.toByte(), 0x00.toByte(),       //   USAGE_MINIMUM (0)
        0x2A.toByte(), 0xFF.toByte(), 0x03.toByte(), // USAGE_MAXIMUM (1023 / 0x03FF)
        0x75.toByte(), 0x10.toByte(),       //   REPORT_SIZE (16 bits)
        0x95.toByte(), 0x01.toByte(),       //   REPORT_COUNT (1)
        0x81.toByte(), 0x00.toByte(),       //   INPUT (Data,Ary,Abs)
        0xC0.toByte()                       // END_COLLECTION
    )

    /**
     * Builds the active press report payload for a given Report ID and key code.
     */
    fun createPressPayload(reportId: Int, keyCode: Int): ByteArray {
        return when (reportId) {
            REPORT_ID_KEYBOARD.toInt() -> {
                // 8 bytes: Modifier, Reserved, Key1, Key2..Key6
                byteArrayOf(0x00, 0x00, keyCode.toByte(), 0x00, 0x00, 0x00, 0x00, 0x00)
            }
            REPORT_ID_CONSUMER.toInt() -> {
                // 2 bytes Little Endian: Low Byte, High Byte
                byteArrayOf(
                    (keyCode and 0xFF).toByte(),
                    ((keyCode shr 8) and 0xFF).toByte()
                )
            }
            else -> ByteArray(0)
        }
    }

    /**
     * Builds the release report payload (all zeros) for a given Report ID.
     */
    fun createReleasePayload(reportId: Int): ByteArray {
        return when (reportId) {
            REPORT_ID_KEYBOARD.toInt() -> ByteArray(8)
            REPORT_ID_CONSUMER.toInt() -> ByteArray(2)
            else -> ByteArray(0)
        }
    }
}
