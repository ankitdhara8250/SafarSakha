package com.safarsakha.presentation.screens.profile.profiledashboard.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ── Premium Design Tokens ────────────────────────────────────────────────────
private val NavyColor = Color(0xFF0F172A)

@Composable
fun HamburgerMenuButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = NavyColor
        )
    ) {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Open navigation drawer",
            modifier = Modifier.size(24.dp)
        )
    }
}