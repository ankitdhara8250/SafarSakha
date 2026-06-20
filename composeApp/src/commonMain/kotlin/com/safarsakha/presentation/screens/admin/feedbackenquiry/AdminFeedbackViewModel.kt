package com.safarsakha.presentation.screens.admin.feedbackenquiry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safarsakha.core.utils.Resource
import com.safarsakha.domain.model.Enquiry
import com.safarsakha.domain.repository.EnquiryRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AdminFeedbackViewModel(
    private val enquiryRepository: EnquiryRepository
) : ViewModel() {

    // List state
    private val _listState = MutableStateFlow<AdminEnquiryListUiState>(AdminEnquiryListUiState.Loading)
    val listState = _listState.asStateFlow()

    // Detail state
    private val _detailState = MutableStateFlow(AdminEnquiryDetailUiState())
    val detailState = _detailState.asStateFlow()

    private val _showSnackbar = MutableSharedFlow<String>()
    val showSnackbar = _showSnackbar.asSharedFlow()

    init {
        observeAllEnquiries()
    }

    fun observeAllEnquiries() {
        viewModelScope.launch {
            enquiryRepository.observeAllEnquiries().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val list = result.data ?: emptyList()
                        _listState.value = if (list.isEmpty()) AdminEnquiryListUiState.Empty
                        else AdminEnquiryListUiState.Success(list.sortedByDescending { it.createdAt })
                    }
                    is Resource.Error -> {
                        _listState.value = AdminEnquiryListUiState.Error(result.message ?: "Unknown error")
                    }
                    else -> {}
                }
            }
        }
    }

    fun loadEnquiryDetail(enquiry: Enquiry) {
        _detailState.update { it.copy(enquiry = enquiry, replyText = enquiry.adminReply ?: "", errorMessage = null) }
    }

    fun onReplyTextChange(text: String) {
        _detailState.update { it.copy(replyText = text) }
    }

    fun sendReply() {
        val enquiry = _detailState.value.enquiry ?: return
        val reply = _detailState.value.replyText.trim()
        if (reply.isBlank()) {
            viewModelScope.launch { _showSnackbar.emit("Reply cannot be empty") }
            return
        }
        viewModelScope.launch {
            _detailState.update { it.copy(isSending = true) }
            when (val result = enquiryRepository.replyToEnquiry(enquiry.enquiryId, reply)) {
                is Resource.Success -> {
                    _detailState.update { it.copy(isSending = false) }
                    _showSnackbar.emit("Reply sent successfully!")
                }
                is Resource.Error -> {
                    _detailState.update { it.copy(isSending = false) }
                    _showSnackbar.emit(result.message ?: "Failed to send reply")
                }
                else -> {}
            }
        }
    }
}