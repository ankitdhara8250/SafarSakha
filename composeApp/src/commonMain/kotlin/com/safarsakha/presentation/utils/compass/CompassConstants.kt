package com.safarsakha.presentation.utils.compass

import androidx.compose.ui.graphics.Color

// =============================================================================
// COMPASS CONSTANTS
// =============================================================================

object CompassConstants {
    // Colors
    val NavyColor = Color(0xFF0F172A)
    val SkyColor = Color(0xFF0EA5E9)
    val CreamColor = Color(0xFFF4E7D3)
    val GoldColor = Color(0xFFD4AF37)
    val BgColor = Color(0xFFFFFFFF)
    val MutedRedColor = Color(0xFFB85C5C)

    // Animation - 5 seconds total
    const val ANIMATION_DURATION_MS = 300_000
    const val FADE_IN_DURATION_MS = 800
    const val FADE_OUT_DURATION_MS = 1200
    const val NEEDLE_ROTATION_DURATION_MS = 2500
    const val HOLD_DURATION_MS = 296_900

    // Card transparency
    const val CARD_ALPHA_DURATION_MS = 600
    const val CARD_RESTORE_DURATION_MS = 800
    const val CARD_RESTORE_DELAY_MS = 500

    // Compass visual
    const val COMPASS_SIZE_MULTIPLIER = 0.50f
    const val COMPASS_VERTICAL_POSITION = 0.34f
    const val MAX_ALPHA = 0.72f
    const val CARD_MIN_ALPHA = 0.88f

    // Needle angles - subtle movement
    const val PRIMARY_NEEDLE_START_ANGLE = -15f
    const val PRIMARY_NEEDLE_END_ANGLE = 40f
    const val SECONDARY_NEEDLE_START_ANGLE = 10f
    const val SECONDARY_NEEDLE_END_ANGLE = -28f

    // Outer rings
    const val RING_1_RADIUS = 1.18f
    const val RING_1_ALPHA = 0.10f
    const val RING_1_STROKE = 2.5f

    const val RING_2_RADIUS = 1.14f
    const val RING_2_ALPHA = 0.08f
    const val RING_2_STROKE = 1.5f

    const val RING_3_RADIUS = 1.08f
    const val RING_3_ALPHA = 0.15f
    const val RING_3_STROKE = 2.0f

    // Face fill
    const val FACE_BG_ALPHA = 0.25f
    const val FACE_CREAM_ALPHA = 0.20f
    const val FACE_NAVY_ALPHA = 0.06f
    const val FACE_SKY_ALPHA = 0.04f

    // Face border
    const val FACE_BORDER_ALPHA = 0.20f
    const val FACE_BORDER_STROKE = 2.0f
    const val FACE_INNER_BORDER_ALPHA = 0.10f
    const val FACE_INNER_BORDER_STROKE = 0.8f
    const val FACE_INNER_BORDER_OFFSET = 4f

    // Tick marks
    const val TICK_RING_MULTIPLIER = 0.92f
    const val TICK_COUNT = 36
    const val DEGREES_PER_TICK = 10

    const val CARDINAL_TICK_LENGTH = 0.18f
    const val HALF_CARDINAL_TICK_LENGTH = 0.12f
    const val QUARTER_TICK_LENGTH = 0.08f
    const val MINOR_TICK_LENGTH = 0.05f

    const val CARDINAL_TICK_ALPHA = 0.65f
    const val HALF_CARDINAL_TICK_ALPHA = 0.45f
    const val QUARTER_TICK_ALPHA = 0.30f
    const val MINOR_TICK_ALPHA = 0.18f

    const val CARDINAL_TICK_STROKE = 2.8f
    const val HALF_CARDINAL_TICK_STROKE = 1.8f
    const val MINOR_TICK_STROKE = 1.0f

    // Labels
    const val LABEL_RADIUS_MULTIPLIER = 0.76f
    const val LABEL_DOT_RADIUS = 10f
    const val LABEL_DOT_ALPHA = 0.30f
    const val LABEL_CIRCLE_RADIUS = 5f

    const val N_LABEL_ALPHA = 0.55f
    const val E_LABEL_ALPHA = 0.35f
    const val S_LABEL_ALPHA = 0.45f
    const val W_LABEL_ALPHA = 0.35f

    // Needles
    const val PRIMARY_NEEDLE_LENGTH = 0.58f
    const val PRIMARY_NEEDLE_BASE = 0.16f
    const val PRIMARY_NEEDLE_ALPHA_1 = 0.90f
    const val PRIMARY_NEEDLE_ALPHA_2 = 0.60f

    const val SECONDARY_NEEDLE_LENGTH = 0.50f
    const val SECONDARY_NEEDLE_BASE = 0.14f
    const val SECONDARY_NEEDLE_ALPHA_1 = 0.75f
    const val SECONDARY_NEEDLE_ALPHA_2 = 0.45f

    // Crossbar
    const val CROSSBAR_ALPHA = 0.25f
    const val CROSSBAR_STROKE = 1.5f
    const val CROSSBAR_LENGTH = 0.50f
    const val CROSSBAR_ACCENT_ALPHA = 0.15f
    const val CROSSBAR_ACCENT_RADIUS = 3f

    // Inner rings
    const val INNER_RING_1_RADIUS = 0.35f
    const val INNER_RING_1_ALPHA = 0.08f
    const val INNER_RING_2_RADIUS = 0.28f
    const val INNER_RING_2_ALPHA = 0.12f
    const val INNER_RING_3_RADIUS = 0.20f
    const val INNER_RING_3_ALPHA = 0.06f
    const val INNER_RING_STROKE = 0.8f

    // Rosette
    const val ROSETTE_COUNT = 8
    const val ROSETTE_DEGREES = 45
    const val ROSETTE_INNER_RADIUS = 0.18f
    const val ROSETTE_OUTER_RADIUS = 0.32f
    const val ROSETTE_ALPHA = 0.08f
    const val ROSETTE_STROKE = 0.8f

    // Center jewel
    const val JEWEL_OUTER_RADIUS = 6.5f
    const val JEWEL_OUTER_ALPHA = 0.85f
    const val JEWEL_INNER_RADIUS = 4.5f
    const val JEWEL_WHITE_ALPHA = 0.95f
    const val JEWEL_GOLD_ALPHA = 0.50f
    const val JEWEL_CREAM_ALPHA = 0.30f
    const val JEWEL_SPARKLE_ALPHA = 0.30f
    const val JEWEL_SPARKLE_RADIUS = 1.5f
    const val JEWEL_SPARKLE_OFFSET = 2f
    const val JEWEL_GLOW_RADIUS = 10f
    const val JEWEL_GLOW_ALPHA = 0.20f
    const val JEWEL_GLOW_STROKE = 1f

    // Highlight
    const val HIGHLIGHT_ALPHA = 0.15f
    const val HIGHLIGHT_STROKE = 2f
}