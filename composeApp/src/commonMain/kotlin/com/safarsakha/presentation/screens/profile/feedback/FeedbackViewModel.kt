package com.safarsakha.presentation.screens.profile.feedback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safarsakha.core.utils.Resource
import com.safarsakha.domain.repository.EnquiryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FeedbackViewModel(
    private val enquiryRepository: EnquiryRepository,
    private val userId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<FeedbackUiState>(FeedbackUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        observeUserEnquiries()
    }

    private fun observeUserEnquiries() {
        viewModelScope.launch {
            if (userId.isBlank()) {
                _uiState.value = FeedbackUiState.Error("Not logged in")
                return@launch
            }
            enquiryRepository.observeEnquiriesByUserId(userId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val list = result.data ?: emptyList()
                        _uiState.value = if (list.isEmpty()) FeedbackUiState.Empty
                        else FeedbackUiState.Success(list.sortedByDescending { it.createdAt })
                    }
                    is Resource.Error -> {
                        _uiState.value = FeedbackUiState.Error(result.message ?: "Unknown error")
                    }
                    else -> {}
                }
            }
        }
    }
}