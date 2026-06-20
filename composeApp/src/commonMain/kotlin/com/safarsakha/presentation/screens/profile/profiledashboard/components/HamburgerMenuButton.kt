package com.safarsakha.presentation.screens.profile.profiledashboard.components

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

@Composable
fun HamburgerMenuButton(onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(text = "\u2630", fontSize = 20.sp, color = Color(0xFF1E3A8A))
    }
}