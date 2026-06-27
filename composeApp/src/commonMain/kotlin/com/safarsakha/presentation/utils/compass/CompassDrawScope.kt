package com.safarsakha.presentation.utils.compass

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// =============================================================================
// COMPASS DRAWING LOGIC
// =============================================================================

fun DrawScope.drawCompass(
    cx: Float,
    cy: Float,
    radius: Float,
    primaryNeedleAngle: Float,
    secondaryNeedleAngle: Float
) {
    val compass = CompassConstants

    // ── OUTER GLOW RINGS ────────────────────────────────────────────────
    drawCircle(
        color = compass.NavyColor.copy(alpha = compass.RING_1_ALPHA),
        radius = radius * compass.RING_1_RADIUS,
        center = Offset(cx, cy),
        style = Stroke(width = compass.RING_1_STROKE.dp.toPx())
    )

    drawCircle(
        color = compass.SkyColor.copy(alpha = compass.RING_2_ALPHA),
        radius = radius * compass.RING_2_RADIUS,
        center = Offset(cx, cy),
        style = Stroke(width = compass.RING_2_STROKE.dp.toPx())
    )

    drawCircle(
        color = compass.NavyColor.copy(alpha = compass.RING_3_ALPHA),
        radius = radius * compass.RING_3_RADIUS,
        center = Offset(cx, cy),
        style = Stroke(width = compass.RING_3_STROKE.dp.toPx())
    )

    // ── FACE FILL ────────────────────────────────────────────────────────
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                compass.BgColor.copy(alpha = compass.FACE_BG_ALPHA),
                compass.CreamColor.copy(alpha = compass.FACE_CREAM_ALPHA),
                compass.NavyColor.copy(alpha = compass.FACE_NAVY_ALPHA),
                compass.SkyColor.copy(alpha = compass.FACE_SKY_ALPHA)
            ),
            center = Offset(cx, cy),
            radius = radius
        ),
        radius = radius,
        center = Offset(cx, cy)
    )

    // ── FACE BORDER ──────────────────────────────────────────────────────
    drawCircle(
        color = compass.NavyColor.copy(alpha = compass.FACE_BORDER_ALPHA),
        radius = radius,
        center = Offset(cx, cy),
        style = Stroke(width = compass.FACE_BORDER_STROKE.dp.toPx())
    )

    drawCircle(
        color = compass.SkyColor.copy(alpha = compass.FACE_INNER_BORDER_ALPHA),
        radius = radius - compass.FACE_INNER_BORDER_OFFSET.dp.toPx(),
        center = Offset(cx, cy),
        style = Stroke(width = compass.FACE_INNER_BORDER_STROKE.dp.toPx())
    )

    // ── TICK MARKS ──────────────────────────────────────────────────────
    val tickRing = radius * compass.TICK_RING_MULTIPLIER
    for (i in 0 until compass.TICK_COUNT) {
        val angleRad = (i * compass.DEGREES_PER_TICK * PI / 180.0).toFloat()
        val isCardinal = i % 9 == 0
        val isHalfCardinal = i % 4 == 0 && !isCardinal
        val isQuarter = i % 2 == 0 && !isCardinal && !isHalfCardinal

        val tickLen = when {
            isCardinal -> radius * compass.CARDINAL_TICK_LENGTH
            isHalfCardinal -> radius * compass.HALF_CARDINAL_TICK_LENGTH
            isQuarter -> radius * compass.QUARTER_TICK_LENGTH
            else -> radius * compass.MINOR_TICK_LENGTH
        }

        val tickAlpha = when {
            isCardinal -> compass.CARDINAL_TICK_ALPHA
            isHalfCardinal -> compass.HALF_CARDINAL_TICK_ALPHA
            isQuarter -> compass.QUARTER_TICK_ALPHA
            else -> compass.MINOR_TICK_ALPHA
        }

        val sw = when {
            isCardinal -> compass.CARDINAL_TICK_STROKE.dp.toPx()
            isHalfCardinal -> compass.HALF_CARDINAL_TICK_STROKE.dp.toPx()
            else -> compass.MINOR_TICK_STROKE.dp.toPx()
        }

        val ox = cx + tickRing * sin(angleRad)
        val oy = cy - tickRing * cos(angleRad)
        val ix = cx + (tickRing - tickLen) * sin(angleRad)
        val iy = cy - (tickRing - tickLen) * cos(angleRad)

        drawLine(
            color = compass.NavyColor.copy(alpha = tickAlpha),
            start = Offset(ox, oy),
            end = Offset(ix, iy),
            strokeWidth = sw,
            cap = StrokeCap.Round
        )
    }

    // ── CARDINAL DIRECTION LABELS ──────────────────────────────────────
    val labelRadius = radius * compass.LABEL_RADIUS_MULTIPLIER
    val cardinalAngles = listOf(270f, 0f, 90f, 180f)
    val labelColors = listOf(
        compass.NavyColor.copy(alpha = compass.N_LABEL_ALPHA),
        compass.NavyColor.copy(alpha = compass.E_LABEL_ALPHA),
        compass.NavyColor.copy(alpha = compass.S_LABEL_ALPHA),
        compass.NavyColor.copy(alpha = compass.W_LABEL_ALPHA)
    )

    cardinalAngles.forEachIndexed { index, angle ->
        val angleRad = (angle * PI / 180.0).toFloat()
        val x = cx + labelRadius * sin(angleRad)
        val y = cy - labelRadius * cos(angleRad)

        drawCircle(
            color = compass.BgColor.copy(alpha = compass.LABEL_DOT_ALPHA),
            radius = compass.LABEL_DOT_RADIUS.dp.toPx(),
            center = Offset(x, y)
        )

        drawCircle(
            color = labelColors[index],
            radius = compass.LABEL_CIRCLE_RADIUS.dp.toPx(),
            center = Offset(x, y)
        )
    }

    // ── DECORATIVE INNER RINGS ─────────────────────────────────────────
    listOf(
        compass.INNER_RING_1_RADIUS to compass.INNER_RING_1_ALPHA,
        compass.INNER_RING_2_RADIUS to compass.INNER_RING_2_ALPHA,
        compass.INNER_RING_3_RADIUS to compass.INNER_RING_3_ALPHA
    ).forEach { (r, alpha) ->
        drawCircle(
            color = compass.NavyColor.copy(alpha = alpha),
            radius = r * radius,
            center = Offset(cx, cy),
            style = Stroke(width = compass.INNER_RING_STROKE.dp.toPx())
        )
    }

    // ── COMPASS ROSETTE ─────────────────────────────────────────────────
    for (i in 0 until compass.ROSETTE_COUNT) {
        val angleRad = (i * compass.ROSETTE_DEGREES * PI / 180.0).toFloat()
        val innerR = radius * compass.ROSETTE_INNER_RADIUS
        val outerR = radius * compass.ROSETTE_OUTER_RADIUS

        drawLine(
            color = compass.NavyColor.copy(alpha = compass.ROSETTE_ALPHA),
            start = Offset(
                cx + innerR * sin(angleRad),
                cy - innerR * cos(angleRad)
            ),
            end = Offset(
                cx + outerR * sin(angleRad),
                cy - outerR * cos(angleRad)
            ),
            strokeWidth = compass.ROSETTE_STROKE.dp.toPx(),
            cap = StrokeCap.Round
        )
    }

    // ── E-W CROSSBAR ────────────────────────────────────────────────────
    drawLine(
        color = compass.NavyColor.copy(alpha = compass.CROSSBAR_ALPHA),
        start = Offset(cx - radius * compass.CROSSBAR_LENGTH, cy),
        end = Offset(cx + radius * compass.CROSSBAR_LENGTH, cy),
        strokeWidth = compass.CROSSBAR_STROKE.dp.toPx(),
        cap = StrokeCap.Round
    )

    drawCircle(
        color = compass.NavyColor.copy(alpha = compass.CROSSBAR_ACCENT_ALPHA),
        radius = compass.CROSSBAR_ACCENT_RADIUS.dp.toPx(),
        center = Offset(cx - radius * compass.CROSSBAR_LENGTH, cy)
    )
    drawCircle(
        color = compass.NavyColor.copy(alpha = compass.CROSSBAR_ACCENT_ALPHA),
        radius = compass.CROSSBAR_ACCENT_RADIUS.dp.toPx(),
        center = Offset(cx + radius * compass.CROSSBAR_LENGTH, cy)
    )

    // ── CENTER JEWEL ────────────────────────────────────────────────────
    drawCircle(
        color = compass.NavyColor.copy(alpha = compass.JEWEL_OUTER_ALPHA),
        radius = compass.JEWEL_OUTER_RADIUS.dp.toPx(),
        center = Offset(cx, cy)
    )

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = compass.JEWEL_WHITE_ALPHA),
                compass.GoldColor.copy(alpha = compass.JEWEL_GOLD_ALPHA),
                compass.CreamColor.copy(alpha = compass.JEWEL_CREAM_ALPHA)
            ),
            center = Offset(cx, cy),
            radius = compass.JEWEL_INNER_RADIUS.dp.toPx()
        ),
        radius = compass.JEWEL_INNER_RADIUS.dp.toPx(),
        center = Offset(cx, cy)
    )

    drawCircle(
        color = Color.White.copy(alpha = compass.JEWEL_SPARKLE_ALPHA),
        radius = compass.JEWEL_SPARKLE_RADIUS.dp.toPx(),
        center = Offset(cx - compass.JEWEL_SPARKLE_OFFSET.dp.toPx(), cy - compass.JEWEL_SPARKLE_OFFSET.dp.toPx())
    )

    drawCircle(
        color = compass.SkyColor.copy(alpha = compass.JEWEL_GLOW_ALPHA),
        radius = compass.JEWEL_GLOW_RADIUS.dp.toPx(),
        center = Offset(cx, cy),
        style = Stroke(width = compass.JEWEL_GLOW_STROKE.dp.toPx())
    )

    // ── ANIMATED NEEDLES ──────────────────────────────────────────────
    drawPrimaryNeedle(cx, cy, radius, primaryNeedleAngle)
    drawSecondaryNeedle(cx, cy, radius, secondaryNeedleAngle)
}

