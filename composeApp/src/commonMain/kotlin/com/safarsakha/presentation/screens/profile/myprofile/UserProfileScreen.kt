package com.safarsakha.presentation.screens.profile.myprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.safarsakha.domain.usecase.auth.LogoutUserUseCase
import com.safarsakha.presentation.navigation.provideAuthRepository
import com.safarsakha.presentation.screens.profile.profiledashboard.components.HamburgerMenuButton
import kotlin.reflect.KClass

private val PrimaryColor = Color(0xFF1E3A8A)
private val BackgroundColor = Color(0xFFF5F7FB)
private val TitleColor = Color(0xFF0F172A)
private val SubtitleColor = Color(0xFF64748B)

/**
 * My Profile screen.
 *
 * Logout flow overview:
 *  1. User taps the red "Logout" button → [showLogoutDialog] becomes true.
 *  2. Material 3 [LogoutConfirmationDialog] is shown.
 *  3. If the user confirms, [MyProfileEvent.Logout] is sent to the ViewModel.
 *  4. ViewModel calls [LogoutUserUseCase] which delegates to [AuthRepository.logout].
 *  5. On success the ViewModel emits [MyProfileUiState.LoggedOut].
 *  6. The [LaunchedEffect] below detects [MyProfileUiState.LoggedOut] and
 *     calls [onLogout], which clears the back stack and navigates to the
 *     login screen (wired in [AppNavigation]).
 *
 * @param onMenuClick  Opens the navigation drawer.
 * @param onLogout     Called after a successful logout; the caller must clear
 *                     the back stack and navigate to the login screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onMenuClick: () -> Unit,
    onLogout: () -> Unit = {}
) {
    val authRepository = remember { provideAuthRepository() }
    val getUserProfileUseCase = remember { GetUserProfileUseCase(authRepository) }
    val logoutUserUseCase = remember { LogoutUserUseCase(authRepository) }

    val viewModel: MyProfileViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
                @Suppress("UNCHECKED_CAST")
                return MyProfileViewModel(getUserProfileUseCase, logoutUserUseCase) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()

    // Controls whether the logout confirmation dialog is visible.
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Re-load the profile every time this composable enters the composition.
    // This is necessary because Navigation3 may reuse the same MyProfileViewModel
    // instance (via the SaveableStateHolder's ViewModelStore cache) when the user
    // navigates back to ProfileMyProfile after a logout+re-login cycle.
    // In that case init{} does not run again, so we need to explicitly trigger
    // a fresh load here. If the ViewModel is brand-new (first visit), calling
    // Retry is harmless — it cancels the already-running init job and starts
    // an identical one.
    LaunchedEffect(Unit) {
        viewModel.onEvent(MyProfileEvent.Retry)
    }


    // WHY WE RESET FIRST:
    // Navigation3's SaveableStateHolder keeps this ViewModel instance alive
    // keyed to AppNavKey.ProfileMyProfile even after it leaves the back stack.
    // If the user re-logs in and opens My Profile again, NavDisplay reuses
    // this same ViewModel — still holding LoggedOut — so this LaunchedEffect
    // would fire immediately on recomposition and call onLogout() again,
    // bouncing the user straight back to the login screen.
    //
    // Sending ResetAfterLogout transitions the ViewModel to Loading *before*
    // navigation runs. The next time this screen is composed (after re-login),
    // uiState is Loading — not LoggedOut — so the LaunchedEffect is a no-op
    // and loadProfile() is retriggered correctly via the screen's when() branch.
    LaunchedEffect(uiState) {
        if (uiState is MyProfileUiState.LoggedOut) {
            viewModel.onEvent(MyProfileEvent.ResetAfterLogout) // reset before nav
            onLogout()
        }
    }


    // ── Logout confirmation dialog ───────────────────────────────────────────
    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                // ── Step 3: Dispatch the Logout event to the ViewModel ───────
                viewModel.onEvent(MyProfileEvent.Logout)
            }
        )
    }

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
                is MyProfileUiState.Loading  -> ProfileLoadingState()
                is MyProfileUiState.Success  -> ProfileContent(
                    user = state.user,
                    // ── Step 1: Show dialog when button is tapped ────────────
                    onLogoutClick = { showLogoutDialog = true }
                )
                is MyProfileUiState.Error    -> ProfileErrorState(
                    message = state.message,
                    onRetry = { viewModel.onEvent(MyProfileEvent.Retry) }
                )
                // LoggedOut is handled by the LaunchedEffect above; show a
                // loading indicator while navigation is in flight.
                is MyProfileUiState.LoggedOut -> ProfileLoadingState()
            }
        }
    }
}

// ── Logout confirmation dialog ───────────────────────────────────────────────

/**
 * Material 3 dialog that asks the user to confirm they want to log out.
 *
 * @param onDismiss Called when the user taps "Cancel" or taps outside the dialog.
 * @param onConfirm Called when the user taps "Logout".
 */
@Composable
private fun LogoutConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = "Logout",
                fontWeight = FontWeight.Bold,
                color = TitleColor
            )
        },
        text = {
            Text(
                text = "Are you sure you want to log out?",
                color = SubtitleColor
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(text = "Logout", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", color = PrimaryColor)
            }
        }
    )
}

// ── Existing composables (unchanged) ────────────────────────────────────────

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

/**
 * The main profile content, now also receives [onLogoutClick] so it can
 * render the Logout button below the info card.
 */
@Composable
private fun ProfileContent(
    user: User,
    onLogoutClick: () -> Unit
) {
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

        // ── Logout button ────────────────────────────────────────────────────
        // Placed 24 dp below the profile card, full-width with rounded corners
        // and Material error colour (red). Tapping opens the confirmation dialog.
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Logout",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Logout",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
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