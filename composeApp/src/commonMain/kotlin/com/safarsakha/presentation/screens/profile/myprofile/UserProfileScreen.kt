package com.safarsakha.presentation.screens.profile.myprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.safarsakha.domain.model.User
import com.safarsakha.domain.usecase.auth.GetUserProfileUseCase
import com.safarsakha.presentation.navigation.provideAuthRepository
import com.safarsakha.presentation.screens.profile.profiledashboard.components.HamburgerMenuButton
import kotlin.reflect.KClass

private val PrimaryColor = Color(0xFF1E3A8A)
private val BackgroundColor = Color(0xFFF5F7FB)
private val TitleColor = Color(0xFF0F172A)
private val SubtitleColor = Color(0xFF64748B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(onMenuClick: () -> Unit) {
    val authRepository = remember { provideAuthRepository() }
    val getUserProfileUseCase = remember { GetUserProfileUseCase(authRepository) }

    val viewModel: MyProfileViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
                @Suppress("UNCHECKED_CAST")
                return MyProfileViewModel(getUserProfileUseCase) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Profile",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColor
                    )
                },
                navigationIcon = { HamburgerMenuButton(onClick = onMenuClick) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is MyProfileUiState.Loading -> ProfileLoadingState()
                is MyProfileUiState.Success -> ProfileContent(user = state.user)
                is MyProfileUiState.Error -> ProfileErrorState(
                    message = state.message,
                    onRetry = { viewModel.onEvent(MyProfileEvent.Retry) }
                )
            }
        }
    }
}

@Composable
private fun ProfileLoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = PrimaryColor)
    }
}

@Composable
private fun ProfileErrorState(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Couldn't load your profile",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TitleColor,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = SubtitleColor,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Text(text = "Retry", color = Color.White)
            }
        }
    }
}

@Composable
private fun ProfileContent(user: User) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProfileAvatar(name = user.name)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = user.name.ifBlank { "Your Profile" },
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TitleColor
        )

        Spacer(modifier = Modifier.height(28.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(
                    text = "Profile Information",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SubtitleColor,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )

                ProfileField(label = "Name", value = user.name)
                HorizontalDivider(color = Color(0xFFEEF1F6))

                ProfileField(label = "Email", value = user.email)
                HorizontalDivider(color = Color(0xFFEEF1F6))

                ProfileField(label = "Phone Number", value = user.phoneNumber)

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ProfileAvatar(name: String) {
    val initial = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    Box(
        modifier = Modifier
            .size(72.dp)
            .background(color = PrimaryColor, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun ProfileField(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 14.dp)) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = SubtitleColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value.ifBlank { "—" },
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = TitleColor
        )
    }
}