package com.safarsakha.presentation.screens.admin.booking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safarsakha.domain.model.Booking
import com.safarsakha.domain.model.BookingStatus
import com.safarsakha.domain.model.PaymentStatus

// ── Design tokens (matching UserProfileScreen) ──────────────────────────────
private val NavyColor = Color(0xFF0F172A)
private val SkyColor = Color(0xFF0EA5E9)
private val SlateColor = Color(0xFF64748B)
private val BorderColor = Color(0xFFE2E8F0)
private val BgColor = Color(0xFFFFFFFF)
private val LightBgColor = Color(0xFFF8FAFC)
private val SuccessColor = Color(0xFF059669)
private val ErrorColor = Color(0xFFDC2626)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBookingListScreen(
    viewModel: AdminBookingViewModel,
    onNavigateBack: () -> Unit,
    onBookingClick: (Booking) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Upcoming", "Previous", "Cancelled")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Booking Management",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyColor,
                            letterSpacing = (-0.3f).sp
                        )
                        Text(
                            text = "Manage all bookings",
                            fontSize = 12.sp,
                            color = SlateColor
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = NavyColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgColor,
                    scrolledContainerColor = BgColor
                ),
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = BorderColor.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(0.dp)
                    )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBgColor)
                .padding(padding)
        ) {
            // ── STATS CHIPS ──────────────────────────────────────────────
            if (!uiState.isLoading && uiState.errorMessage == null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgColor)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdminStatChip(
                        text = "📅 ${uiState.upcomingBookings.size} Upcoming",
                        color = SkyColor,
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatChip(
                        text = "✅ ${uiState.previousBookings.size} Done",
                        color = SuccessColor,
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatChip(
                        text = "❌ ${uiState.cancelledBookings.size} Cancelled",
                        color = ErrorColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── TAB ROW ──────────────────────────────────────────────────
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = BgColor,
                contentColor = NavyColor,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = SkyColor,
                        height = 3.dp
                    )
                },
                divider = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(BorderColor.copy(alpha = 0.3f))
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontSize = 14.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == index) NavyColor else SlateColor
                            )
                        }
                    )
                }
            }

            // ── CONTENT ──────────────────────────────────────────────────
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = SkyColor,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Loading bookings...",
                                fontSize = 14.sp,
                                color = SlateColor
                            )
                        }
                    }
                }

                uiState.errorMessage != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(ErrorColor.copy(alpha = 0.08f))
                                    .border(1.dp, ErrorColor.copy(alpha = 0.15f), RoundedCornerShape(50)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("⚠️", fontSize = 32.sp)
                            }
                            Text(
                                uiState.errorMessage ?: "Error loading bookings",
                                fontSize = 14.sp,
                                color = SlateColor,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = { viewModel.handleEvent(AdminBookingEvent.LoadAllBookings) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NavyColor,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text(
                                    "Retry",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                else -> {
                    val list = when (selectedTab) {
                        0 -> uiState.upcomingBookings
                        1 -> uiState.previousBookings
                        else -> uiState.cancelledBookings
                    }

                    if (list.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(SkyColor.copy(alpha = 0.08f))
                                        .border(1.dp, SkyColor.copy(alpha = 0.15f), RoundedCornerShape(50)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        when (selectedTab) {
                                            0 -> "🗓️"
                                            1 -> "✅"
                                            else -> "❌"
                                        },
                                        fontSize = 32.sp
                                    )
                                }
                                Text(
                                    text = "No ${tabs[selectedTab].lowercase()} bookings",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = NavyColor
                                )
                                Text(
                                    text = "All ${tabs[selectedTab].lowercase()} bookings will appear here",
                                    fontSize = 13.sp,
                                    color = SlateColor
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(list, key = { it.bookingId }) { booking ->
                                AdminBookingCard(
                                    booking = booking,
                                    onClick = { onBookingClick(booking) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// STAT CHIP
// =============================================================================

@Composable
private fun AdminStatChip(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.15f)),
        modifier = modifier
    ) {
        Text(
            text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .fillMaxWidth()
        )
    }
}

// =============================================================================
// BOOKING CARD
// =============================================================================

@Composable
private fun AdminBookingCard(
    booking: Booking,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = BgColor,
        shadowElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = NavyColor.copy(alpha = 0.04f),
                spotColor = NavyColor.copy(alpha = 0.08f)
            )
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = NavyColor.copy(alpha = 0.02f),
                spotColor = NavyColor.copy(alpha = 0.04f)
            )
            .border(
                width = 1.dp,
                color = BorderColor.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        booking.packageName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = NavyColor,
                        letterSpacing = (-0.2f).sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "👤 ${booking.userName}",
                        fontSize = 13.sp,
                        color = SlateColor
                    )
                }
                AdminBookingStatusChip(booking.bookingStatus)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                BorderColor.copy(alpha = 0.1f),
                                BorderColor.copy(alpha = 0.5f),
                                BorderColor.copy(alpha = 0.1f)
                            )
                        )
                    )
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                LabelValue("Start", booking.startDate.toString())
                LabelValue("End", booking.endDate.toString())
                LabelValue("Booked", booking.bookingDate.toString().take(10))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "₹${booking.totalAmount}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = SuccessColor // Green color for amount
                )
                AdminPaymentChip(booking.paymentStatus)
            }

            Text(
                "Tap to view details →",
                fontSize = 11.sp,
                color = SlateColor.copy(alpha = 0.6f)
            )
        }
    }
}

// =============================================================================
// LABEL VALUE
// =============================================================================

@Composable
private fun LabelValue(label: String, value: String) {
    Column {
        Text(
            label,
            fontSize = 10.sp,
            color = SlateColor.copy(alpha = 0.6f)
        )
        Text(
            value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = NavyColor
        )
    }
}

// =============================================================================
// BOOKING STATUS CHIP
// =============================================================================

@Composable
private fun AdminBookingStatusChip(status: BookingStatus) {
    val (text, bg, fg) = when (status) {
        BookingStatus.UPCOMING -> Triple("Upcoming", SkyColor.copy(alpha = 0.08f), SkyColor)
        BookingStatus.COMPLETED -> Triple("Completed", SuccessColor.copy(alpha = 0.08f), SuccessColor)
        BookingStatus.CANCELLED -> Triple("Cancelled", ErrorColor.copy(alpha = 0.08f), ErrorColor)
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = bg,
        border = BorderStroke(1.dp, fg.copy(alpha = 0.2f))
    ) {
        Text(
            text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

// =============================================================================
// PAYMENT CHIP
// =============================================================================

@Composable
private fun AdminPaymentChip(status: PaymentStatus) {
    val (text, bg, fg) = when (status) {
        PaymentStatus.SUCCESS -> Triple("Paid", SuccessColor.copy(alpha = 0.08f), SuccessColor)
        PaymentStatus.FAILED -> Triple("Unpaid", ErrorColor.copy(alpha = 0.08f), ErrorColor)
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = bg,
        border = BorderStroke(1.dp, fg.copy(alpha = 0.2f))
    ) {
        Text(
            text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}