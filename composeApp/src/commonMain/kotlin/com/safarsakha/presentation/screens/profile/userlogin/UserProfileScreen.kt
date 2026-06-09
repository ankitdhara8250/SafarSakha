package com.safarsakha.presentation.screens.user.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safarsakha.presentation.screens.profile.userlogin.UserProfileEvent
import com.safarsakha.presentation.screens.profile.userlogin.UserProfileViewModel

@Composable
fun UserProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: UserProfileViewModel,
    onRegisterClick: () -> Unit,
    onAdminLoginClick: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle successful login
    LaunchedEffect(uiState.isLoginSuccess) {
        if (uiState.isLoginSuccess) {
            onLoginSuccess()
            viewModel.onEvent(UserProfileEvent.OnResetSuccess)
        }
    }

    // Handle error display
    if (uiState.error != null) {
        LaunchedEffect(uiState.error) {
            // Error is displayed in the UI, can add snackbar logic here
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFFF5F7FB)
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7FB))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center
            ) {

// ---------------------------------------------------------
// TOP SECTION
// ---------------------------------------------------------

                Text(
                    text = "Welcome to SafarSakha",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Explore journeys with ease",
                    fontSize = 15.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(30.dp))

                // ---------------------------------------------------------
                // LOGIN CARD
                // ---------------------------------------------------------

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        // ---------------------------------------------------------
                        // LOGIN TITLE
                        // ---------------------------------------------------------

                        Text(
                            text = "User Login",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // ---------------------------------------------------------
                        // EMAIL FIELD
                        // ---------------------------------------------------------

                        OutlinedTextField(
                            value = uiState.email,
                            onValueChange = {
                                viewModel.onEvent(UserProfileEvent.OnEmailChanged(it))
                            },
                            label = {
                                Text("Email")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !uiState.isLoading,
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // ---------------------------------------------------------
                        // PASSWORD FIELD
                        // ---------------------------------------------------------

                        OutlinedTextField(
                            value = uiState.password,
                            onValueChange = {
                                viewModel.onEvent(UserProfileEvent.OnPasswordChanged(it))
                            },
                            label = {
                                Text("Password")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !uiState.isLoading,
                            shape = RoundedCornerShape(14.dp),
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // ---------------------------------------------------------
                        // LOGIN BUTTON
                        // ---------------------------------------------------------

                        Button(
                            onClick = {
                                viewModel.onEvent(UserProfileEvent.OnLoginClick)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = !uiState.isLoading,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E3A8A)
                            )
                        ) {

                            if (uiState.isLoading) {

                                CircularProgressIndicator(
                                    modifier = Modifier.height(22.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )

                            } else {

                                Text(
                                    text = "Login",
                                    color = Color.White
                                )
                            }
                        }

                        // Show error message if present
                        if (uiState.error != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = uiState.error!!,
                                color = Color.Red,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(26.dp))

                        // ---------------------------------------------------------
                        // DIVIDER
                        // ---------------------------------------------------------

                        HorizontalDivider()

                        Spacer(modifier = Modifier.height(20.dp))

                        // ---------------------------------------------------------
                        // REGISTER SECTION
                        // ---------------------------------------------------------

                        Text(
                            text = "New User?",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        TextButton(
                            onClick = onRegisterClick
                        ) {

                            Text(
                                text = "Register Here",
                                color = Color(0xFF1E3A8A),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // ---------------------------------------------------------
                        // ADMIN LOGIN BUTTON
                        // ---------------------------------------------------------

                        OutlinedButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            onClick = onAdminLoginClick
                        ) {

                            Text(
                                text = "Admin Login",
                                color = Color(0xFF1E3A8A)
                            )
                        }
                    }
                }
            }
        }
    }
}

