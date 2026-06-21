package com.safarsakha.presentation.screens.profile.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safarsakha.core.utils.Resource
import com.safarsakha.domain.model.Transaction
import com.safarsakha.domain.usecase.transaction.CreateTransactionUseCase
import com.safarsakha.domain.usecase.transaction.GetUserTransactionsUseCase
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Backend ViewModel for Transactions. Handles:
 *  - Get User Transactions (live observe)
 *  - Create Transaction (will be invoked by the future payment flow)
 */
class TransactionViewModel(
    private val getUserTransactionsUseCase: GetUserTransactionsUseCase,
    private val createTransactionUseCase: CreateTransactionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeTransactions()
    }

    fun handleEvent(event: TransactionEvent) {
        when (event) {
            is TransactionEvent.LoadTransactions -> observeTransactions()
            is TransactionEvent.CreateTransaction -> createTransaction(event.transaction)
        }
    }

    private fun observeTransactions() {
        val userId = Firebase.auth.currentUser?.uid
        if (userId == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Please log in to view your transactions.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            getUserTransactionsUseCase.observe(userId).collect { result ->
                when (result) {
                    is Resource.Success -> _uiState.update {
                        it.copy(isLoading = false, transactions = result.data ?: emptyList(), errorMessage = null)
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message ?: "Failed to load transactions")
                    }
                    else -> {}
                }
            }
        }
    }

    private fun createTransaction(transaction: Transaction) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submitErrorMessage = null) }
            when (val result = createTransactionUseCase(transaction)) {
                is Resource.Success -> _uiState.update {
                    it.copy(isSubmitting = false, submitErrorMessage = null)
                    // The live observe() Flow above will pick up the new transaction automatically.
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isSubmitting = false, submitErrorMessage = result.message ?: "Failed to record transaction")
                }
                else -> {}
            }
        }
    }
}