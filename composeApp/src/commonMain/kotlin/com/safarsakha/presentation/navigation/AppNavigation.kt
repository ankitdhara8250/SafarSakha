package com.safarsakha.presentation.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.safarsakha.domain.repository.AuthRepository
import com.safarsakha.domain.usecase.auth.LoginUserUseCase
import com.safarsakha.domain.usecase.auth.RegisterUserUseCase
import com.safarsakha.presentation.screens.admin.dashboard.AdminDashboardScreen
import com.safarsakha.presentation.screens.admin.login.AdminLoginScreen
import com.safarsakha.presentation.screens.admin.login.AdminLoginViewModel
import com.safarsakha.presentation.screens.admin.tourpackage.AdminTourPackageListScreen
import com.safarsakha.presentation.screens.admin.tourpackage.CreateTourPackageScreen
import com.safarsakha.presentation.screens.admin.tourpackage.EditTourPackageScreen
import com.safarsakha.presentation.screens.profile.feedback.FeedbackScreen
import com.safarsakha.presentation.screens.profile.mybooking.MyBookingScreen
import com.safarsakha.presentation.screens.profile.myprofile.UserProfileScreen as MyProfileScreen
import com.safarsakha.presentation.screens.profile.profiledashboard.ProfileDashboardScreen
import com.safarsakha.presentation.screens.profile.profiledashboard.ProfileDrawerItem
import com.safarsakha.presentation.screens.profile.registration.UserRegisterScreen
import com.safarsakha.presentation.screens.profile.registration.UserRegisterViewModel
import com.safarsakha.presentation.screens.profile.tours.TourDetailScreen
import com.safarsakha.presentation.screens.profile.tours.UserTourListScreen
import com.safarsakha.presentation.screens.profile.transaction.TransactionScreen
import com.safarsakha.presentation.screens.profile.userlogin.UserProfileViewModel
import com.safarsakha.presentation.screens.user.profile.UserProfileScreen
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.PolymorphicModuleBuilder

expect fun provideAuthRepository(): AuthRepository

@Serializable
sealed interface AppNavKey : NavKey {

    // Admin navigations
    @Serializable
    data object AdminLogin : AppNavKey

    @Serializable
    data object AdminDashboard : AppNavKey

    @Serializable
    data object AdminTourPackageList : AppNavKey

    @Serializable
    data object CreateTourPackage : AppNavKey

    @Serializable
    data class EditTourPackage(val packageId: String) : AppNavKey

    // User profile (login screen)
    @Serializable
    data object UserProfile : AppNavKey

    // User registration
    @Serializable
    data object UserRegister : AppNavKey

    // User tours
    @Serializable
    data object UserTourList : AppNavKey

    @Serializable
    data class UserTourDetail(val packageId: String) : AppNavKey

    // Profile drawer destinations
    @Serializable
    data object ProfileMyProfile : AppNavKey

    @Serializable
    data object ProfileMyBooking : AppNavKey

    @Serializable
    data object ProfileTransaction : AppNavKey

    @Serializable
    data object ProfileFeedback : AppNavKey

