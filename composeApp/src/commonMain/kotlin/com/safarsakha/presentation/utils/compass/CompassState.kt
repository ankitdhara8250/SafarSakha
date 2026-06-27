package com.safarsakha.presentation.utils.compass

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

// =============================================================================
// COMPASS STATE
// =============================================================================

data class CompassState(
    val alpha: Animatable<Float, *>,
    val cardAlpha: Animatable<Float, *>,
    val primaryNeedleRotation: Animatable<Float, *>,
    val secondaryNeedleRotation: Animatable<Float, *>,
    // animationKey increments every time the screen re-enters composition so that
    // LaunchedEffect re-launches even when the SaveableStateHolder keeps the node alive
    // across navigation back-stack pops (nav3 + rememberSaveableStateHolderNavEntryDecorator).
    val animationKey: Int
)

@Composable
fun rememberCompassState(): CompassState {
    // Each of the four Animatables is created once and reused across recompositions —
    // that is correct and intentional.  What was broken is that LaunchedEffect(Unit)
    // never re-triggered on screen re-entry because the SaveableStateHolder kept the
    // entire composition subtree alive, so "Unit" as a key never changed.
    //
    // The fix: maintain a mutable integer key that we increment inside a
    // DisposableEffect every time this composable leaves composition (i.e. the screen
    // is navigated away from).  On the next entry the integer is different, so
    // LaunchedEffect(compassState.animationKey) re-runs the animation from the top.
    val alpha = remember { Animatable(0f) }
    val cardAlpha = remember { Animatable(1f) }
    val primaryNeedleRotation = remember { Animatable(CompassConstants.PRIMARY_NEEDLE_START_ANGLE) }
    val secondaryNeedleRotation = remember { Animatable(CompassConstants.SECONDARY_NEEDLE_START_ANGLE) }

    // Starts at 0; incremented whenever the screen leaves composition.
    var animationKey by remember { mutableIntStateOf(0) }

    // When the screen enters composition for the very first time this effect does
    // nothing.  When it LEAVES (user navigates away) onDispose increments the key.
    // The next time the screen enters, the incremented key is already in place, so
    // LaunchedEffect(animationKey) fires again — replaying the full animation.
    DisposableEffect(Unit) {
        onDispose {
            animationKey++
        }
    }

    return CompassState(
        alpha = alpha,
        cardAlpha = cardAlpha,
        primaryNeedleRotation = primaryNeedleRotation,
        secondaryNeedleRotation = secondaryNeedleRotation,
        animationKey = animationKey
    )
}