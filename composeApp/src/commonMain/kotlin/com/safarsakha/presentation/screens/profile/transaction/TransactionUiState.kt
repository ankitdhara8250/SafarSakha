package com.safarsakha.presentation.screens.profile.transaction

import com.safarsakha.domain.model.PaymentStatus
import com.safarsakha.domain.model.Transaction

data class TransactionUiState(
    val isLoading: Boolean = true,
    val transactions: List<Transaction> = emptyList(),
    val errorMessage: String? = null,

    // Create-transaction state (used by the future Booking/Payment flow)
    val isSubmitting: Boolean = false,
    val submitErrorMessage: String? = null
) {
    val successfulTransactions: List<Transaction>
        get() = transactions.filter { it.paymentStatus == PaymentStatus.SUCCESS }
            .sortedByDescending { it.transactionDate }

    val failedTransactions: List<Transaction>
        get() = transactions.filter { it.paymentStatus == PaymentStatus.FAILED }
            .sortedByDescending { it.transactionDate }

    val allTransactionsSorted: List<Transaction>
        get() = transactions.sortedByDescending { it.transactionDate }
}