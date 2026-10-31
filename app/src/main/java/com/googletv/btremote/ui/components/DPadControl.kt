package com.googletv.btremote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.googletv.btremote.ui.theme.CenterSelect
import com.googletv.btremote.ui.theme.SurfaceDark
import com.googletv.btremote.ui.theme.TextSecondary

/**
 * Minimalist circular D-Pad controller reproducing the official Google TV remote layout.
 *
 * Outer ring: #2A2A2E with 4 directional arrows (Up, Down, Left, Right).
 * Inner circle: #44444A for OK / Select.
 */
@Composable
fun DPadControl(
    onUpClick: () -> Unit,
    onDownClick: () -> Unit,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit,
    onCenterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val diameter = min(maxWidth * 0.88f, 320.dp)
        val centerDiameter = diameter * 0.44f
        val arrowSize = 36.dp

        // Outer Dark Gray Circle
        Box(
            modifier = Modifier
                .size(diameter)
                .clip(CircleShape)
                .background(SurfaceDark),
            contentAlignment = Alignment.Center
        ) {
            // Directional click areas

            // UP BUTTON
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(width = diameter * 0.6f, height = (diameter - centerDiameter) / 2f + 16.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, radius = 64.dp)
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onUpClick()
                    }
                    .padding(top = 18.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Cima",
                    tint = TextSecondary,
                    modifier = Modifier.size(arrowSize)
                )
            }

            // DOWN BUTTON
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .size(width = diameter * 0.6f, height = (diameter - centerDiameter) / 2f + 16.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, radius = 64.dp)
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDownClick()
                    }
                    .padding(bottom = 18.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Baixo",
                    tint = TextSecondary,
                    modifier = Modifier.size(arrowSize)
                )
            }

            // LEFT BUTTON
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(width = (diameter - centerDiameter) / 2f + 16.dp, height = diameter * 0.6f)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, radius = 64.dp)
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLeftClick()
                    }
                    .padding(start = 18.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Esquerda",
                    tint = TextSecondary,
                    modifier = Modifier.size(arrowSize)
                )
            }

            // RIGHT BUTTON
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(width = (diameter - centerDiameter) / 2f + 16.dp, height = diameter * 0.6f)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, radius = 64.dp)
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onRightClick()
                    }
                    .padding(end = 18.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Direita",
                    tint = TextSecondary,
                    modifier = Modifier.size(arrowSize)
                )
            }

            // CENTER (SELECT / OK) BUTTON
            Box(
                modifier = Modifier
                    .size(centerDiameter)
                    .clip(CircleShape)
                    .background(CenterSelect)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true)
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCenterClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                // Smooth minimalist circular surface as in the official remote
            }
        }
    }
}
