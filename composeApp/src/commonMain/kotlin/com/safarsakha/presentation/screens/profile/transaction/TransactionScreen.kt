package com.safarsakha.presentation.screens.profile.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import com.safarsakha.data.remote.firebase.FirebaseTransactionDataSource
import com.safarsakha.data.repository.impl.TransactionRepositoryImpl
import com.safarsakha.domain.model.PaymentStatus
import com.safarsakha.domain.model.Transaction
import com.safarsakha.domain.usecase.transaction.CreateTransactionUseCase
import com.safarsakha.domain.usecase.transaction.GetUserTransactionsUseCase
import com.safarsakha.presentation.screens.profile.profiledashboard.components.HamburgerMenuButton
import kotlin.reflect.KClass

private enum class TransactionFilter { ALL, SUCCESS, FAILED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(onMenuClick: () -> Unit) {
    val transactionRepository = remember { TransactionRepositoryImpl(FirebaseTransactionDataSource()) }
    val viewModel: TransactionViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
                @Suppress("UNCHECKED_CAST")
                return TransactionViewModel(
                    getUserTransactionsUseCase = GetUserTransactionsUseCase(transactionRepository),
                    createTransactionUseCase = CreateTransactionUseCase(transactionRepository)
                ) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()
    var activeFilter by remember { mutableStateOf(TransactionFilter.ALL) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A)) },
                navigationIcon = { HamburgerMenuButton(onClick = onMenuClick) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FB)).padding(paddingValues)) {
            Row(
                modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TransactionFilterChip("All", activeFilter == TransactionFilter.ALL) { activeFilter = TransactionFilter.ALL }
                TransactionFilterChip("✓ Success", activeFilter == TransactionFilter.SUCCESS) { activeFilter = TransactionFilter.SUCCESS }
                TransactionFilterChip("✗ Failed", activeFilter == TransactionFilter.FAILED) { activeFilter = TransactionFilter.FAILED }
            }

            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF1E3A8A))
                }
                uiState.errorMessage != null -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("⚠️", fontSize = 40.sp)
                        Text(uiState.errorMessage ?: "Something went wrong", fontSize = 14.sp, color = Color(0xFF64748B), textAlign = TextAlign.Center)
                        Button(onClick = { viewModel.handleEvent(TransactionEvent.LoadTransactions) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A))) { Text("Retry") }
                    }
                }
                else -> {
                    val displayList = when (activeFilter) {
                        TransactionFilter.ALL -> uiState.allTransactionsSorted
                        TransactionFilter.SUCCESS -> uiState.successfulTransactions
                        TransactionFilter.FAILED -> uiState.failedTransactions
                    }

                    if (uiState.transactions.isNotEmpty()) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SummaryCard("✅", "Successful", uiState.successfulTransactions.size, uiState.successfulTransactions.sumOf { it.amount }, Color(0xFF16A34A), Modifier.weight(1f))
                            SummaryCard("❌", "Failed", uiState.failedTransactions.size, uiState.failedTransactions.sumOf { it.amount }, Color(0xFFDC2626), Modifier.weight(1f))
                        }
                    }

                    if (displayList.isEmpty()) {
                        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("💳", fontSize = 44.sp)
                                Text(when (activeFilter) { TransactionFilter.ALL -> "No transactions yet"; TransactionFilter.SUCCESS -> "No successful transactions"; TransactionFilter.FAILED -> "No failed transactions" }, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color(0xFF0F172A))
                                Text("Your transaction history will appear here once you make a booking.", fontSize = 13.sp, color = Color(0xFF64748B), textAlign = TextAlign.Center)
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(displayList, key = { it.transactionId }) { transaction ->
                                TransactionCard(transaction = transaction)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(50), color = if (selected) Color(0xFF1E3A8A) else Color(0xFFF1F5F9), modifier = Modifier.height(32.dp)) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 14.dp)) {
            Text(label, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, color = if (selected) Color.White else Color(0xFF64748B))
        }
    }
}

@Composable
private fun SummaryCard(emoji: String, label: String, count: Int, amount: Double, color: Color, modifier: Modifier = Modifier) {
    Surface(shape = RoundedCornerShape(10.dp), color = Color.White, shadowElevation = 1.dp, modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(emoji, fontSize = 14.sp); Text(label, fontSize = 12.sp, color = Color(0xFF64748B))
            }
            Text("$count transaction${if (count != 1) "s" else ""}", fontSize = 11.sp, color = Color(0xFF94A3B8))
            Text("₹$amount", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
        }
    }
}

@Composable
private fun TransactionCard(transaction: Transaction) {
    Surface(shape = RoundedCornerShape(12.dp), color = Color.White, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("TXN #${transaction.transactionId.take(8).uppercase()}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                    Spacer(Modifier.height(2.dp))
                    Text("Booking: ${transaction.bookingId.take(8)}…", fontSize = 11.sp, color = Color(0xFF94A3B8))
                }
                TransactionStatusBadge(transaction.paymentStatus)
            }
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    DetailRow("Method", transaction.paymentMethod)
                    DetailRow("Date", transaction.transactionDate.toString().take(10))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Amount", fontSize = 11.sp, color = Color(0xFF64748B))
                    Text("₹${transaction.amount}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = if (transaction.paymentStatus == PaymentStatus.SUCCESS) Color(0xFF16A34A) else Color(0xFFDC2626))
                }
            }
        }
    }
}

@Composable
private fun TransactionStatusBadge(status: PaymentStatus) {
    val (text, bg, fg) = when (status) {
        PaymentStatus.SUCCESS -> Triple("Success", Color(0xFFF0FDF4), Color(0xFF16A34A))
        PaymentStatus.FAILED -> Triple("Failed", Color(0xFFFEF2F2), Color(0xFFDC2626))
    }
    Surface(shape = RoundedCornerShape(50), color = bg) {
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = fg, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("$label:", fontSize = 12.sp, color = Color(0xFF64748B))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
    }
}