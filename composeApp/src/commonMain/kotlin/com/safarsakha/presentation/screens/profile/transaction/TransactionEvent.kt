package com.safarsakha.presentation.screens.profile.transaction

import com.safarsakha.domain.model.Transaction

sealed class TransactionEvent {
    object LoadTransactions : TransactionEvent()
    data class CreateTransaction(val transaction: Transaction) : TransactionEvent()
}