package com.googletv.btremote.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.googletv.btremote.ui.theme.AssistantIcon

/**
 * Draws the iconic Google Assistant 4-dot mark.
 */
@Composable
fun GoogleAssistantIcon(
    modifier: Modifier = Modifier,
    size: Dp = 26.dp,
    tint: Color = AssistantIcon
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Dot 1: Large center-left dot
        drawCircle(
            color = tint,
            radius = w * 0.17f,
            center = Offset(w * 0.36f, h * 0.50f)
        )

        // Dot 2: Medium top-right dot
        drawCircle(
            color = tint,
            radius = w * 0.11f,
            center = Offset(w * 0.68f, h * 0.38f)
        )

        // Dot 3: Small bottom-right dot
        drawCircle(
            color = tint,
            radius = w * 0.08f,
            center = Offset(w * 0.70f, h * 0.65f)
        )

        // Dot 4: Tiny far-right dot
        drawCircle(
            color = tint,
            radius = w * 0.055f,
            center = Offset(w * 0.88f, h * 0.44f)
        )
    }
}