// =============================================================================
// PRIMARY NEEDLE - Premium Navy
// =============================================================================

private fun DrawScope.drawPrimaryNeedle(
    cx: Float,
    cy: Float,
    radius: Float,
    angle: Float
) {
    val compass = CompassConstants
    val nLen = radius * compass.PRIMARY_NEEDLE_LENGTH
    val nBase = radius * compass.PRIMARY_NEEDLE_BASE

    rotate(angle, pivot = Offset(cx, cy)) {
        val needlePath = Path().apply {
            moveTo(cx, cy - nLen)
            lineTo(cx - nBase * 0.50f, cy + nBase * 0.12f)
            lineTo(cx, cy - nBase * 0.28f)
            lineTo(cx + nBase * 0.50f, cy + nBase * 0.12f)
            close()
        }

        drawPath(
            path = needlePath,
            brush = Brush.linearGradient(
                colors = listOf(
                    compass.NavyColor.copy(alpha = compass.PRIMARY_NEEDLE_ALPHA_1),
                    compass.NavyColor.copy(alpha = compass.PRIMARY_NEEDLE_ALPHA_2),
                    compass.SkyColor.copy(alpha = 0.30f)
                ),
                start = Offset(cx, cy - nLen),
                end = Offset(cx, cy)
            )
        )

        drawLine(
            color = Color.White.copy(alpha = 0.12f),
            start = Offset(cx - nBase * 0.12f, cy - nLen * 0.85f),
            end = Offset(cx + nBase * 0.08f, cy - nLen * 0.60f),
            strokeWidth = 1.8f.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

// =============================================================================
// SECONDARY NEEDLE - Premium Faded Red
// =============================================================================

private fun DrawScope.drawSecondaryNeedle(
    cx: Float,
    cy: Float,
    radius: Float,
    angle: Float
) {
    val compass = CompassConstants
    val sLen = radius * compass.SECONDARY_NEEDLE_LENGTH
    val sBase = radius * compass.SECONDARY_NEEDLE_BASE

    rotate(angle, pivot = Offset(cx, cy)) {
        val needlePath = Path().apply {
            moveTo(cx, cy + sLen)
            lineTo(cx - sBase * 0.45f, cy - sBase * 0.10f)
            lineTo(cx, cy + sBase * 0.20f)
            lineTo(cx + sBase * 0.45f, cy - sBase * 0.10f)
            close()
        }

        drawPath(
            path = needlePath,
            brush = Brush.linearGradient(
                colors = listOf(
                    compass.MutedRedColor.copy(alpha = compass.SECONDARY_NEEDLE_ALPHA_1),
                    compass.MutedRedColor.copy(alpha = compass.SECONDARY_NEEDLE_ALPHA_2),
                    compass.MutedRedColor.copy(alpha = 0.20f)
                ),
                start = Offset(cx, cy + sLen),
                end = Offset(cx, cy)
            )
        )

        drawLine(
            color = Color.White.copy(alpha = 0.08f),
            start = Offset(cx - sBase * 0.10f, cy + sLen * 0.85f),
            end = Offset(cx + sBase * 0.08f, cy + sLen * 0.60f),
            strokeWidth = 1.2f.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}