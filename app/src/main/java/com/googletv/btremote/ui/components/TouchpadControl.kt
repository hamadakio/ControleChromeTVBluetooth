package com.googletv.btremote.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.googletv.btremote.ui.theme.SurfaceDark
import com.googletv.btremote.ui.theme.TouchpadDot
import com.googletv.btremote.ui.util.HapticHelper
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Touchpad navigation surface reproducing the official Google TV swipe/tap layout.
 *
 * Touch & drag: Shows a white circular indicator tracking the finger.
 * Swipe gestures (Up/Down/Left/Right): Dispatches corresponding D-Pad directional navigation.
 * Simple tap: Dispatches Center (OK / Enter).
 */
@Composable
fun TouchpadControl(
    onUpSwipe: () -> Unit,
    onDownSwipe: () -> Unit,
    onLeftSwipe: () -> Unit,
    onRightSwipe: () -> Unit,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    val swipeThresholdPx = with(density) { 38.dp.toPx() }
    val dotDiameter = 40.dp
    val dotRadiusPx = with(density) { (dotDiameter / 2).toPx() }

    val dotX = remember { Animatable(0f) }
    val dotY = remember { Animatable(0f) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth(0.96f)
            .aspectRatio(0.92f)
            .clip(RoundedCornerShape(32.dp))
            .background(SurfaceDark)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val width = size.width.toFloat()
                    val height = size.height.toFloat()

                    // Center point of the pad
                    val centerX = width / 2f
                    val centerY = height / 2f

                    // Clamp to inside bounds
                    val clampedDownX = down.position.x.coerceIn(dotRadiusPx, width - dotRadiusPx)
                    val clampedDownY = down.position.y.coerceIn(dotRadiusPx, height - dotRadiusPx)

                    coroutineScope.launch {
                        dotX.snapTo(clampedDownX - centerX)
                        dotY.snapTo(clampedDownY - centerY)
                    }

                    var totalDx = 0f
                    var totalDy = 0f
                    var hasSwiped = false

                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break

                        if (change.pressed) {
                            val posChange = change.positionChange()
                            totalDx += posChange.x
                            totalDy += posChange.y

                            val newX = (change.position.x).coerceIn(dotRadiusPx, width - dotRadiusPx)
                            val newY = (change.position.y).coerceIn(dotRadiusPx, height - dotRadiusPx)

                            coroutineScope.launch {
                                dotX.snapTo(newX - centerX)
                                dotY.snapTo(newY - centerY)
                            }

                            // Detect Swipe if threshold reached
                            if (!hasSwiped) {
                                val absX = abs(totalDx)
                                val absY = abs(totalDy)

                                if (absX > swipeThresholdPx || absY > swipeThresholdPx) {
                                    hasSwiped = true
                                    HapticHelper.vibrateClick(context)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                                    if (absX > absY) {
                                        if (totalDx > 0) {
                                            onRightSwipe()
                                        } else {
                                            onLeftSwipe()
                                        }
                                    } else {
                                        if (totalDy > 0) {
                                            onDownSwipe()
                                        } else {
                                            onUpSwipe()
                                        }
                                    }
                                }
                            }
                            change.consume()
                        } else {
                            // Finger lifted (UP)
                            if (!hasSwiped) {
                                // Tap event -> OK / Enter
                                HapticHelper.vibrateClick(context)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onTap()
                            }

                            // Smooth spring back to center
                            coroutineScope.launch {
                                dotX.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                            coroutineScope.launch {
                                dotY.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                            break
                        }
                    } while (event.changes.any { it.pressed })
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Position indicator dot (White circle)
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = dotX.value.roundToInt(),
                        y = dotY.value.roundToInt()
                    )
                }
                .size(dotDiameter)
                .clip(CircleShape)
                .background(TouchpadDot)
        )
    }
}
