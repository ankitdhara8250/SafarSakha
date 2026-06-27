package com.safarsakha.presentation.screens.profile.myprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safarsakha.core.utils.Resource
import com.safarsakha.domain.usecase.auth.GetUserProfileUseCase
import com.safarsakha.domain.usecase.auth.LogoutUserUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyProfileViewModel(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val logoutUserUseCase: LogoutUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<MyProfileUiState>(MyProfileUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadProfile()
    }

    fun onEvent(event: MyProfileEvent) {
        when (event) {
            is MyProfileEvent.Retry -> loadProfile()
            is MyProfileEvent.Logout -> performLogout()
            // Called by the screen immediately after it has acted on LoggedOut
            // (i.e. after it has called onLogout() and navigation is in flight).
            // Resets the ViewModel back to Loading so that if this same ViewModel
            // instance is reused for a future ProfileMyProfile entry (because
            // Navigation3's SaveableStateHolder caches ViewModelStores by key),
            // it does NOT re-emit LoggedOut on composition and bounce the user
            // back to the login screen.
            is MyProfileEvent.ResetAfterLogout -> {
                _uiState.value = MyProfileUiState.Loading
            }
        }
    }

    private fun loadProfile() {
        loadJob?.cancel()

        loadJob = viewModelScope.launch {
            _uiState.value = MyProfileUiState.Loading

            when (val result = getUserProfileUseCase()) {
                is Resource.Success -> {
                    val user = result.data
                    _uiState.value = if (user != null) {
                        MyProfileUiState.Success(user)
                    } else {
                        MyProfileUiState.Error("Profile data not found")
                    }
                }
                is Resource.Error -> {
                    _uiState.value = MyProfileUiState.Error(
                        result.message ?: "Unable to load profile"
                    )
                }
                is Resource.Loading -> {
                    _uiState.value = MyProfileUiState.Loading
                }
            }
        }
    }

    private fun performLogout() {
        viewModelScope.launch {
            _uiState.value = MyProfileUiState.Loading

            when (logoutUserUseCase()) {
                is Resource.Success -> {
                    _uiState.value = MyProfileUiState.LoggedOut
                }
                is Resource.Error -> {
                    // Logout failed — reload the profile so the screen is usable
                    loadProfile()
                }
                is Resource.Loading -> { /* covered by the Loading state above */ }
            }
        }
    }
}