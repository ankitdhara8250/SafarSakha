package com.safarsakha.presentation.screens.profile.myprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safarsakha.core.utils.Resource
import com.safarsakha.domain.usecase.auth.GetUserProfileUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyProfileViewModel(
    private val getUserProfileUseCase: GetUserProfileUseCase
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
                    _uiState.value = MyProfileUiState.Error(result.message ?: "Unable to load profile")
                }
                is Resource.Loading -> {
                    _uiState.value = MyProfileUiState.Loading
                }
            }
        }
    }
}