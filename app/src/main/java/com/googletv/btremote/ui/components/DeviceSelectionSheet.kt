package com.googletv.btremote.ui.components

import android.bluetooth.BluetoothDevice
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.googletv.btremote.bluetooth.HidConnectionState
import com.googletv.btremote.ui.theme.StatusGreen
import com.googletv.btremote.ui.theme.SurfaceDark
import com.googletv.btremote.ui.theme.TextSecondary
import com.googletv.btremote.ui.theme.TextWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceSelectionSheet(
    sheetState: SheetState,
    bondedDevices: List<BluetoothDevice>,
    scannedDevices: List<BluetoothDevice>,
    isScanning: Boolean,
    connectionState: HidConnectionState,
    onDeviceSelected: (BluetoothDevice) -> Unit,
    onPairAndConnect: (BluetoothDevice) -> Unit,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onDisconnectClick: () -> Unit,
    onOpenSettingsClick: () -> Unit,
    onMakeDiscoverableClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color(0xFF1E1E22),
        contentColor = TextWhite
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Conectar Google TV",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextWhite
                    )

                    IconButton(onClick = onRefreshClick) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Atualizar",
                            tint = TextSecondary
                        )
                    }
                }
            }

            // Explication Card: Por que a TV não encontra o celular?
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceDark)
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFFBBC04),
                            modifier = Modifier
                                .size(22.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Como conectar sua TV:",
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "O Google TV filtra e oculta celulares na tela de busca dele. Para parear:\n" +
                                        "1. Deixe a TV na tela 'Adicionar acessório'.\n" +
                                        "2. Toque em 'Buscar TV' abaixo (ou no botão 'Bluetooth do Celular') e selecione sua TV.\n" +
                                        "3. Confirme o código de pareamento na TV e no celular.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Scanner action bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (isScanning) onStopScan() else onStartScan()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isScanning) Color(0xFF5A3030) else Color(0xFF383842)
                        )
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = TextWhite,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Parar Busca", fontSize = 13.sp, color = TextWhite)
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.BluetoothSearching,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = TextWhite
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Buscar TV", fontSize = 13.sp, color = TextWhite)
                        }
                    }

                    OutlinedButton(
                        onClick = onOpenSettingsClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = TextWhite
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bluetooth Celular", fontSize = 13.sp)
                    }
                }
            }

            // Scanned Devices Section (Nearby)
            if (scannedDevices.isNotEmpty()) {
                item {
                    Text(
                        text = "TVs e Dispositivos Encontrados (Toque para Parear):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF8AB4F8),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                items(scannedDevices, key = { "scan_${it.address}" }) { device ->
                    val deviceName = try {
                        device.name ?: device.address ?: "Dispositivo"
                    } catch (e: SecurityException) {
                        device.address ?: "Dispositivo"
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF263238))
                            .clickable {
                                onPairAndConnect(device)
                                onDismissRequest()
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E88E5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tv,
                                contentDescription = null,
                                tint = TextWhite,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = deviceName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextWhite
                            )
                            Text(
                                text = "${device.address} (Toque para parear)",
                                fontSize = 11.sp,
                                color = Color(0xFFB0BEC5)
                            )
                        }
                    }
                }
            }

            // Bonded Devices Section
            item {
                Text(
                    text = "Dispositivos Já Pareados:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (bondedDevices.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhum dispositivo Bluetooth pareado no momento.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(bondedDevices, key = { "bonded_${it.address}" }) { device ->
                    val isConnected = connectionState is HidConnectionState.Connected &&
                            connectionState.deviceAddress.equals(device.address, ignoreCase = true)

                    val deviceName = try {
                        device.name ?: device.address ?: "Dispositivo"
                    } catch (e: SecurityException) {
                        device.address ?: "Dispositivo"
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isConnected) Color(0xFF233524) else SurfaceDark)
                            .clickable {
                                onDeviceSelected(device)
                                onDismissRequest()
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isConnected) StatusGreen else Color(0xFF38383F)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isConnected) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                                contentDescription = null,
                                tint = TextWhite,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = deviceName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextWhite
                            )
                            Text(
                                text = device.address,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        if (isConnected) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Conectado",
                                    tint = StatusGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Ativo",
                                    color = StatusGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Tip box for pairing/connecting
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF23252E))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "⚡ Dica de Conexão:",
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhite,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "1. Toque no seu Chromecast acima para enviar o sinal de conexão.\n" +
                                    "2. Se não conectar em instantes, olhe na TV: em Configurações > Controles e acessórios, clique no nome do seu celular para a TV autorizar a conexão.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            // Make phone discoverable button
            item {
                OutlinedButton(
                    onClick = onMakeDiscoverableClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite)
                ) {
                    Text("Tornar Celular Visível para Parear (180s)", fontSize = 13.sp)
                }
            }

            // Disconnect button
            if (connectionState is HidConnectionState.Connected) {
                item {
                    Button(
                        onClick = {
                            onDisconnectClick()
                            onDismissRequest()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5A2E2E))
                    ) {
                        Text("Desconectar Controle", color = Color(0xFFFFB4AB))
                    }
                }
            }
        }
    }
}
