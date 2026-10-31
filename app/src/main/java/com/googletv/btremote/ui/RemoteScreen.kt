package com.googletv.btremote.ui

import android.bluetooth.BluetoothDevice
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.googletv.btremote.bluetooth.HidConnectionState
import com.googletv.btremote.ui.components.DPadControl
import com.googletv.btremote.ui.components.DeviceSelectionSheet
import com.googletv.btremote.ui.components.GoogleAssistantIcon
import com.googletv.btremote.ui.components.TouchpadControl
import com.googletv.btremote.ui.theme.AssistantBg
import com.googletv.btremote.ui.theme.BgDark
import com.googletv.btremote.ui.theme.DividerDark
import com.googletv.btremote.ui.theme.StatusAmber
import com.googletv.btremote.ui.theme.StatusGreen
import com.googletv.btremote.ui.theme.SurfaceDark
import com.googletv.btremote.ui.theme.TextSecondary
import com.googletv.btremote.ui.theme.TextWhite
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteScreen(
    viewModel: RemoteViewModel,
    onOpenBluetoothSettings: () -> Unit,
    onRequestDiscoverable: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    val connectionState by viewModel.connectionState.collectAsState()
    val remoteMode by viewModel.remoteMode.collectAsState()
    val bondedDevices by viewModel.bondedDevices.collectAsState()
    val scannedDevices by viewModel.scannedDevices.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var showDeviceSheet by remember { mutableStateOf(false) }
    var showDisconnectDialog by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // =================================================================
            // 1. TOP APP BAR
            // =================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: X button + Device Name or Status
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            showDeviceSheet = true
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (connectionState is HidConnectionState.Connected) {
                                showDisconnectDialog = true
                            } else {
                                showDeviceSheet = true
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar / Desconectar",
                            tint = TextWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    val (statusText, statusColor) = when (val state = connectionState) {
                        is HidConnectionState.Connected -> Pair(state.deviceName, TextWhite)
                        is HidConnectionState.Connecting -> Pair("Conectando...", StatusAmber)
                        is HidConnectionState.Registering -> Pair("Iniciando HID...", StatusAmber)
                        is HidConnectionState.Disconnected -> Pair("Desconectado - Toque para conectar", TextSecondary)
                        is HidConnectionState.Error -> Pair("Erro: ${state.message}", Color(0xFFEA4335))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        if (connectionState is HidConnectionState.Connected) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(StatusGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Text(
                            text = statusText,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Right: Power button + Overflow Menu
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.onPower()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PowerSettingsNew,
                            contentDescription = "Ligar / Desligar TV",
                            tint = TextWhite,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Mais opções",
                                tint = TextWhite,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(SurfaceDark)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Modo D-Pad", color = TextWhite) },
                                trailingIcon = {
                                    if (remoteMode == RemoteMode.DPAD) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = StatusGreen
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.setRemoteMode(RemoteMode.DPAD)
                                    showMenu = false
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Modo Touchpad (Swipe)", color = TextWhite) },
                                trailingIcon = {
                                    if (remoteMode == RemoteMode.TOUCHPAD) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = StatusGreen
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.setRemoteMode(RemoteMode.TOUCHPAD)
                                    showMenu = false
                                }
                            )

                            HorizontalDivider(color = DividerDark)

                            DropdownMenuItem(
                                text = { Text("Dispositivos Pareados", color = TextWhite) },
                                onClick = {
                                    showMenu = false
                                    viewModel.refreshBondedDevices()
                                    showDeviceSheet = true
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Tornar Visível para Parear", color = TextWhite) },
                                onClick = {
                                    showMenu = false
                                    onRequestDiscoverable()
                                }
                            )

                            if (connectionState is HidConnectionState.Connected) {
                                HorizontalDivider(color = DividerDark)
                                DropdownMenuItem(
                                    text = { Text("Desconectar", color = Color(0xFFFFB4AB)) },
                                    onClick = {
                                        showMenu = false
                                        viewModel.disconnectDevice()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // =================================================================
            // 2. CENTRAL NAVIGATION AREA (DPAD or TOUCHPAD)
            // =================================================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Crossfade(
                    targetState = remoteMode,
                    label = "RemoteNavigationModeCrossfade"
                ) { mode ->
                    when (mode) {
                        RemoteMode.DPAD -> {
                            DPadControl(
                                onUpClick = { viewModel.onDpadUp() },
                                onDownClick = { viewModel.onDpadDown() },
                                onLeftClick = { viewModel.onDpadLeft() },
                                onRightClick = { viewModel.onDpadRight() },
                                onCenterClick = { viewModel.onCenterClick() }
                            )
                        }
                        RemoteMode.TOUCHPAD -> {
                            TouchpadControl(
                                onUpSwipe = { viewModel.onDpadUp() },
                                onDownSwipe = { viewModel.onDpadDown() },
                                onLeftSwipe = { viewModel.onDpadLeft() },
                                onRightSwipe = { viewModel.onDpadRight() },
                                onTap = { viewModel.onCenterClick() }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // =================================================================
            // 3. SYSTEM NAVIGATION ROW (Back, Home, Assistant)
            // =================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Voltar (Back) - Medium Circle #2A2A2E
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(SurfaceDark)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true)
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.onBack()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = TextWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Home - Larger Circle #2A2A2E
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .clip(CircleShape)
                        .background(SurfaceDark)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true)
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.onHome()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Home,
                        contentDescription = "Início (Home)",
                        tint = TextWhite,
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Google Assistente - Medium White Circle #F1F3F4 with dark 4-dot icon
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(AssistantBg)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true)
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.onAssistant()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    GoogleAssistantIcon(
                        size = 28.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // =================================================================
            // 4. BOTTOM AUDIO ROW (Mute + Volume Pill)
            // =================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mute - Circular Button #2A2A2E
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(SurfaceDark)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true)
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.onMute()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeOff,
                        contentDescription = "Mudo",
                        tint = TextWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Volume Pill Button - Divided into Volume Down and Volume Up
                Box(
                    modifier = Modifier
                        .width(200.dp)
                        .height(62.dp)
                        .clip(RoundedCornerShape(50))
                        .background(SurfaceDark)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Half: Volume Down (-)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true)
                                ) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.onVolumeDown()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeDown,
                                contentDescription = "Diminuir Volume",
                                tint = TextWhite,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Fine Vertical Divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight(0.6f)
                                .background(DividerDark)
                        )

                        // Right Half: Volume Up (+)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true)
                                ) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.onVolumeUp()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Aumentar Volume",
                                tint = TextWhite,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // Bottom Navigation Indicator Bar
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF38383F))
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // =====================================================================
        // MODALS AND DIALOGS
        // =====================================================================

        // 1. Paired Devices Bottom Sheet
        if (showDeviceSheet) {
            DeviceSelectionSheet(
                sheetState = sheetState,
                bondedDevices = bondedDevices,
                scannedDevices = scannedDevices,
                isScanning = isScanning,
                connectionState = connectionState,
                onDeviceSelected = { device ->
                    viewModel.connectDevice(device)
                },
                onPairAndConnect = { device ->
                    viewModel.pairAndConnect(device)
                },
                onStartScan = { viewModel.startScanning() },
                onStopScan = { viewModel.stopScanning() },
                onDisconnectClick = {
                    viewModel.disconnectDevice()
                },
                onOpenSettingsClick = onOpenBluetoothSettings,
                onMakeDiscoverableClick = onRequestDiscoverable,
                onRefreshClick = { viewModel.refreshBondedDevices() },
                onDismissRequest = {
                    coroutineScope.launch {
                        viewModel.stopScanning()
                        sheetState.hide()
                        showDeviceSheet = false
                    }
                }
            )
        }

        // 2. Disconnect Confirmation Dialog
        if (showDisconnectDialog) {
            AlertDialog(
                onDismissRequest = { showDisconnectDialog = false },
                containerColor = SurfaceDark,
                title = { Text("Desconectar controle?", color = TextWhite) },
                text = {
                    Text(
                        "Deseja desconectar o controle Bluetooth da TV?",
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.disconnectDevice()
                            showDisconnectDialog = false
                        }
                    ) {
                        Text("Desconectar", color = Color(0xFFFFB4AB))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDisconnectDialog = false }) {
                        Text("Cancelar", color = TextWhite)
                    }
                }
            )
        }
    }
}
