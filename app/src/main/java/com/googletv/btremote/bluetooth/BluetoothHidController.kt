package com.googletv.btremote.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.googletv.btremote.data.BluetoothHidDescriptor
import com.googletv.btremote.data.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

/**
 * Low-level Bluetooth HID profile controller for Google TV / Android TV.
 *
 * Implements BluetoothHidDevice registration with composite HID descriptor,
 * connection management, key press & release dispatching with 30ms delay,
 * discovery scanning for nearby TVs, and auto-reconnection to bonded TVs.
 */
class BluetoothHidController(
    private val context: Context,
    private val preferencesManager: PreferencesManager
) {

    private val tag = "BluetoothHidController"
    private val coroutineScope = CoroutineScope(Dispatchers.IO + Job())
    private val executor = Executors.newSingleThreadExecutor()

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private var hidDevice: BluetoothHidDevice? = null
    var isAppRegistered = false
        private set
    private var connectedDevice: BluetoothDevice? = null
    private var pendingDeviceToConnect: BluetoothDevice? = null

    private val _connectionState = MutableStateFlow<HidConnectionState>(HidConnectionState.Disconnected)
    val connectionState: StateFlow<HidConnectionState> = _connectionState.asStateFlow()

    private val _scannedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val scannedDevices: StateFlow<List<BluetoothDevice>> = _scannedDevices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private var isReceiverRegistered = false

    /**
     * BroadcastReceiver to discover nearby TVs during inquiry scanning
     * and handle pairing (bonding) state transitions.
     */
    private val discoveryReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(c: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }

                    if (device != null) {
                        val currentList = _scannedDevices.value
                        val alreadyBonded = getBondedDevices().any { it.address.equals(device.address, ignoreCase = true) }
                        val alreadyInList = currentList.any { it.address.equals(device.address, ignoreCase = true) }

                        if (!alreadyBonded && !alreadyInList) {
                            _scannedDevices.value = currentList + device
                        }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    _isScanning.value = true
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _isScanning.value = false
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE)
                    val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }

                    if (device != null && state == BluetoothDevice.BOND_BONDED) {
                        Log.i(tag, "Device bonded successfully: ${device.address}. Connecting HID...")
                        connect(device)
                    }
                }
            }
        }
    }

    /**
     * ServiceListener for BluetoothProfile.HID_DEVICE
     */
    private val serviceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                Log.d(tag, "Bluetooth HID Device profile proxy connected.")
                hidDevice = proxy as? BluetoothHidDevice
                registerHidApp()
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                Log.d(tag, "Bluetooth HID Device profile proxy disconnected.")
                hidDevice = null
                isAppRegistered = false
                connectedDevice = null
                _connectionState.value = HidConnectionState.Disconnected
            }
        }
    }

    /**
     * Callback for Bluetooth HID device events and state changes
     */
    private val hidCallback = object : BluetoothHidDevice.Callback() {
        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            super.onAppStatusChanged(pluggedDevice, registered)
            isAppRegistered = registered
            Log.d(tag, "HID onAppStatusChanged: registered=$registered, pluggedDevice=${pluggedDevice?.address}")

            if (registered) {
                // If there was a pending connect request waiting for registration
                val pending = pendingDeviceToConnect
                if (pending != null) {
                    pendingDeviceToConnect = null
                    connect(pending)
                } else if (pluggedDevice != null) {
                    onDeviceConnected(pluggedDevice)
                } else {
                    autoConnect()
                }
            } else {
                _connectionState.value = HidConnectionState.Disconnected
            }
        }

        override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
            super.onConnectionStateChanged(device, state)
            Log.d(tag, "HID onConnectionStateChanged: device=${device?.address}, state=$state")

            when (state) {
                BluetoothProfile.STATE_CONNECTED -> {
                    if (device != null) {
                        onDeviceConnected(device)
                    }
                }
                BluetoothProfile.STATE_CONNECTING -> {
                    val name = getDeviceName(device)
                    _connectionState.value = HidConnectionState.Connecting(name)
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    if (connectedDevice?.address == device?.address || connectedDevice == null) {
                        connectedDevice = null
                        _connectionState.value = HidConnectionState.Disconnected
                    }
                }
                BluetoothProfile.STATE_DISCONNECTING -> {
                    // In-flight disconnect
                }
            }
        }

        override fun onGetReport(device: BluetoothDevice?, type: Byte, id: Byte, bufferSize: Int) {
            super.onGetReport(device, type, id, bufferSize)
            Log.d(tag, "HID onGetReport: type=$type, id=$id, bufferSize=$bufferSize")
            if (hasConnectPermission() && device != null) {
                try {
                    // CRITICAL: Respond with EXACT report size to avoid Linux kernel -EINVAL report size mismatch!
                    val payload = when (id.toInt()) {
                        BluetoothHidDescriptor.REPORT_ID_KEYBOARD.toInt() -> ByteArray(8)
                        BluetoothHidDescriptor.REPORT_ID_CONSUMER.toInt() -> ByteArray(2)
                        else -> ByteArray(bufferSize.coerceAtLeast(0))
                    }
                    hidDevice?.replyReport(device, type, id, payload)
                } catch (e: SecurityException) {
                    Log.e(tag, "SecurityException in onGetReport", e)
                }
            }
        }

        override fun onSetReport(device: BluetoothDevice?, type: Byte, id: Byte, data: ByteArray?) {
            super.onSetReport(device, type, id, data)
            Log.d(tag, "HID onSetReport: type=$type, id=$id")
            if (hasConnectPermission() && device != null) {
                try {
                    hidDevice?.reportError(device, BluetoothHidDevice.ERROR_RSP_SUCCESS)
                } catch (e: SecurityException) {
                    Log.e(tag, "SecurityException in onSetReport", e)
                }
            }
        }

        override fun onSetProtocol(device: BluetoothDevice?, protocol: Byte) {
            super.onSetProtocol(device, protocol)
            if (hasConnectPermission() && device != null) {
                try {
                    hidDevice?.reportError(device, BluetoothHidDevice.ERROR_RSP_SUCCESS)
                } catch (e: SecurityException) {
                    Log.e(tag, "SecurityException in onSetProtocol", e)
                }
            }
        }

        override fun onInterruptData(device: BluetoothDevice?, reportId: Byte, data: ByteArray?) {
            super.onInterruptData(device, reportId, data)
            Log.d(tag, "HID onInterruptData: reportId=$reportId")
        }

        override fun onVirtualCableUnplug(device: BluetoothDevice?) {
            super.onVirtualCableUnplug(device)
            Log.d(tag, "HID onVirtualCableUnplug: device=${device?.address}")
            connectedDevice = null
            _connectionState.value = HidConnectionState.Disconnected
        }
    }

    /**
     * Initializes the HID controller and binds the HID profile proxy.
     */
    fun initialize() {
        registerDiscoveryReceiver()

        if (bluetoothAdapter == null) {
            _connectionState.value = HidConnectionState.Error("Bluetooth não suportado neste aparelho")
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            _connectionState.value = HidConnectionState.Error("Bluetooth desativado")
            return
        }

        try {
            _connectionState.value = HidConnectionState.Registering
            bluetoothAdapter.getProfileProxy(context, serviceListener, BluetoothProfile.HID_DEVICE)
        } catch (e: Exception) {
            Log.e(tag, "Failed to getProfileProxy", e)
            _connectionState.value = HidConnectionState.Error("Erro ao iniciar serviço HID: ${e.message}")
        }
    }

    private fun registerDiscoveryReceiver() {
        if (!isReceiverRegistered) {
            val filter = IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_FOUND)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            }
            ContextCompat.registerReceiver(
                context,
                discoveryReceiver,
                filter,
                ContextCompat.RECEIVER_EXPORTED
            )
            isReceiverRegistered = true
        }
    }

    /**
     * Starts Bluetooth discovery for nearby Google TV / Android TV devices.
     */
    @SuppressLint("MissingPermission")
    fun startDiscovery() {
        if (!hasConnectPermission() || bluetoothAdapter == null) return
        _scannedDevices.value = emptyList()

        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }
        bluetoothAdapter.startDiscovery()
    }

    /**
     * Stops Bluetooth discovery.
     */
    @SuppressLint("MissingPermission")
    fun stopDiscovery() {
        if (!hasConnectPermission() || bluetoothAdapter == null) return
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }
        _isScanning.value = false
    }

    /**
     * Initiates pairing with an unbonded device, then connects once bonded.
     */
    @SuppressLint("MissingPermission")
    fun pairAndConnect(device: BluetoothDevice) {
        if (!hasConnectPermission()) return
        stopDiscovery()

        if (device.bondState == BluetoothDevice.BOND_BONDED) {
            connect(device)
        } else {
            val name = getDeviceName(device)
            _connectionState.value = HidConnectionState.Connecting(name)
            device.createBond()
        }
    }

    /**
     * Called on Activity onResume to ensure the HID service is registered
     * and attempt reconnection to the bonded TV.
     */
    fun onResume() {
        if (bluetoothAdapter?.isEnabled == true) {
            if (hidDevice == null) {
                initialize()
            } else if (!isAppRegistered) {
                registerHidApp()
            } else if (connectedDevice == null) {
                autoConnect()
            }
        }
    }

    /**
     * Registers the application as a Bluetooth HID Device.
     */
    @SuppressLint("MissingPermission")
    private fun registerHidApp() {
        if (!hasConnectPermission()) {
            Log.w(tag, "Missing BLUETOOTH_CONNECT permission to register HID app")
            return
        }

        val proxy = hidDevice ?: return
        if (isAppRegistered) return

        try {
            // Using SUBCLASS1_KEYBOARD for Remote Control / Keyboard (Page 0x07 + 0x0C)
            val sdpSettings = BluetoothHidDeviceAppSdpSettings(
                "Google TV BT Remote",
                "Google TV Bluetooth HID Remote Control",
                "Google",
                BluetoothHidDevice.SUBCLASS1_KEYBOARD,
                BluetoothHidDescriptor.DESCRIPTOR
            )

            val success = proxy.registerApp(sdpSettings, null, null, executor, hidCallback)
            Log.d(tag, "proxy.registerApp result: $success")
        } catch (e: SecurityException) {
            Log.e(tag, "SecurityException registering HID app", e)
            _connectionState.value = HidConnectionState.Error("Permissão de Bluetooth negada")
        } catch (e: Exception) {
            Log.e(tag, "Exception registering HID app", e)
            _connectionState.value = HidConnectionState.Error("Falha ao registrar HID: ${e.message}")
        }
    }

    /**
     * Attempts automatic reconnection to the last connected device,
     * or to any bonded device matching Chromecast / Google TV.
     */
    fun autoConnect() {
        if (connectedDevice != null) return

        val bonded = getBondedDevices()
        if (bonded.isEmpty()) return

        // 1. Prioritize previously connected MAC address
        val lastMac = preferencesManager.lastConnectedAddress
        var targetDevice = bonded.firstOrNull { it.address.equals(lastMac, ignoreCase = true) }

        // 2. If no last MAC (first pairing), identify bonded TV
        if (targetDevice == null) {
            targetDevice = bonded.firstOrNull { device ->
                val name = getDeviceName(device).lowercase()
                name.contains("chromecast") || name.contains("tv") || name.contains("shield") || name.contains("google")
            } ?: if (bonded.size == 1) bonded.first() else null
        }

        if (targetDevice != null) {
            Log.d(tag, "Auto-connecting to target device: ${targetDevice.address}")
            connect(targetDevice)
        }
    }

    /**
     * Initiates connection to a specific Bluetooth device.
     */
    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice) {
        if (!hasConnectPermission()) {
            _connectionState.value = HidConnectionState.Error("Permissão BLUETOOTH_CONNECT necessária")
            return
        }

        val proxy = hidDevice
        if (proxy == null || !isAppRegistered) {
            pendingDeviceToConnect = device
            initialize()
            return
        }

        try {
            val name = getDeviceName(device)
            _connectionState.value = HidConnectionState.Connecting(name)
            Log.d(tag, "proxy.connect() called for: $name (${device.address})")

            val connectResult = proxy.connect(device)
            Log.d(tag, "proxy.connect() result: $connectResult")

            if (!connectResult) {
                // TV might be connecting to us or in idle state.
                Log.w(tag, "proxy.connect() returned false. Awaiting TV incoming connection.")
            }
        } catch (e: SecurityException) {
            Log.e(tag, "SecurityException connecting to device", e)
            _connectionState.value = HidConnectionState.Error("Permissão negada ao conectar")
        } catch (e: Exception) {
            Log.e(tag, "Exception connecting to device", e)
            _connectionState.value = HidConnectionState.Error("Erro ao conectar: ${e.message}")
        }
    }

    /**
     * Disconnects the currently connected device.
     */
    @SuppressLint("MissingPermission")
    fun disconnect() {
        if (!hasConnectPermission()) return

        val device = connectedDevice ?: return
        try {
            hidDevice?.disconnect(device)
        } catch (e: Exception) {
            Log.e(tag, "Error disconnecting device", e)
        } finally {
            connectedDevice = null
            _connectionState.value = HidConnectionState.Disconnected
        }
    }

    /**
     * Sends an HID key press followed by a release report after 30ms.
     * Prevents key repetition / sticking on Google TV / Android TV.
     *
     * @param reportId Report ID: 1 for Keyboard, 2 for Consumer Control.
     * @param keyCode The usage code / key code to dispatch.
     */
    @SuppressLint("MissingPermission")
    fun sendKey(reportId: Int, keyCode: Int) {
        val device = connectedDevice
        val proxy = hidDevice

        if (device == null || proxy == null) {
            Log.w(tag, "Cannot sendKey: device is not connected")
            return
        }

        if (!hasConnectPermission()) {
            Log.w(tag, "Cannot sendKey: missing BLUETOOTH_CONNECT permission")
            return
        }

        coroutineScope.launch {
            try {
                val pressPayload = BluetoothHidDescriptor.createPressPayload(reportId, keyCode)
                val releasePayload = BluetoothHidDescriptor.createReleasePayload(reportId)

                // 1. Send Key Press
                val pressResult = proxy.sendReport(device, reportId, pressPayload)
                if (!pressResult) {
                    Log.w(tag, "sendReport (press) returned false for key 0x${Integer.toHexString(keyCode)}")
                }

                // 2. Wait 30ms
                delay(30L)

                // 3. Send Key Release
                val releaseResult = proxy.sendReport(device, reportId, releasePayload)
                if (!releaseResult) {
                    Log.w(tag, "sendReport (release) returned false")
                }
            } catch (e: SecurityException) {
                Log.e(tag, "SecurityException in sendKey", e)
            } catch (e: Exception) {
                Log.e(tag, "Exception in sendKey", e)
            }
        }
    }

    /**
     * Helper to get list of bonded (paired) Bluetooth devices.
     */
    @SuppressLint("MissingPermission")
    fun getBondedDevices(): List<BluetoothDevice> {
        if (!hasConnectPermission()) return emptyList()
        return try {
            bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
        } catch (e: SecurityException) {
            Log.e(tag, "SecurityException getting bonded devices", e)
            emptyList()
        }
    }

    /**
     * Safely reads the user-friendly name of a BluetoothDevice.
     */
    @SuppressLint("MissingPermission")
    fun getDeviceName(device: BluetoothDevice?): String {
        if (device == null) return "Desconhecido"
        if (!hasConnectPermission()) return device.address ?: "Desconhecido"
        return try {
            device.name ?: device.address ?: "Dispositivo"
        } catch (e: SecurityException) {
            device.address ?: "Dispositivo"
        }
    }

    private fun onDeviceConnected(device: BluetoothDevice) {
        connectedDevice = device
        pendingDeviceToConnect = null
        val name = getDeviceName(device)
        val address = device.address ?: ""
        preferencesManager.lastConnectedAddress = address
        preferencesManager.lastConnectedName = name
        _connectionState.value = HidConnectionState.Connected(device, name, address)
        Log.i(tag, "Successfully connected to $name ($address)")
    }

    fun hasConnectPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Cleans up profile proxy, unregisters receiver and HID app when closing.
     */
    @SuppressLint("MissingPermission")
    fun destroy() {
        try {
            stopDiscovery()
            if (isReceiverRegistered) {
                context.unregisterReceiver(discoveryReceiver)
                isReceiverRegistered = false
            }
            if (hasConnectPermission() && isAppRegistered) {
                hidDevice?.unregisterApp()
            }
            bluetoothAdapter?.closeProfileProxy(BluetoothProfile.HID_DEVICE, hidDevice)
        } catch (e: Exception) {
            Log.e(tag, "Error during destroy", e)
        } finally {
            hidDevice = null
            isAppRegistered = false
            connectedDevice = null
            executor.shutdown()
        }
    }
}
