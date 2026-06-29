package com.safarsakha.presentation.screens.profile.tours.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Premium Design Tokens ────────────────────────────────────────────────────
private val NavyColor = Color(0xFF0F172A)
private val SkyColor = Color(0xFF0EA5E9)
private val SlateColor = Color(0xFF64748B)

@Composable
fun EmptyToursState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Premium soft-tint badge container for the icon
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(RoundedCornerShape(50))
                .background(SkyColor.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🧳",
                fontSize = 30.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "No Tours Available Right Now",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = NavyColor,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.3f).sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "We're working on adding new tour packages. Please check back soon!",
            fontSize = 13.sp,
            color = SlateColor,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}