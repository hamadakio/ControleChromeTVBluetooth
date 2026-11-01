package com.googletv.btremote

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.googletv.btremote.ui.RemoteScreen
import com.googletv.btremote.ui.RemoteViewModel
import com.googletv.btremote.ui.theme.BgDark
import com.googletv.btremote.ui.theme.GoogleTVBTRemoteTheme
import com.googletv.btremote.ui.theme.TextSecondary
import com.googletv.btremote.ui.theme.TextWhite

class MainActivity : ComponentActivity() {

    private val viewModel: RemoteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Keep screen active while remote is open to prevent CPU/Bluetooth sleep and reconnect flickers
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            GoogleTVBTRemoteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContent(
                        viewModel = viewModel,
                        onOpenBluetoothSettings = { openBluetoothSettings() },
                        onRequestDiscoverable = { requestDiscoverable() },
                        onRequestEnableBluetooth = { requestEnableBluetooth() }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    private fun openBluetoothSettings() {
        try {
            startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
        } catch (e: Exception) {
            Toast.makeText(this, "Não foi possível abrir as configurações de Bluetooth", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestDiscoverable() {
        try {
            val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 180)
            }
            startActivity(discoverableIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao solicitar visibilidade Bluetooth", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestEnableBluetooth() {
        try {
            startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        } catch (e: Exception) {
            Toast.makeText(this, "Ative o Bluetooth manualmente nas Configurações", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
private fun MainContent(
    viewModel: RemoteViewModel,
    onOpenBluetoothSettings: () -> Unit,
    onRequestDiscoverable: () -> Unit,
    onRequestEnableBluetooth: () -> Unit
) {
    val requiredPermissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }
    }

    var hasPermissions by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasPermissions = results.values.all { it }
        if (hasPermissions) {
            viewModel.onResume()
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        val allGranted = requiredPermissions.all { perm ->
            ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
        }
        hasPermissions = allGranted
        if (!allGranted) {
            permissionLauncher.launch(requiredPermissions)
        }
    }

    if (hasPermissions) {
        RemoteScreen(
            viewModel = viewModel,
            onOpenBluetoothSettings = onOpenBluetoothSettings,
            onRequestDiscoverable = onRequestDiscoverable
        )
    } else {
        // Permissions rational screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BgDark)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Permissão Bluetooth Necessária",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Para se conectar ao Chromecast / Google TV como um controle remoto HID, o aplicativo necessita de permissão para gerenciar conexões Bluetooth.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { permissionLauncher.launch(requiredPermissions) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF44444A))
                ) {
                    Text("Conceder Permissões", color = TextWhite)
                }
            }
        }
    }
}

