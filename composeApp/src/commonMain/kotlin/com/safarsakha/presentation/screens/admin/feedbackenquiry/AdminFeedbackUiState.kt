package com.safarsakha.presentation.screens.admin.feedbackenquiry

import com.safarsakha.domain.model.Enquiry

sealed class AdminEnquiryListUiState {
    object Loading : AdminEnquiryListUiState()
    object Empty : AdminEnquiryListUiState()
    data class Success(val enquiries: List<Enquiry>) : AdminEnquiryListUiState()
    data class Error(val message: String) : AdminEnquiryListUiState()
}

data class AdminEnquiryDetailUiState(
    val enquiry: Enquiry? = null,
    val replyText: String = "",
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)