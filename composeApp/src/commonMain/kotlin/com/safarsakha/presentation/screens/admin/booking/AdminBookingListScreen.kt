package com.safarsakha.presentation.screens.admin.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.safarsakha.domain.usecase.booking.GetAllBookingsUseCase
import kotlin.reflect.KClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBookingListScreen(
    onNavigateBack: () -> Unit,
    onBookingClick: (Booking) -> Unit
) {
    val bookingRepository = remember { BookingRepositoryImpl(FirebaseBookingDataSource()) }
    val viewModel: AdminBookingViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
                @Suppress("UNCHECKED_CAST")
                return AdminBookingViewModel(GetAllBookingsUseCase(bookingRepository)) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Upcoming", "Previous", "Cancelled")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Booking Management", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A)) },
                navigationIcon = { TextButton(onClick = onNavigateBack) { Text("Back", color = Color(0xFF1E3A8A)) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FB)).padding(padding)) {
            if (!uiState.isLoading && uiState.errorMessage == null) {
                Row(modifier = Modifier.fillMaxWidth().background(Color.White).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AdminStatChip("📅 ${uiState.upcomingBookings.size} Upcoming", Color(0xFF1E3A8A), Modifier.weight(1f))
                    AdminStatChip("✅ ${uiState.previousBookings.size} Done", Color(0xFF16A34A), Modifier.weight(1f))
                    AdminStatChip("❌ ${uiState.cancelledBookings.size} Cancelled", Color(0xFFDC2626), Modifier.weight(1f))
                }
            }

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF1E3A8A),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]), color = Color(0xFF1E3A8A))
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontSize = 13.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal, color = if (selectedTab == index) Color(0xFF1E3A8A) else Color(0xFF64748B)) }
                    )
                }
            }

            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFF1E3A8A)) }
                uiState.errorMessage != null -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("⚠️", fontSize = 40.sp)
                        Text(uiState.errorMessage ?: "Error", fontSize = 14.sp, color = Color(0xFF64748B), textAlign = TextAlign.Center)
                        Button(onClick = { viewModel.handleEvent(AdminBookingEvent.LoadAllBookings) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A))) { Text("Retry") }
                    }
                }
                else -> {
                    val list = when (selectedTab) { 0 -> uiState.upcomingBookings; 1 -> uiState.previousBookings; else -> uiState.cancelledBookings }
                    if (list.isEmpty()) {
                        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(when (selectedTab) { 0 -> "🗓️"; 1 -> "✅"; else -> "❌" }, fontSize = 44.sp)
                                Text("No ${tabs[selectedTab].lowercase()} bookings", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color(0xFF0F172A))
                            }
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(list, key = { it.bookingId }) { booking ->
                                AdminBookingCard(booking = booking, onClick = { onBookingClick(booking) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminStatChip(text: String, color: Color, modifier: Modifier = Modifier) {
    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.1f), modifier = modifier) {
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp).fillMaxWidth())
    }
}

@Composable
private fun AdminBookingCard(booking: Booking, onClick: () -> Unit) {
    Surface(shape = RoundedCornerShape(12.dp), color = Color.White, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(booking.packageName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                    Text("👤 ${booking.userName}", fontSize = 13.sp, color = Color(0xFF64748B))
                }
                AdminBookingStatusChip(booking.bookingStatus)
            }
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                LabelValue("Start", booking.startDate.toString())
                LabelValue("End", booking.endDate.toString())
                LabelValue("Booked", booking.bookingDate.toString().take(10))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("₹${booking.totalAmount}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E3A8A))
                AdminPaymentChip(booking.paymentStatus)
            }
            Text("Tap to view details →", fontSize = 11.sp, color = Color(0xFF94A3B8))
        }
    }
}

@Composable
private fun LabelValue(label: String, value: String) {
    Column {
        Text(label, fontSize = 10.sp, color = Color(0xFF94A3B8))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
    }
}

@Composable
private fun AdminBookingStatusChip(status: BookingStatus) {
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
private fun AdminPaymentChip(status: PaymentStatus) {
    val (text, bg, fg) = when (status) {
        PaymentStatus.SUCCESS -> Triple("Paid", Color(0xFFF0FDF4), Color(0xFF16A34A))
        PaymentStatus.FAILED -> Triple("Unpaid", Color(0xFFFEF2F2), Color(0xFFDC2626))
    }
    Surface(shape = RoundedCornerShape(50), color = bg) {
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = fg, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
    }
}