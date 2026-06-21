package com.safarsakha.presentation.screens.profile.mybooking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.safarsakha.data.remote.firebase.FirebaseBookingDataSource
import com.safarsakha.data.repository.impl.BookingRepositoryImpl
import com.safarsakha.domain.model.Booking
import com.safarsakha.domain.model.BookingStatus
import com.safarsakha.domain.model.PaymentStatus
import com.safarsakha.domain.usecase.booking.CancelBookingUseCase
import com.safarsakha.domain.usecase.booking.GetUserBookingsUseCase
import com.safarsakha.presentation.screens.profile.profiledashboard.components.HamburgerMenuButton
import kotlin.reflect.KClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingScreen(onMenuClick: () -> Unit) {
    val bookingRepository = remember { BookingRepositoryImpl(FirebaseBookingDataSource()) }
    val viewModel: MyBookingViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
                @Suppress("UNCHECKED_CAST")
                return MyBookingViewModel(
                    getUserBookingsUseCase = GetUserBookingsUseCase(bookingRepository),
                    cancelBookingUseCase = CancelBookingUseCase(bookingRepository)
                ) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Upcoming", "Completed", "Cancelled")

    if (uiState.bookingPendingCancellation != null) {
        CancelBookingDialog(
            booking = uiState.bookingPendingCancellation!!,
            isCancelling = uiState.isCancelling,
            errorMessage = uiState.cancelErrorMessage,
            onConfirm = { viewModel.handleEvent(MyBookingEvent.ConfirmCancelBooking) },
            onDismiss = { viewModel.handleEvent(MyBookingEvent.DismissCancelDialog) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Bookings", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A)) },
                navigationIcon = { HamburgerMenuButton(onClick = onMenuClick) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FB)).padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF1E3A8A),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF1E3A8A)
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title, fontSize = 13.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) Color(0xFF1E3A8A) else Color(0xFF64748B)
                            )
                        }
                    )
                }
            }

            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF1E3A8A))
                }
                uiState.errorMessage != null -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("⚠️", fontSize = 40.sp)
                        Text(uiState.errorMessage ?: "Something went wrong", fontSize = 14.sp, color = Color(0xFF64748B), textAlign = TextAlign.Center)
                        Button(onClick = { viewModel.handleEvent(MyBookingEvent.LoadBookings) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A))) { Text("Retry") }
                    }
                }
                else -> {
                    val displayList = when (selectedTab) {
                        0 -> uiState.upcomingBookings
                        1 -> uiState.completedBookings
                        else -> uiState.cancelledBookings
                    }
                    if (displayList.isEmpty()) {
                        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(when (selectedTab) { 0 -> "🗓️"; 1 -> "✅"; else -> "❌" }, fontSize = 44.sp)
                                Text("No ${tabs[selectedTab].lowercase()} bookings", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color(0xFF0F172A))
                                Text("Your ${tabs[selectedTab].lowercase()} bookings will appear here.", fontSize = 13.sp, color = Color(0xFF64748B), textAlign = TextAlign.Center)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(displayList, key = { it.bookingId }) { booking ->
                                BookingCard(
                                    booking = booking,
                                    showCancelAction = selectedTab == 0,
                                    onCancel = { viewModel.handleEvent(MyBookingEvent.RequestCancelBooking(booking)) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingCard(booking: Booking, showCancelAction: Boolean, onCancel: () -> Unit) {
    Surface(shape = RoundedCornerShape(12.dp), color = Color.White, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(booking.packageName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                    Spacer(Modifier.height(2.dp))
                    Text("ID: ${booking.bookingId.take(8)}…", fontSize = 11.sp, color = Color(0xFF94A3B8))
                }
                BookingStatusChip(booking.bookingStatus)
            }
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column { Text("Start", fontSize = 11.sp, color = Color(0xFF64748B)); Text(booking.startDate.toString(), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A)) }
                Column { Text("End", fontSize = 11.sp, color = Color(0xFF64748B)); Text(booking.endDate.toString(), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A)) }
                Column { Text("Booked On", fontSize = 11.sp, color = Color(0xFF64748B)); Text(booking.bookingDate.toString().take(10), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A)) }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Total Amount", fontSize = 11.sp, color = Color(0xFF64748B))
                    Text("₹${booking.totalAmount}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                }
                PaymentStatusChip(booking.paymentStatus)
            }
            if (showCancelAction) {
                Spacer(Modifier.height(4.dp))
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDC2626))
                ) { Text("Cancel Booking", fontWeight = FontWeight.Medium, fontSize = 13.sp) }
            }
            if (booking.bookingStatus == BookingStatus.CANCELLED && booking.cancellationDate != null) {
                Text("Cancelled on: ${booking.cancellationDate.toString().take(10)}", fontSize = 11.sp, color = Color(0xFFDC2626))
            }
        }
    }
}

@Composable
private fun BookingStatusChip(status: BookingStatus) {
    val (text, bg, fg) = when (status) {
        BookingStatus.UPCOMING -> Triple("Upcoming", Color(0xFFEFF6FF), Color(0xFF1E3A8A))
        BookingStatus.COMPLETED -> Triple("Completed", Color(0xFFF0FDF4), Color(0xFF16A34A))
        BookingStatus.CANCELLED -> Triple("Cancelled", Color(0xFFFEF2F2), Color(0xFFDC2626))
    }
    Surface(shape = RoundedCornerShape(50), color = bg) {
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = fg, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
    }
}

@Composable
private fun PaymentStatusChip(status: PaymentStatus) {
    val (text, bg, fg) = when (status) {
        PaymentStatus.SUCCESS -> Triple("Paid ✓", Color(0xFFF0FDF4), Color(0xFF16A34A))
        PaymentStatus.FAILED -> Triple("Failed ✗", Color(0xFFFEF2F2), Color(0xFFDC2626))
    }
    Surface(shape = RoundedCornerShape(50), color = bg) {
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = fg, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
    }
}

@Composable
private fun CancelBookingDialog(booking: Booking, isCancelling: Boolean, errorMessage: String?, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { if (!isCancelling) onDismiss() },
        title = { Text("Cancel Booking", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Are you sure you want to cancel your booking for:", fontSize = 14.sp, color = Color(0xFF64748B))
                Text(booking.packageName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                Text("${booking.startDate} → ${booking.endDate}", fontSize = 13.sp, color = Color(0xFF64748B))
                errorMessage?.let { Spacer(Modifier.height(4.dp)); Text(it, color = Color(0xFFDC2626), fontSize = 13.sp) }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = !isCancelling, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))) {
                if (isCancelling) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Yes, Cancel")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isCancelling) { Text("Keep Booking", color = Color(0xFF1E3A8A)) }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}