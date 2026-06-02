package com.safarsakha

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.savedstate.serialization.SavedStateConfiguration
import com.safarsakha.presentation.navigation.AppNavKey
import com.safarsakha.presentation.navigation.AppNavigation
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Composable
fun SafarSakhaApp() {

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) { innerPadding ->

        val backStack: NavBackStack<NavKey> =
            rememberNavBackStack(
                savedStateConfig,
                AppNavKey.AdminDashboard
            )

        AppNavigation(backStack = backStack)
    }
}

private val savedStateConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            AppNavKey.register(this)

            val appSerializer = AppNavKey.serializer()

            subclass(AppNavKey::class, appSerializer)
            defaultDeserializer { AppNavKey.serializer() }
        }
    }
}