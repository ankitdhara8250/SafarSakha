package com.safarsakha.presentation.utils.compass

import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.EaseInOutQuint
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.EaseOutExpo
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch

@Composable
fun LaunchedCompassAnimation(
    compassState: CompassState
) {
    LaunchedEffect(compassState.animationKey) {
        val compassAlpha = compassState.alpha
        val cardAlpha = compassState.cardAlpha
        val primaryNeedle = compassState.primaryNeedleRotation
        val secondaryNeedle = compassState.secondaryNeedleRotation

        // Snap to start positions (safety reset)
        compassAlpha.snapTo(0f)
        cardAlpha.snapTo(1f)
        primaryNeedle.snapTo(CompassConstants.PRIMARY_NEEDLE_START_ANGLE)
        secondaryNeedle.snapTo(CompassConstants.SECONDARY_NEEDLE_START_ANGLE)

        // Small delay so the screen is fully drawn before animating.
        kotlinx.coroutines.delay(80)

        // ── SEQUENTIAL ALPHA LOGIC (Fade In -> Hold -> Fade Out -> Restore Card) ──
        launch {
            // 1. FADE IN COMPASS
            compassAlpha.animateTo(
                targetValue = CompassConstants.MAX_ALPHA,
                animationSpec = tween(
                    CompassConstants.FADE_IN_DURATION_MS,
                    easing = EaseOutExpo
                )
            )

            // 2. WAIT (Hold period + Needle swinging time)
            val holdTime = CompassConstants.NEEDLE_ROTATION_DURATION_MS +
                    CompassConstants.HOLD_DURATION_MS
            kotlinx.coroutines.delay(holdTime.toLong())

            // 3. FADE OUT COMPASS
            compassAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = CompassConstants.FADE_OUT_DURATION_MS,
                    easing = EaseInOutQuint
                )
            )

            // 4. RESTORE CARD ALPHA
            kotlinx.coroutines.delay(CompassConstants.CARD_RESTORE_DELAY_MS.toLong())
            cardAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = CompassConstants.CARD_RESTORE_DURATION_MS,
                    easing = EaseInOutQuad
                )
            )
        }

        // ── CARD GOES SEMI-TRANSPARENT (Happens at start) ────────────
        launch {
            cardAlpha.animateTo(
                targetValue = CompassConstants.CARD_MIN_ALPHA,
                animationSpec = tween(
                    CompassConstants.CARD_ALPHA_DURATION_MS,
                    easing = EaseInOutQuad
                )
            )
        }

        // ── PRIMARY NEEDLE ─────────────────────────────────────────────
        launch {
            primaryNeedle.animateTo(
                targetValue = CompassConstants.PRIMARY_NEEDLE_END_ANGLE,
                animationSpec = tween(
                    durationMillis = CompassConstants.NEEDLE_ROTATION_DURATION_MS,
                    delayMillis = 200, // delayMillis inside tween is fine here because no other coroutine is fighting for this needle!
                    easing = EaseOutBack
                )
            )
        }

        // ── SECONDARY NEEDLE ───────────────────────────────────────────
        launch {
            secondaryNeedle.animateTo(
                targetValue = CompassConstants.SECONDARY_NEEDLE_END_ANGLE,
                animationSpec = tween(
                    durationMillis = CompassConstants.NEEDLE_ROTATION_DURATION_MS + 300,
                    delayMillis = 300,
                    easing = EaseOutCubic
                )
            )
        }
    }
}