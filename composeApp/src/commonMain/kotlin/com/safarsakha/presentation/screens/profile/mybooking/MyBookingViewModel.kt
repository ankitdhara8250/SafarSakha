package com.safarsakha.presentation.screens.profile.mybooking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safarsakha.core.utils.Resource
import com.safarsakha.domain.usecase.booking.CancelBookingUseCase
import com.safarsakha.domain.usecase.booking.GetUserBookingsUseCase
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Backend ViewModel for "My Bookings". Handles:
 *  - Get User Bookings (live observe)
 *  - Cancel Booking
 */
class MyBookingViewModel(
    private val getUserBookingsUseCase: GetUserBookingsUseCase,
    private val cancelBookingUseCase: CancelBookingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyBookingUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeBookings()
    }

    fun handleEvent(event: MyBookingEvent) {
        when (event) {
            is MyBookingEvent.LoadBookings -> observeBookings()
            is MyBookingEvent.RequestCancelBooking -> _uiState.update {
                it.copy(bookingPendingCancellation = event.booking, cancelErrorMessage = null)
            }
            is MyBookingEvent.DismissCancelDialog -> _uiState.update {
                it.copy(bookingPendingCancellation = null, cancelErrorMessage = null)
            }
            is MyBookingEvent.ConfirmCancelBooking -> cancelBooking()
        }
    }

    private fun observeBookings() {
        val userId = Firebase.auth.currentUser?.uid
        if (userId == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Please log in to view your bookings.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            getUserBookingsUseCase.observe(userId).collect { result ->
                when (result) {
                    is Resource.Success -> _uiState.update {
                        it.copy(isLoading = false, bookings = result.data ?: emptyList(), errorMessage = null)
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message ?: "Failed to load bookings")
                    }
                    else -> {}
                }
            }
        }
    }

    private fun cancelBooking() {
        val booking = _uiState.value.bookingPendingCancellation ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isCancelling = true, cancelErrorMessage = null) }
            when (val result = cancelBookingUseCase(booking.bookingId)) {
                is Resource.Success -> _uiState.update {
                    it.copy(isCancelling = false, bookingPendingCancellation = null)
                    // Booking list updates automatically via the live observe() Flow above.
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isCancelling = false, cancelErrorMessage = result.message ?: "Failed to cancel booking")
                }
                else -> {}
            }
        }
    }
}