package com.safarsakha.presentation.screens.profile.feedback

import com.safarsakha.domain.model.Enquiry

sealed class FeedbackUiState {
    object Loading : FeedbackUiState()
    object Empty : FeedbackUiState()
    data class Success(val enquiries: List<Enquiry>) : FeedbackUiState()
    data class Error(val message: String) : FeedbackUiState()
}