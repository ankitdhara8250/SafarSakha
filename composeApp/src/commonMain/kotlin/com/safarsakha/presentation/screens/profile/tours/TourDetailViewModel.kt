package com.safarsakha.presentation.screens.profile.tours

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safarsakha.core.utils.Resource
import com.safarsakha.domain.model.Enquiry
import com.safarsakha.domain.repository.EnquiryRepository
import com.safarsakha.domain.usecase.tourpackage.GetTourPackageByIdUseCase
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TourDetailViewModel(
    private val getTourPackageByIdUseCase: GetTourPackageByIdUseCase,
    private val enquiryRepository: EnquiryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TourDetailUiState())
    val uiState = _uiState.asStateFlow()

    private val _showSnackbar = MutableSharedFlow<String>()
    val showSnackbar = _showSnackbar.asSharedFlow()

    private var lastLoadedId: String? = null

    fun handleEvent(event: TourDetailEvent) {
        when (event) {
            is TourDetailEvent.LoadPackage -> loadPackage(event.id)
            is TourDetailEvent.Retry -> lastLoadedId?.let { loadPackage(it) }
            is TourDetailEvent.OpenEnquiryDialog -> _uiState.update { it.copy(showEnquiryDialog = true) }
            is TourDetailEvent.DismissEnquiryDialog -> _uiState.update { it.copy(showEnquiryDialog = false) }
            is TourDetailEvent.SubmitEnquiry -> submitEnquiry(event.message)
        }
    }

    private fun loadPackage(id: String) {
        lastLoadedId = id
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = getTourPackageByIdUseCase(id)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            tourPackage = result.data,
                            errorMessage = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message ?: "Failed to load tour package"
                        )
                    }
                }
                else -> {}
            }
        }
    }

    private fun submitEnquiry(message: String) {
        val tourPackage = _uiState.value.tourPackage ?: return
        val firebaseUser = Firebase.auth.currentUser

        if (firebaseUser == null) {
            viewModelScope.launch { _showSnackbar.emit("Please log in to send an enquiry.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingEnquiry = true) }

            val enquiry = Enquiry(
                tourPackageId = tourPackage.id,
                tourPackageName = tourPackage.title,
                userId = firebaseUser.uid,
                userName = firebaseUser.displayName ?: "Anonymous",
                enquiryMessage = message
            )

            when (val result = enquiryRepository.submitEnquiry(enquiry)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isSubmittingEnquiry = false, showEnquiryDialog = false) }
                    _showSnackbar.emit("Your enquiry has been sent!")
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isSubmittingEnquiry = false) }
                    _showSnackbar.emit(result.message ?: "Failed to send enquiry")
                }
                else -> {
                    _uiState.update { it.copy(isSubmittingEnquiry = false) }
                }
            }
        }
    }
}