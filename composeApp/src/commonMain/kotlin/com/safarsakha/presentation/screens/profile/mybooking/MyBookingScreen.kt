package com.safarsakha.presentation.screens.profile.mybooking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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

// ── Design tokens (matching CreateTourPackageScreen / UserProfileScreen) ──────
private val NavyColor = Color(0xFF0F172A)
private val SkyColor = Color(0xFF0EA5E9)
private val SlateColor = Color(0xFF64748B)
private val BorderColor = Color(0xFFE2E8F0)
private val BgColor = Color(0xFFFFFFFF)
private val LightBgColor = Color(0xFFF8FAFC)
private val ErrorColor = Color(0xFFDC2626)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingScreen(onMenuClick: () -> Unit) {
    // Both the repository and the viewModel factory live inside the same remember{} block
    val viewModel: MyBookingViewModel = viewModel(
        factory = remember {
            object : ViewModelProvider.Factory {
                private val bookingRepository =
                    BookingRepositoryImpl(FirebaseBookingDataSource())

                override fun <T : ViewModel> create(
                    modelClass: KClass<T>,
                    extras: CreationExtras
                ): T {
                    @Suppress("UNCHECKED_CAST")
                    return MyBookingViewModel(
                        getUserBookingsUseCase = GetUserBookingsUseCase(bookingRepository),
                        cancelBookingUseCase = CancelBookingUseCase(bookingRepository)
                    ) as T
                }
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
                title = {
                    Column {
                        Text(
                            text = "My Bookings",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyColor,
                            letterSpacing = (-0.3f).sp
                        )
                        Text(
                            text = "Manage your tour reservations",
                            fontSize = 12.sp,
                            color = SlateColor
                        )
                    }
                },
                navigationIcon = { HamburgerMenuButton(onClick = onMenuClick) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgColor,
                    scrolledContainerColor = BgColor
                ),
                modifier = Modifier.border(
                    width = 1.dp,
                    color = BorderColor.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(0.dp)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBgColor)
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = BgColor,
                contentColor = NavyColor,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = SkyColor
                    )
                },
                modifier = Modifier.border(
                    width = 1.dp,
                    color = BorderColor.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(0.dp)
                )
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) NavyColor else SlateColor
                            )
                        }
                    )
                }
            }

            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SkyColor, strokeWidth = 3.dp)
                }
                uiState.errorMessage != null -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(50))
                                .background(ErrorColor.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⚠️", fontSize = 24.sp)
                        }
                        Text(
                            text = uiState.errorMessage ?: "Something went wrong",
                            fontSize = 14.sp,
                            color = SlateColor,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                        Button(
                            onClick = { viewModel.handleEvent(MyBookingEvent.LoadBookings) },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyColor),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Retry", fontWeight = FontWeight.SemiBold)
                        }
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
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(SkyColor.copy(alpha = 0.08f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when (selectedTab) { 0 -> "🗓️"; 1 -> "✅"; else -> "❌" },
                                        fontSize = 28.sp
                                    )
                                }
                                Text(
                                    text = "No ${tabs[selectedTab].lowercase()} bookings",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = NavyColor
                                )
                                Text(
                                    text = "Your ${tabs[selectedTab].lowercase()} bookings will appear here.",
                                    fontSize = 13.sp,
                                    color = SlateColor.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(
                                items = displayList,
                                key = { booking ->
                                    booking.bookingId.ifBlank { "fallback_${displayList.indexOf(booking)}" }
                                }
                            ) { booking ->
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

// =============================================================================
// PREMIUM BOOKING CARD
// =============================================================================

@Composable
private fun BookingCard(booking: Booking, showCancelAction: Boolean, onCancel: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
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
        colors = CardDefaults.cardColors(containerColor = BgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = BorderColor.copy(alpha = 0.60f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = booking.packageName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = NavyColor,
                        letterSpacing = (-0.2f).sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "ID: ${booking.bookingId.take(8)}…",
                        fontSize = 11.sp,
                        color = SlateColor.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }
                BookingStatusChip(booking.bookingStatus)
            }

            HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Start Date", fontSize = 11.sp, color = SlateColor, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(2.dp))
                    Text(booking.startDate.toString(), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NavyColor)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("End Date", fontSize = 11.sp, color = SlateColor, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(2.dp))
                    Text(booking.endDate.toString(), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NavyColor)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Booked On", fontSize = 11.sp, color = SlateColor, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(2.dp))
                    Text(booking.bookingDate.toString().take(10), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NavyColor)
                }
            }

            HorizontalDivider(color = BorderColor.copy(alpha = 0.4f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Amount", fontSize = 11.sp, color = SlateColor, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(1.dp))
                    Text("₹${booking.totalAmount}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NavyColor)
                }
                PaymentStatusChip(booking.paymentStatus)
            }

            if (showCancelAction) {
                Spacer(Modifier.height(4.dp))
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorColor),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ErrorColor.copy(alpha = 0.6f))
                ) {
                    Text("Cancel Booking", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }

            if (booking.bookingStatus == BookingStatus.CANCELLED && booking.cancellationDate != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ErrorColor.copy(alpha = 0.06f))
                        .border(1.dp, ErrorColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Cancelled on: ${booking.cancellationDate.toString().take(10)}",
                        fontSize = 12.sp,
                        color = ErrorColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// =============================================================================
// CHIPS & DIALOG COMPONENTS
// =============================================================================

@Composable
private fun BookingStatusChip(status: BookingStatus) {
    val (text, bg, fg) = when (status) {
        BookingStatus.UPCOMING -> Triple("Upcoming", SkyColor.copy(alpha = 0.08f), SkyColor)
        BookingStatus.COMPLETED -> Triple("Completed", Color(0xFFF0FDF4), Color(0xFF16A34A))
        BookingStatus.CANCELLED -> Triple("Cancelled", ErrorColor.copy(alpha = 0.08f), ErrorColor)
    }
    Surface(shape = RoundedCornerShape(8.dp), color = bg) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun PaymentStatusChip(status: PaymentStatus) {
    val (text, bg, fg) = when (status) {
        PaymentStatus.SUCCESS -> Triple("Paid ✓", Color(0xFFF0FDF4), Color(0xFF16A34A))
        PaymentStatus.FAILED -> Triple("Failed ✗", ErrorColor.copy(alpha = 0.08f), ErrorColor)
    }
    Surface(shape = RoundedCornerShape(8.dp), color = bg) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun CancelBookingDialog(
    booking: Booking,
    isCancelling: Boolean,
    errorMessage: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isCancelling) onDismiss() },
        title = {
            Text(
                text = "Cancel Booking",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = NavyColor
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Are you sure you want to cancel your booking for:",
                    fontSize = 14.sp,
                    color = SlateColor
                )
                Text(
                    text = booking.packageName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = NavyColor
                )
                Text(
                    text = "${booking.startDate} → ${booking.endDate}",
                    fontSize = 13.sp,
                    color = SlateColor.copy(alpha = 0.8f)
                )
                errorMessage?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = it,
                        color = ErrorColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isCancelling,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorColor,
                    disabledContainerColor = ErrorColor.copy(alpha = 0.45f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isCancelling) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Yes, Cancel", fontWeight = FontWeight.SemiBold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isCancelling) {
                Text("Keep Booking", color = NavyColor, fontWeight = FontWeight.SemiBold)
            }
        },
        containerColor = BgColor,
        shape = RoundedCornerShape(20.dp)
    )
}