package com.safarsakha.presentation.screens.profile.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import com.safarsakha.data.remote.firebase.FirebaseTransactionDataSource
import com.safarsakha.data.repository.impl.TransactionRepositoryImpl
import com.safarsakha.domain.model.PaymentStatus
import com.safarsakha.domain.model.Transaction
import com.safarsakha.domain.usecase.transaction.CreateTransactionUseCase
import com.safarsakha.domain.usecase.transaction.GetUserTransactionsUseCase
import com.safarsakha.presentation.screens.profile.profiledashboard.components.HamburgerMenuButton
import kotlin.reflect.KClass

private enum class TransactionFilter { ALL, SUCCESS, FAILED }

// ── Premium Design Tokens ────────────────────────────────────────────────────
private val NavyColor = Color(0xFF0F172A)
private val SkyColor = Color(0xFF0EA5E9)
private val SlateColor = Color(0xFF64748B)
private val BorderColor = Color(0xFFE2E8F0)
private val BgColor = Color(0xFFFFFFFF)
private val LightBgColor = Color(0xFFF8FAFC)
private val ErrorColor = Color(0xFFDC2626)
private val SuccessColor = Color(0xFF16A34A)
private val SuccessBgColor = Color(0xFFF0FDF4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(onMenuClick: () -> Unit) {
    val viewModel: TransactionViewModel = viewModel(
        factory = remember {
            object : ViewModelProvider.Factory {
                private val transactionRepository =
                    TransactionRepositoryImpl(FirebaseTransactionDataSource())

                override fun <T : ViewModel> create(
                    modelClass: KClass<T>,
                    extras: CreationExtras
                ): T {
                    @Suppress("UNCHECKED_CAST")
                    return TransactionViewModel(
                        getUserTransactionsUseCase = GetUserTransactionsUseCase(transactionRepository),
                        createTransactionUseCase = CreateTransactionUseCase(transactionRepository)
                    ) as T
                }
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()
    var activeFilter by remember { mutableStateOf(TransactionFilter.ALL) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Transactions",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyColor,
                            letterSpacing = (-0.3f).sp
                        )
                        Text(
                            text = "View your payment history",
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
            // Filter Chips Container
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgColor)
                    .border(
                        width = 1.dp,
                        color = BorderColor.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(0.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TransactionFilterChip("All", activeFilter == TransactionFilter.ALL) { activeFilter = TransactionFilter.ALL }
                TransactionFilterChip("✓ Success", activeFilter == TransactionFilter.SUCCESS) { activeFilter = TransactionFilter.SUCCESS }
                TransactionFilterChip("✗ Failed", activeFilter == TransactionFilter.FAILED) { activeFilter = TransactionFilter.FAILED }
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
                            onClick = { viewModel.handleEvent(TransactionEvent.LoadTransactions) },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyColor),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Retry", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                else -> {
                    val displayList = when (activeFilter) {
                        TransactionFilter.ALL -> uiState.allTransactionsSorted
                        TransactionFilter.SUCCESS -> uiState.successfulTransactions
                        TransactionFilter.FAILED -> uiState.failedTransactions
                    }

                    if (uiState.transactions.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SummaryCard(
                                emoji = "✅",
                                label = "Successful",
                                count = uiState.successfulTransactions.size,
                                amount = uiState.successfulTransactions.sumOf { it.amount },
                                color = SuccessColor,
                                bgColor = SuccessBgColor,
                                modifier = Modifier.weight(1f)
                            )
                            SummaryCard(
                                emoji = "❌",
                                label = "Failed",
                                count = uiState.failedTransactions.size,
                                amount = uiState.failedTransactions.sumOf { it.amount },
                                color = ErrorColor,
                                bgColor = ErrorColor.copy(alpha = 0.06f),
                                modifier = Modifier.weight(1f)
                            )
                        }
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
                                    Text("💳", fontSize = 28.sp)
                                }
                                Text(
                                    text = when (activeFilter) {
                                        TransactionFilter.ALL -> "No transactions yet"
                                        TransactionFilter.SUCCESS -> "No successful transactions"
                                        TransactionFilter.FAILED -> "No failed transactions"
                                    },
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = NavyColor
                                )
                                Text(
                                    text = "Your transaction history will appear here once you make a booking.",
                                    fontSize = 13.sp,
                                    color = SlateColor.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp, top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(
                                items = displayList,
                                key = { transaction ->
                                    transaction.transactionId.ifBlank { "fallback_${displayList.indexOf(transaction)}" }
                                }
                            ) { transaction ->
                                TransactionCard(transaction = transaction)
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// PREMIUM SUB-COMPONENTS
// =============================================================================

@Composable
private fun TransactionFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) NavyColor else LightBgColor,
        border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, BorderColor.copy(alpha = 0.8f)),
        modifier = Modifier.height(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) Color.White else SlateColor
            )
        }
    }
}

@Composable
private fun SummaryCard(
    emoji: String,
    label: String,
    count: Int,
    amount: Double,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = NavyColor.copy(alpha = 0.02f),
                spotColor = NavyColor.copy(alpha = 0.04f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = BorderColor.copy(alpha = 0.6f), shape = RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, fontSize = 11.sp)
                }
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyColor,
                    letterSpacing = (-0.1f).sp
                )
            }
            Text(
                text = "$count transaction${if (count != 1) "s" else ""}",
                fontSize = 11.sp,
                color = SlateColor,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "₹$amount",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = color
            )
        }
    }
}

@Composable
private fun TransactionCard(transaction: Transaction) {
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
                        text = "TXN #${transaction.transactionId.take(8).uppercase()}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = NavyColor,
                        letterSpacing = (-0.2f).sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Booking: ${transaction.bookingId.take(8)}…",
                        fontSize = 11.sp,
                        color = SlateColor.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }
                TransactionStatusBadge(transaction.paymentStatus)
            }

            HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    DetailRow("Method", transaction.paymentMethod)
                    DetailRow("Date", transaction.transactionDate.toString().take(10))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Amount",
                        fontSize = 11.sp,
                        color = SlateColor,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(1.dp))
                    Text(
                        text = "₹${transaction.amount}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = if (transaction.paymentStatus == PaymentStatus.SUCCESS) SuccessColor else ErrorColor
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionStatusBadge(status: PaymentStatus) {
    val (text, bg, fg) = when (status) {
        PaymentStatus.SUCCESS -> Triple("Success", SuccessBgColor, SuccessColor)
        PaymentStatus.FAILED -> Triple("Failed", ErrorColor.copy(alpha = 0.08f), ErrorColor)
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
private fun DetailRow(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "$label:", fontSize = 12.sp, color = SlateColor, fontWeight = FontWeight.Medium)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NavyColor)
    }
}