package com.safarsakha.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import com.safarsakha.presentation.screens.admin.AdminLoginScreen
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.subclass

@Serializable
sealed interface AppNavKey : NavKey {

    @Serializable
    data object AdminLogin : AppNavKey

    companion object {
        fun register(builder: PolymorphicModuleBuilder<NavKey>) {
            builder.subclass(AdminLogin::class, AdminLogin.serializer())
        }
    }
}

@Composable
fun AppNavigation(backStack: NavBackStack<NavKey>) {

    NavDisplay(
        backStack = backStack,
        entryProvider = { key ->

            when (val route = key as? AppNavKey) {

                AppNavKey.AdminLogin -> {
                    NavEntry(route) {

                        var email by remember { mutableStateOf("") }
                        var password by remember { mutableStateOf("") }
                        var isLoading by remember { mutableStateOf(false) }
                        var errorMessage by remember { mutableStateOf<String?>(null) }

                        AdminLoginScreen(
                            email = email,
                            password = password,
                            isLoading = isLoading,
                            errorMessage = errorMessage,
                            onEmailChange = {
                                email = it
                            },
                            onPasswordChange = {
                                password = it
                            },
                            onLoginClick = {
                                if (email.isBlank() || password.isBlank()) {
                                    errorMessage = "Please enter email and password"
                                } else {
                                    errorMessage = null
                                    isLoading = true
                                }
                            }
                        )
                    }
                }

                null -> error("Unknown route")
            }
        }
    )
}