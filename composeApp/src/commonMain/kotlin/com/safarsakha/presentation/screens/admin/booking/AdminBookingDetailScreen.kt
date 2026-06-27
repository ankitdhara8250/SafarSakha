package com.safarsakha.presentation.screens.admin.booking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBookingDetailScreen(
    booking: Booking,
    onNavigateBack: () -> Unit
) {
    val nights = (booking.endDate.toEpochDays() - booking.startDate.toEpochDays()).coerceAtLeast(1).toInt()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Booking Detail",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyColor,
                            letterSpacing = (-0.3f).sp
                        )
                        Text(
                            text = "ID: ${booking.bookingId}",
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── STATUS BANNER ────────────────────────────────────────────
            StatusBanner(
                bookingStatus = booking.bookingStatus,
                paymentStatus = booking.paymentStatus,
                bookingId = booking.bookingId
            )

            // ── USER INFORMATION ────────────────────────────────────────
            DetailSection("👤 User Information") {
                DetailItem("User Name", booking.userName)
                DetailItem("User ID", booking.userId)
            }

            // ── TOUR INFORMATION ─────────────────────────────────────────
            DetailSection("🏔️ Tour Information") {
                DetailItem("Package Name", booking.packageName)
                DetailItem("Package ID", booking.packageId)
                DetailItem("Price per Night", "₹${booking.packagePrice}")
            }

            // ── BOOKING INFORMATION ──────────────────────────────────────
            DetailSection("📅 Booking Information") {
                DetailItem("Start Date", booking.startDate.toString())
                DetailItem("End Date", booking.endDate.toString())
                DetailItem("Duration", "$nights night${if (nights != 1) "s" else ""}")
                DetailItem("Booked On", booking.bookingDate.toString().take(19).replace("T", " "))
                DetailItem("Booking Status", bookingStatusLabel(booking.bookingStatus))
                if (booking.cancellationDate != null) {
                    DetailItem("Cancelled On", booking.cancellationDate.toString().take(19).replace("T", " "))
                }
            }

            // ── PAYMENT INFORMATION ──────────────────────────────────────
            DetailSection("💳 Payment Information") {
                DetailItem("Payment Status", paymentStatusLabel(booking.paymentStatus))
                DetailItem("Total Amount", "₹${booking.totalAmount}")
                DetailItem("Calculation", "₹${booking.packagePrice} × $nights = ₹${booking.packagePrice * nights}")
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// =============================================================================
// STATUS BANNER
// =============================================================================

@Composable
private fun StatusBanner(
    bookingStatus: BookingStatus,
    paymentStatus: PaymentStatus,
    bookingId: String
) {
    val statusColor = bookingStatusColor(bookingStatus)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
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
            ),
        shape = RoundedCornerShape(16.dp),
        color = statusColor.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = bookingStatusLabel(bookingStatus),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = statusColor,
                    letterSpacing = (-0.3f).sp
                )
                Text(
                    text = "ID: $bookingId",
                    fontSize = 11.sp,
                    color = SlateColor
                )
            }
            PaymentBadge(paymentStatus)
        }
    }
}

// =============================================================================
// DETAIL SECTION
// =============================================================================

@Composable
private fun DetailSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
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
            ),
        shape = RoundedCornerShape(16.dp),
        color = BgColor,
        border = BorderStroke(1.dp, BorderColor.copy(alpha = 0.4f)),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = NavyColor,
                letterSpacing = (-0.2f).sp
            )
            Spacer(Modifier.height(4.dp))
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
            Spacer(Modifier.height(4.dp))
            content()
        }
    }
}

// =============================================================================
// DETAIL ITEM
// =============================================================================

@Composable
private fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            fontSize = 13.sp,
            color = SlateColor,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = NavyColor,
            modifier = Modifier.weight(0.6f)
        )
    }
}

// =============================================================================
// PAYMENT BADGE
// =============================================================================

@Composable
private fun PaymentBadge(status: PaymentStatus) {
    val (text, bgColor, textColor) = when (status) {
        PaymentStatus.SUCCESS -> Triple("✓ Paid", Color(0xFF059669).copy(alpha = 0.1f), Color(0xFF059669))
        PaymentStatus.FAILED -> Triple("✗ Unpaid", Color(0xFFDC2626).copy(alpha = 0.1f), Color(0xFFDC2626))
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = bgColor,
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.2f))
    ) {
        Text(
            text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

// =============================================================================
// HELPER FUNCTIONS
// =============================================================================

private fun bookingStatusColor(status: BookingStatus): Color = when (status) {
    BookingStatus.UPCOMING -> SkyColor
    BookingStatus.COMPLETED -> Color(0xFF059669)
    BookingStatus.CANCELLED -> Color(0xFFDC2626)
}

private fun bookingStatusLabel(status: BookingStatus): String = when (status) {
    BookingStatus.UPCOMING -> "Upcoming"
    BookingStatus.COMPLETED -> "Completed"
    BookingStatus.CANCELLED -> "Cancelled"
}

private fun paymentStatusLabel(status: PaymentStatus): String = when (status) {
    PaymentStatus.SUCCESS -> "Success"
    PaymentStatus.FAILED -> "Failed"
}