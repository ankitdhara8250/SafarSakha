package com.safarsakha.presentation.screens.admin.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safarsakha.domain.model.Booking
import com.safarsakha.domain.model.BookingStatus
import com.safarsakha.domain.model.PaymentStatus

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
                title = { Text("Booking Detail", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A)) },
                navigationIcon = { TextButton(onClick = onNavigateBack) { Text("Back", color = Color(0xFF1E3A8A)) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FB)).padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(shape = RoundedCornerShape(12.dp), color = bookingStatusColor(booking.bookingStatus).copy(alpha = 0.1f), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(bookingStatusLabel(booking.bookingStatus), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = bookingStatusColor(booking.bookingStatus))
                        Text("ID: ${booking.bookingId}", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                    PaymentBadge(booking.paymentStatus)
                }
            }

            DetailSection("👤 User Information") {
                DetailItem("User Name", booking.userName)
                DetailItem("User ID", booking.userId)
            }

            DetailSection("🏔️ Tour Information") {
                DetailItem("Package Name", booking.packageName)
                DetailItem("Package ID", booking.packageId)
                DetailItem("Price per Night", "₹${booking.packagePrice}")
            }

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

            DetailSection("💳 Payment Information") {
                DetailItem("Payment Status", paymentStatusLabel(booking.paymentStatus))
                DetailItem("Total Amount", "₹${booking.totalAmount}")
                DetailItem("Calculation", "₹${booking.packagePrice} × $nights = ₹${booking.packagePrice * nights}")
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DetailSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = RoundedCornerShape(12.dp), color = Color.White, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E3A8A))
            Spacer(Modifier.height(4.dp))
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Spacer(Modifier.height(4.dp))
            content()
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = Color(0xFF64748B), modifier = Modifier.weight(0.45f))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A), modifier = Modifier.weight(0.55f))
    }
}

@Composable
private fun PaymentBadge(status: PaymentStatus) {
    val (text, bg, fg) = when (status) {
        PaymentStatus.SUCCESS -> Triple("✓ Paid", Color(0xFFF0FDF4), Color(0xFF16A34A))
        PaymentStatus.FAILED -> Triple("✗ Unpaid", Color(0xFFFEF2F2), Color(0xFFDC2626))
    }
    Surface(shape = RoundedCornerShape(50), color = bg) {
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = fg, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp))
    }
}

private fun bookingStatusColor(status: BookingStatus) = when (status) {
    BookingStatus.UPCOMING -> Color(0xFF1E3A8A)
    BookingStatus.COMPLETED -> Color(0xFF16A34A)
    BookingStatus.CANCELLED -> Color(0xFFDC2626)
}

private fun bookingStatusLabel(status: BookingStatus) = when (status) {
    BookingStatus.UPCOMING -> "Upcoming"
    BookingStatus.COMPLETED -> "Completed"
    BookingStatus.CANCELLED -> "Cancelled"
}

private fun paymentStatusLabel(status: PaymentStatus) = when (status) {
    PaymentStatus.SUCCESS -> "Success"
    PaymentStatus.FAILED -> "Failed"
}