    companion object {
        fun register(builder: PolymorphicModuleBuilder<NavKey>) {
            builder.subclass(AdminLogin::class, AdminLogin.serializer())
            builder.subclass(AdminDashboard::class, AdminDashboard.serializer())
            builder.subclass(AdminTourPackageList::class, AdminTourPackageList.serializer())
            builder.subclass(CreateTourPackage::class, CreateTourPackage.serializer())
            builder.subclass(EditTourPackage::class, EditTourPackage.serializer())
            builder.subclass(UserProfile::class, UserProfile.serializer())
            builder.subclass(UserRegister::class, UserRegister.serializer())
            builder.subclass(UserTourList::class, UserTourList.serializer())
            builder.subclass(UserTourDetail::class, UserTourDetail.serializer())
            // Profile drawer destinations
            builder.subclass(ProfileMyProfile::class, ProfileMyProfile.serializer())
            builder.subclass(ProfileMyBooking::class, ProfileMyBooking.serializer())
            builder.subclass(ProfileTransaction::class, ProfileTransaction.serializer())
            builder.subclass(ProfileFeedback::class, ProfileFeedback.serializer())
        }
    }
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    backStack: NavBackStack<NavKey>
) {
    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator()
        ),
        entryProvider = { key ->
            when (val route = key as? AppNavKey) {

                AppNavKey.AdminLogin -> {
                    NavEntry(key = route) {
                        val viewModel = remember {
                            AdminLoginViewModel(authRepository = provideAuthRepository())
                        }
                        AdminLoginScreen(
                            viewModel = viewModel,
                            onLoginSuccess = { backStack.navigateToAdminDashboard() }
                        )
                    }
                }

                AppNavKey.AdminDashboard -> {
                    NavEntry(key = route) {
                        AdminDashboardScreen(
                            onTourPackageClick = { backStack.add(AppNavKey.AdminTourPackageList) },
                            onBookingClick = { },
                            onFeedbackEnquiryClick = { }
                        )
                    }
                }

                AppNavKey.AdminTourPackageList -> {
                    NavEntry(key = route) {
                        AdminTourPackageListScreen(
                            onNavigateToCreate = { backStack.add(AppNavKey.CreateTourPackage) },
                            onNavigateToEdit = { id -> backStack.add(AppNavKey.EditTourPackage(id)) }
                        )
                    }
                }

                AppNavKey.CreateTourPackage -> {
                    NavEntry(key = route) {
                        CreateTourPackageScreen(onNavigateBack = { backStack.removeLast() })
                    }
                }

                is AppNavKey.EditTourPackage -> {
                    NavEntry(key = route) {
                        EditTourPackageScreen(
                            packageId = route.packageId,
                            onNavigateBack = { backStack.removeLast() }
                        )
                    }
                }

                AppNavKey.UserProfile -> {
                    NavEntry(key = route) {
                        val authRepository = provideAuthRepository()
                        val loginUserUseCase = remember { LoginUserUseCase(authRepository) }
                        val viewModel = remember { UserProfileViewModel(loginUserUseCase) }
                        UserProfileScreen(
                            viewModel = viewModel,
                            onRegisterClick = { backStack.add(AppNavKey.UserRegister) },
                            onAdminLoginClick = { backStack.add(AppNavKey.AdminLogin) },
                            onLoginSuccess = { backStack.navigateToUserTourList() }
                        )
                    }
                }

                AppNavKey.UserRegister -> {
                    NavEntry(key = route) {
                        val authRepository = provideAuthRepository()
                        val registerUserUseCase = remember { RegisterUserUseCase(authRepository) }
                        val viewModel = remember { UserRegisterViewModel(registerUserUseCase) }
                        UserRegisterScreen(
                            viewModel = viewModel,
                            onBackToLogin = { backStack.removeLast() },
                            onRegistrationSuccess = { backStack.removeLast() }
                        )
                    }
                }

                AppNavKey.UserTourList -> {
                    NavEntry(key = route) {
                        ProfileDashboardScreen(
                            selectedItem = ProfileDrawerItem.Tours,
                            onItemSelected = { item -> backStack.navigateToProfileItem(item) }
                        ) { openDrawer ->
                            UserTourListScreen(
                                onTourClick = { packageId ->
                                    backStack.add(AppNavKey.UserTourDetail(packageId))
                                },
                                onMenuClick = openDrawer
                            )
                        }
                    }
                }

                is AppNavKey.UserTourDetail -> {
                    NavEntry(key = route) {
                        TourDetailScreen(
                            packageId = route.packageId,
                            onNavigateBack = { backStack.removeLast() }
                        )
                    }
                }

                AppNavKey.ProfileMyProfile -> {
                    NavEntry(key = route) {
                        ProfileDashboardScreen(
                            selectedItem = ProfileDrawerItem.MyProfile,
                            onItemSelected = { item -> backStack.navigateToProfileItem(item) }
                        ) { openDrawer ->
                            MyProfileScreen(onMenuClick = openDrawer)
                        }
                    }
                }

                AppNavKey.ProfileMyBooking -> {
                    NavEntry(key = route) {
                        ProfileDashboardScreen(
                            selectedItem = ProfileDrawerItem.MyBooking,
                            onItemSelected = { item -> backStack.navigateToProfileItem(item) }
                        ) { openDrawer ->
                            MyBookingScreen(onMenuClick = openDrawer)
                        }
                    }
                }

                AppNavKey.ProfileTransaction -> {
                    NavEntry(key = route) {
                        ProfileDashboardScreen(
                            selectedItem = ProfileDrawerItem.Transaction,
                            onItemSelected = { item -> backStack.navigateToProfileItem(item) }
                        ) { openDrawer ->
                            TransactionScreen(onMenuClick = openDrawer)
                        }
                    }
                }

                AppNavKey.ProfileFeedback -> {
                    NavEntry(key = route) {
                        ProfileDashboardScreen(
                            selectedItem = ProfileDrawerItem.Feedback,
                            onItemSelected = { item -> backStack.navigateToProfileItem(item) }
                        ) { openDrawer ->
                            FeedbackScreen(onMenuClick = openDrawer)
                        }
                    }
                }

                null -> error("Unknown route")
            }
        },
        transitionSpec = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = smoothSpec
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = smoothSpec
            )
        },
        popTransitionSpec = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = smoothSpec
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = smoothSpec
            )
        },
        predictivePopTransitionSpec = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = smoothSpec
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = smoothSpec
            )
        }
    )
}

fun NavBackStack<NavKey>.navigateToAdminDashboard() {
    clear()
    add(AppNavKey.AdminDashboard)
}

fun NavBackStack<NavKey>.navigateToUserTourList() {
    clear()
    add(AppNavKey.UserTourList)
}

fun NavBackStack<NavKey>.navigateToProfileItem(item: ProfileDrawerItem) {
    clear()
    add(
        when (item) {
            ProfileDrawerItem.MyProfile   -> AppNavKey.ProfileMyProfile
            ProfileDrawerItem.Tours       -> AppNavKey.UserTourList
            ProfileDrawerItem.MyBooking   -> AppNavKey.ProfileMyBooking
            ProfileDrawerItem.Transaction -> AppNavKey.ProfileTransaction
            ProfileDrawerItem.Feedback    -> AppNavKey.ProfileFeedback
        }
    )
}

val smoothSpec = tween<IntOffset>(
    durationMillis = 800,
    easing = FastOutSlowInEasing
)