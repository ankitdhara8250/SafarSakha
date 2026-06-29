package com.safarsakha.presentation.screens.profile.myprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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

// ── Premium Design Tokens ────────────────────────────────────────────────────
private val NavyColor = Color(0xFF0F172A)
private val SkyColor = Color(0xFF0EA5E9)
private val SlateColor = Color(0xFF64748B)
private val BorderColor = Color(0xFFE2E8F0)
private val BgColor = Color(0xFFFFFFFF)
private val LightBgColor = Color(0xFFF8FAFC)
private val ErrorColor = Color(0xFFDC2626)

/**
 * My Profile screen.
 *
 * Logout flow overview:
 * 1. User taps the red "Logout" button → [showLogoutDialog] becomes true.
 * 2. Material 3 [LogoutConfirmationDialog] is shown.
 * 3. If the user confirms, [MyProfileEvent.Logout] is sent to the ViewModel.
 * 4. ViewModel calls [LogoutUserUseCase] which delegates to [AuthRepository.logout].
 * 5. On success the ViewModel emits [MyProfileUiState.LoggedOut].
 * 6. The [LaunchedEffect] below detects [MyProfileUiState.LoggedOut] and
 * calls [onLogout], which clears the back stack and navigates to the
 * login screen (wired in [AppNavigation]).
 *
 * @param onMenuClick  Opens the navigation drawer.
 * @param onLogout     Called after a successful logout; the caller must clear
 * the back stack and navigate to the login screen.
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

    // FIX: Memoized the factory reference inside remember {} so state initialization streams
    // remain completely stable across target KMP/Navigation targets architecture recompositions.
    val viewModel: MyProfileViewModel = viewModel(
        factory = remember(getUserProfileUseCase, logoutUserUseCase) {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
                    @Suppress("UNCHECKED_CAST")
                    return MyProfileViewModel(getUserProfileUseCase, logoutUserUseCase) as T
                }
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
                    Column {
                        Text(
                            text = "My Profile",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyColor,
                            letterSpacing = (-0.3f).sp
                        )
                        Text(
                            text = "Manage your personal credentials",
                            fontSize = 12.sp,
                            color = SlateColor
                        )
                    }
                },
                navigationIcon = { HamburgerMenuButton(onClick = onMenuClick) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgColor,
                    scrolledContainerColor = BgColor
                ),
                modifier = Modifier.border(
                    width = 1.dp,
                    color = BorderColor.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(0.dp)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBgColor)
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
                tint = ErrorColor
            )
        },
        title = {
            Text(
                text = "Logout",
                fontWeight = FontWeight.Bold,
                color = NavyColor
            )
        },
        text = {
            Text(
                text = "Are you sure you want to log out?",
                color = SlateColor
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ErrorColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Logout", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", color = NavyColor, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

// ── Secondary states UI ──────────────────────────────────────────────────────

@Composable
private fun ProfileLoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = SkyColor, strokeWidth = 3.dp)
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(50))
                    .background(ErrorColor.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Text("⚠️", fontSize = 24.sp)
            }
            Text(
                text = "Couldn't load your profile",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = NavyColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = message,
                fontSize = 13.sp,
                color = SlateColor,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyColor)
            ) {
                Text(text = "Retry", color = Color.White, fontWeight = FontWeight.SemiBold)
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
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProfileAvatar(name = user.name)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = user.name.ifBlank { "Your Profile" },
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = NavyColor,
            letterSpacing = (-0.3f).sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = NavyColor.copy(alpha = 0.04f),
                    spotColor = NavyColor.copy(alpha = 0.08f)
                )
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = NavyColor.copy(alpha = 0.02f),
                    spotColor = NavyColor.copy(alpha = 0.04f)
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BgColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = BorderColor.copy(alpha = 0.60f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Profile Information",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SkyColor,
                    letterSpacing = 0.2.sp,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )

                ProfileField(label = "Name", value = user.name)
                HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))

                ProfileField(label = "Email", value = user.email)
                HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))

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
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ErrorColor),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
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
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
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
            .size(80.dp)
            .shadow(4.dp, CircleShape)
            .background(color = NavyColor, shape = CircleShape)
            .border(2.dp, BgColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
    }
}

@Composable
private fun ProfileField(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = SlateColor,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value.ifBlank { "—" },
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = NavyColor
        )
    }
}