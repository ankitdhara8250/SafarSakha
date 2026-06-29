package com.safarsakha.presentation.screens.profile.tours.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.safarsakha.domain.model.TourPackage

// ── Premium Design Tokens ────────────────────────────────────────────────────
private val NavyColor = Color(0xFF0F172A)
private val SkyColor = Color(0xFF0EA5E9)
private val SlateColor = Color(0xFF64748B)
private val BorderColor = Color(0xFFE2E8F0)
private val BgColor = Color(0xFFFFFFFF)
private val ErrorColor = Color(0xFFDC2626)
private val SuccessColor = Color(0xFF16A34A)
private val SuccessBgColor = Color(0xFFF0FDF4)

@Composable
fun UserTourCard(
    tourPackage: TourPackage,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = BorderColor.copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // Zero out raw shadow for premium flat border style
        colors = CardDefaults.cardColors(containerColor = BgColor)
    ) {
        Column {
            // Image & Price Banner Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(BorderColor.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                if (!tourPackage.imageUrl.isNullOrEmpty()) {
                    var isLoading by remember { mutableStateOf(true) }
                    var isError by remember { mutableStateOf(false) }

                    AsyncImage(
                        model = tourPackage.imageUrl,
                        contentDescription = tourPackage.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        onState = { state ->
                            isLoading = state is AsyncImagePainter.State.Loading
                            isError = state is AsyncImagePainter.State.Error
                        }
                    )

                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = SkyColor,
                            strokeWidth = 2.5.dp
                        )
                    }

                    if (isError) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("⚠️", fontSize = 24.sp)
                            Text("Load Failed", color = ErrorColor, fontSize = 10.sp)
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📷", fontSize = 32.sp)
                        Text("No Image Available", color = SlateColor.copy(alpha = 0.6f), fontSize = 12.sp)
                    }
                }

                // Green Highlighted Amount Badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = SuccessBgColor
                ) {
                    Text(
                        text = "₹${tourPackage.price}",
                        color = SuccessColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            // Descriptive Information Content Section
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = tourPackage.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    letterSpacing = (-0.3f).sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Location & Duration Metadata Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📍", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = tourPackage.location,
                        fontSize = 13.sp,
                        color = SlateColor
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "⏱️", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = tourPackage.duration,
                        fontSize = 13.sp,
                        color = SlateColor
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = tourPackage.description,
                    fontSize = 13.sp,
                    color = NavyColor.copy(alpha = 0.75f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive Call To Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "View Details",
                        color = SkyColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "→",
                        color = SkyColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}