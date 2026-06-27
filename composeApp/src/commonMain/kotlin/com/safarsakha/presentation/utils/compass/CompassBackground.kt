package com.safarsakha.presentation.utils.compass

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer

// =============================================================================
// COMPASS BACKGROUND COMPOSABLE
// =============================================================================

@Composable
fun CompassBackground(
    state: CompassState,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = state.alpha.value
            }
    ) {
        val compassRadius = size.width * CompassConstants.COMPASS_SIZE_MULTIPLIER
        val cx = size.width * 0.50f
        val cy = size.height * CompassConstants.COMPASS_VERTICAL_POSITION

        drawCompass(
            cx = cx,
            cy = cy,
            radius = compassRadius,
            primaryNeedleAngle = state.primaryNeedleRotation.value,
            secondaryNeedleAngle = state.secondaryNeedleRotation.value
        )
    }
}

// =============================================================================
// BACKGROUND WASH COMPOSABLE
// =============================================================================

@Composable
fun BackgroundWash(modifier: Modifier = Modifier) {
    val compass = CompassConstants
    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    compass.CreamColor.copy(alpha = 0.32f),
                    compass.CreamColor.copy(alpha = 0.12f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.82f, size.height * 0.10f),
                radius = size.width * 0.80f
            )
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    compass.SkyColor.copy(alpha = 0.10f),
                    compass.SkyColor.copy(alpha = 0.04f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.08f, size.height * 0.92f),
                radius = size.width * 0.55f
            )
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    compass.GoldColor.copy(alpha = 0.06f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.50f, size.height * 0.40f),
                radius = size.width * 0.35f
            )
        )
    }
}