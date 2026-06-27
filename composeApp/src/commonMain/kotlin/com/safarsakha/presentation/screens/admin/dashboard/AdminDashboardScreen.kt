package com.safarsakha.presentation.screens.admin.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookOnline
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.Tour
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Design tokens (matching UserProfileScreen) ──────────────────────────────
private val NavyColor = Color(0xFF0F172A)
private val SkyColor = Color(0xFF0EA5E9)
private val CreamColor = Color(0xFFF4E7D3)
private val SlateColor = Color(0xFF64748B)
private val BorderColor = Color(0xFFE2E8F0)
private val BgColor = Color(0xFFFFFFFF)
private val LightBgColor = Color(0xFFF8FAFC)

@Composable
fun AdminDashboardScreen(
    onTourPackageClick: () -> Unit,
    onBookingClick: () -> Unit,
    onFeedbackEnquiryClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = LightBgColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 24.dp)
        ) {
            // ── HEADER ────────────────────────────────────────────────────
            BrandHeader()

            Spacer(modifier = Modifier.height(32.dp))

            // ── DASHBOARD CARDS ─────────────────────────────────────────
            AdminDashboardCard(
                icon = Icons.Outlined.Tour,
                title = "Tour Package Management",
                description = "Add, update, delete and view tour packages",
                onClick = onTourPackageClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            AdminDashboardCard(
                icon = Icons.Outlined.BookOnline,
                title = "Booking Management",
                description = "View, approve, reject and manage bookings",
                onClick = onBookingClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            AdminDashboardCard(
                icon = Icons.Outlined.Feedback,
                title = "Feedback & Enquiry",
                description = "View user enquiries and respond to feedback",
                onClick = onFeedbackEnquiryClick
            )

            Spacer(modifier = Modifier.weight(1f))

            // ── FOOTER ────────────────────────────────────────────────────
            Text(
                text = "SafarSakha Admin v1.0",
                fontSize = 12.sp,
                color = SlateColor.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// =============================================================================
// BRAND HEADER
// =============================================================================

@Composable
private fun BrandHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Dashboard",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NavyColor,
                    letterSpacing = (-0.5f).sp
                )
                Text(
                    text = "Manage SafarSakha platform",
                    fontSize = 14.sp,
                    color = SlateColor
                )
            }

            // Admin badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SkyColor.copy(alpha = 0.1f))
                    .border(1.dp, SkyColor.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Admin",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = SkyColor
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Decorative line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(SkyColor.copy(alpha = 0.9f), SkyColor.copy(alpha = 0.15f))
                    )
                )
        )
    }
}

// =============================================================================
// ADMIN DASHBOARD CARD
// =============================================================================

@Composable
private fun AdminDashboardCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = NavyColor.copy(alpha = 0.04f),
                spotColor = NavyColor.copy(alpha = 0.08f)
            )
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = NavyColor.copy(alpha = 0.02f),
                spotColor = NavyColor.copy(alpha = 0.04f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.94f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            CreamColor.copy(alpha = 0.85f),
                            BorderColor.copy(alpha = 0.60f),
                            BorderColor.copy(alpha = 0.25f)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    SkyColor.copy(alpha = 0.15f),
                                    SkyColor.copy(alpha = 0.05f)
                                ),
                                radius = 80f
                            )
                        )
                        .border(1.dp, SkyColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = SkyColor
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Content
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NavyColor,
                        letterSpacing = (-0.3f).sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = description,
                        fontSize = 13.sp,
                        color = SlateColor,
                        lineHeight = 18.sp
                    )
                }

                // Arrow indicator
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SkyColor.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "→",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SkyColor
                    )
                }
            }
        }
    }
}