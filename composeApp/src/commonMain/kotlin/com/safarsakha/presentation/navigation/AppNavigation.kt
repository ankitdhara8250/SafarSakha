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
import com.safarsakha.data.remote.firebase.FirebaseEnquiryDataSource
import com.safarsakha.data.repository.impl.EnquiryRepositoryImpl
import com.safarsakha.domain.model.TourPackage
import com.safarsakha.presentation.screens.profile.booking.BookingScreen
import com.safarsakha.domain.repository.AuthRepository
import com.safarsakha.domain.usecase.auth.LoginUserUseCase
import com.safarsakha.domain.usecase.auth.RegisterUserUseCase
import com.safarsakha.presentation.screens.admin.booking.AdminBookingDetailScreen
import com.safarsakha.presentation.screens.admin.booking.AdminBookingListScreen
import com.safarsakha.presentation.screens.admin.dashboard.AdminDashboardScreen
import com.safarsakha.presentation.screens.admin.feedbackenquiry.AdminEnquiryDetailScreen
import com.safarsakha.presentation.screens.admin.feedbackenquiry.AdminEnquiryListScreen
import com.safarsakha.presentation.screens.admin.feedbackenquiry.AdminFeedbackViewModel
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.PolymorphicModuleBuilder

expect fun provideAuthRepository(): AuthRepository

@Serializable
sealed interface AppNavKey : NavKey {

    // Admin
    @Serializable data object AdminLogin : AppNavKey
    @Serializable data object AdminDashboard : AppNavKey
    @Serializable data object AdminTourPackageList : AppNavKey
    @Serializable data object CreateTourPackage : AppNavKey
    @Serializable data class EditTourPackage(val packageId: String) : AppNavKey
    @Serializable data object AdminEnquiryList : AppNavKey
    @Serializable data object AdminEnquiryDetail : AppNavKey
    @Serializable data object AdminBookingList : AppNavKey
    @Serializable data class AdminBookingDetail(
        val bookingId: String,
        val userId: String,
        val userName: String,
        val packageId: String,
        val packageName: String,
        val packagePrice: Double,
        val startDate: String,
        val endDate: String,
        val bookingDate: String,
        val bookingStatus: String,
        val paymentStatus: String,
        val totalAmount: Double,
        val cancellationDate: String?
    ) : AppNavKey

    // User
    @Serializable data object UserProfile : AppNavKey
    @Serializable data object UserRegister : AppNavKey
    @Serializable data object UserTourList : AppNavKey
    @Serializable data class UserTourDetail(val packageId: String) : AppNavKey
    @Serializable data class Booking(
        val packageId: String,
        val packageTitle: String,
        val packagePrice: Double,
        val packageLocation: String,
        val packageDuration: String,
        val packageDescription: String,
        val packageImageUrl: String,
        val packageIsActive: Boolean
    ) : AppNavKey

    // Profile drawer
    @Serializable data object ProfileMyProfile : AppNavKey
    @Serializable data object ProfileMyBooking : AppNavKey
    @Serializable data object ProfileTransaction : AppNavKey
    @Serializable data object ProfileFeedback : AppNavKey

    companion object {
        fun register(builder: PolymorphicModuleBuilder<NavKey>) {
            builder.subclass(AdminLogin::class, AdminLogin.serializer())
            builder.subclass(AdminDashboard::class, AdminDashboard.serializer())
            builder.subclass(AdminTourPackageList::class, AdminTourPackageList.serializer())
            builder.subclass(CreateTourPackage::class, CreateTourPackage.serializer())
            builder.subclass(EditTourPackage::class, EditTourPackage.serializer())
            builder.subclass(AdminEnquiryList::class, AdminEnquiryList.serializer())
            builder.subclass(AdminEnquiryDetail::class, AdminEnquiryDetail.serializer())
            builder.subclass(AdminBookingList::class, AdminBookingList.serializer())
            builder.subclass(AdminBookingDetail::class, AdminBookingDetail.serializer())
            builder.subclass(UserProfile::class, UserProfile.serializer())
            builder.subclass(UserRegister::class, UserRegister.serializer())
            builder.subclass(UserTourList::class, UserTourList.serializer())
            builder.subclass(UserTourDetail::class, UserTourDetail.serializer())
            builder.subclass(Booking::class, Booking.serializer())
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
    val enquiryRepository = remember { EnquiryRepositoryImpl(FirebaseEnquiryDataSource()) }
    val adminFeedbackViewModel = remember { AdminFeedbackViewModel(enquiryRepository) }

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
        entryProvider = { key ->
            when (val route = key as? AppNavKey) {

                AppNavKey.AdminLogin -> NavEntry(key = route) {
                    val viewModel = remember { AdminLoginViewModel(authRepository = provideAuthRepository()) }
                    AdminLoginScreen(viewModel = viewModel, onLoginSuccess = { backStack.navigateToAdminDashboard() })
                }

                AppNavKey.AdminDashboard -> NavEntry(key = route) {
                    // Provide AuthRepository so we can call logout() here,
                    // keeping the Dashboard screen free of business logic.
                    val authRepository = remember { provideAuthRepository() }
                    // Coroutine scope for the logout suspend call.
                    val logoutScope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

                    AdminDashboardScreen(
                        onTourPackageClick = { backStack.add(AppNavKey.AdminTourPackageList) },
                        onBookingClick = { backStack.add(AppNavKey.AdminBookingList) },
                        onFeedbackEnquiryClick = { backStack.add(AppNavKey.AdminEnquiryList) },
                        onLogout = {
                            // 1. Call AuthRepository.logout() to sign out of Firebase
                            //    and clear any cached admin session/token.
                            // 2. Navigate to AdminLogin and clear the back stack so
                            //    pressing Back cannot return to the Dashboard.
                            logoutScope.launch {
                                authRepository.logout()
                                backStack.navigateToAdminLogin()
                            }
                        }
                    )
                }

                AppNavKey.AdminBookingList -> NavEntry(key = route) {
                    AdminBookingListScreen(
                        onNavigateBack = { backStack.removeLast() },
                        onBookingClick = { booking ->
                            backStack.add(AppNavKey.AdminBookingDetail(
                                bookingId = booking.bookingId,
                                userId = booking.userId,
                                userName = booking.userName,
                                packageId = booking.packageId,
                                packageName = booking.packageName,
                                packagePrice = booking.packagePrice,
                                startDate = booking.startDate.toString(),
                                endDate = booking.endDate.toString(),
                                bookingDate = booking.bookingDate.toString(),
                                bookingStatus = booking.bookingStatus.name,
                                paymentStatus = booking.paymentStatus.name,
                                totalAmount = booking.totalAmount,
                                cancellationDate = booking.cancellationDate?.toString()
                            ))
                        }
                    )
                }

                is AppNavKey.AdminBookingDetail -> NavEntry(key = route) {
                    val booking = remember(route) {
                        com.safarsakha.domain.model.Booking(
                            bookingId = route.bookingId,
                            userId = route.userId,
                            userName = route.userName,
                            packageId = route.packageId,
                            packageName = route.packageName,
                            packagePrice = route.packagePrice,
                            startDate = kotlinx.datetime.LocalDate.parse(route.startDate),
                            endDate = kotlinx.datetime.LocalDate.parse(route.endDate),
                            bookingDate = kotlinx.datetime.Instant.parse(route.bookingDate),
                            bookingStatus = com.safarsakha.domain.model.BookingStatus.valueOf(route.bookingStatus),
                            paymentStatus = com.safarsakha.domain.model.PaymentStatus.valueOf(route.paymentStatus),
                            totalAmount = route.totalAmount,
                            cancellationDate = route.cancellationDate?.let { kotlinx.datetime.Instant.parse(it) }
                        )
                    }
                    AdminBookingDetailScreen(booking = booking, onNavigateBack = { backStack.removeLast() })
                }

                AppNavKey.AdminTourPackageList -> NavEntry(key = route) {
                    AdminTourPackageListScreen(
                        onNavigateToCreate = { backStack.add(AppNavKey.CreateTourPackage) },
                        onNavigateToEdit = { id -> backStack.add(AppNavKey.EditTourPackage(id)) }
                    )
                }

                AppNavKey.CreateTourPackage -> NavEntry(key = route) {
                    CreateTourPackageScreen(onNavigateBack = { backStack.removeLast() })
                }

                is AppNavKey.EditTourPackage -> NavEntry(key = route) {
                    EditTourPackageScreen(packageId = route.packageId, onNavigateBack = { backStack.removeLast() })
                }

                AppNavKey.AdminEnquiryList -> NavEntry(key = route) {
                    AdminEnquiryListScreen(
                        viewModel = adminFeedbackViewModel,
                        onEnquiryClick = { backStack.add(AppNavKey.AdminEnquiryDetail) },
                        onNavigateBack = { backStack.removeLast() }
                    )
                }

                AppNavKey.AdminEnquiryDetail -> NavEntry(key = route) {
                    AdminEnquiryDetailScreen(
                        viewModel = adminFeedbackViewModel,
                        onNavigateBack = { backStack.removeLast() }
                    )
                }

                AppNavKey.UserProfile -> NavEntry(key = route) {
                    // FIX: Do NOT wrap authRepository, loginUserUseCase, or
                    // viewModel in remember{} here.
                    //
                    // ROOT CAUSE of the re-login bug:
                    //   When the user logged out, navigateToUserLogin() called
                    //   backStack.clear() + backStack.add(AppNavKey.UserProfile).
                    //   NavDisplay re-uses an existing NavEntry for the same key
                    //   when one is alive in the SaveableStateHolder, so the
                    //   remember{}-cached UserProfileViewModel from the *first*
                    //   login session was reused.
                    //
                    //   That stale ViewModel still held isLoginSuccess = true from
                    //   the previous session (it was only reset to false by
                    //   OnResetSuccess *after* navigation — but navigation now
                    //   lands right back on this entry before OnResetSuccess can
                    //   run, so the LaunchedEffect immediately sees isLoginSuccess
                    //   = true and fires onLoginSuccess() again, sending the user
                    //   straight to ProfileMyProfile with no real session).
                    //
                    //   After the second login the new Firebase session is valid,
                    //   but then navigateToProfileItem(MyProfile) lands on
                    //   ProfileMyProfile, which creates a fresh MyProfileViewModel,
                    //   which calls GetUserProfileUseCase → getCurrentUser() →
                    //   auth.currentUser.  The *fresh* Firebase Auth object now
                    //   reports a valid user, so the profile loads fine.
                    //
                    //   But the stale ViewModel's LaunchedEffect fires first and
                    //   navigates away to UserTourList *before* that can happen —
                    //   and because the ViewModel is cached by remember{} across
                    //   the clear()+add() cycle, the old isLoginSuccess flag is
                    //   still true when the screen recomposes.
                    //
                    // FIX:
                    //   Remove remember{} from the ViewModel construction inside
                    //   this NavEntry.  NavDisplay already provides its own
                    //   SaveableStateHolder scoping per key; without remember{}
                    //   the ViewModel is a plain object constructed fresh every
                    //   time this entry is entered (i.e. after each logout).
                    //   The fresh ViewModel starts with isLoginSuccess = false,
                    //   so the LaunchedEffect does NOT fire on entry, and the user
                    //   sees the normal login form.  After a successful re-login
                    //   isLoginSuccess becomes true, onLoginSuccess() fires,
                    //   navigateToUserTourList() runs, and from there My Profile
                    //   works correctly because it uses a separate, independent
                    //   ViewModel that always reads the live Firebase session.
                    val authRepository = provideAuthRepository()
                    val loginUserUseCase = LoginUserUseCase(authRepository)
                    val viewModel = UserProfileViewModel(loginUserUseCase)
                    UserProfileScreen(
                        viewModel = viewModel,
                        onRegisterClick = { backStack.add(AppNavKey.UserRegister) },
                        onAdminLoginClick = { backStack.add(AppNavKey.AdminLogin) },
                        onLoginSuccess = { backStack.navigateToUserTourList() }
                    )
                }

                AppNavKey.UserRegister -> NavEntry(key = route) {
                    val authRepository = provideAuthRepository()
                    val registerUserUseCase = remember { RegisterUserUseCase(authRepository) }
                    val viewModel = remember { UserRegisterViewModel(registerUserUseCase) }
                    UserRegisterScreen(
                        viewModel = viewModel,
                        onBackToLogin = { backStack.removeLast() },
                        onRegistrationSuccess = { backStack.removeLast() }
                    )
                }

                AppNavKey.UserTourList -> NavEntry(key = route) {
                    ProfileDashboardScreen(
                        selectedItem = ProfileDrawerItem.Tours,
                        onItemSelected = { item -> backStack.navigateToProfileItem(item) }
                    ) { openDrawer ->
                        UserTourListScreen(
                            onTourClick = { packageId -> backStack.add(AppNavKey.UserTourDetail(packageId)) },
                            onMenuClick = openDrawer
                        )
                    }
                }

                is AppNavKey.UserTourDetail -> NavEntry(key = route) {
                    TourDetailScreen(
                        packageId = route.packageId,
                        onNavigateBack = { backStack.removeLast() },
                        onBookNow = { pkg ->
                            backStack.add(AppNavKey.Booking(
                                packageId = pkg.id,
                                packageTitle = pkg.title,
                                packagePrice = pkg.price,
                                packageLocation = pkg.location,
                                packageDuration = pkg.duration,
                                packageDescription = pkg.description,
                                packageImageUrl = pkg.imageUrl ?: "",
                                packageIsActive = pkg.isActive
                            ))
                        }
                    )
                }

                is AppNavKey.Booking -> NavEntry(key = route) {
                    val pkg = remember(route) {
                        TourPackage(
                            id = route.packageId,
                            title = route.packageTitle,
                            price = route.packagePrice,
                            location = route.packageLocation,
                            duration = route.packageDuration,
                            description = route.packageDescription,
                            imageUrl = route.packageImageUrl.ifEmpty { null },
                            isActive = route.packageIsActive,
                            includedServices = emptyList(),
                            createdAt = kotlinx.datetime.Clock.System.now(),
                            updatedAt = kotlinx.datetime.Clock.System.now()
                        )
                    }
                    BookingScreen(
                        tourPackage = pkg,
                        onNavigateBack = { backStack.removeLast() },
                        onBookingSuccess = {
                            backStack.clear(); backStack.add(AppNavKey.ProfileMyBooking)
                        },
                        onPaymentFailed = {
                            backStack.clear(); backStack.add(AppNavKey.ProfileTransaction)
                        }
                    )
                }

                AppNavKey.ProfileMyProfile -> NavEntry(key = route) {
                    ProfileDashboardScreen(
                        selectedItem = ProfileDrawerItem.MyProfile,
                        onItemSelected = { item -> backStack.navigateToProfileItem(item) }
                    ) { openDrawer ->
                        MyProfileScreen(
                            onMenuClick = openDrawer,
                            // After successful logout, clear the entire back stack
                            // and navigate to the login screen so the user cannot
                            // press Back to return to the profile.
                            onLogout = { backStack.navigateToUserLogin() }
                        )
                    }
                }

                AppNavKey.ProfileMyBooking -> NavEntry(key = route) {
                    ProfileDashboardScreen(
                        selectedItem = ProfileDrawerItem.MyBooking,
                        onItemSelected = { item -> backStack.navigateToProfileItem(item) }
                    ) { openDrawer -> MyBookingScreen(onMenuClick = openDrawer) }
                }

                AppNavKey.ProfileTransaction -> NavEntry(key = route) {
                    ProfileDashboardScreen(
                        selectedItem = ProfileDrawerItem.Transaction,
                        onItemSelected = { item -> backStack.navigateToProfileItem(item) }
                    ) { openDrawer -> TransactionScreen(onMenuClick = openDrawer) }
                }

                AppNavKey.ProfileFeedback -> NavEntry(key = route) {
                    ProfileDashboardScreen(
                        selectedItem = ProfileDrawerItem.Feedback,
                        onItemSelected = { item -> backStack.navigateToProfileItem(item) }
                    ) { openDrawer -> FeedbackScreen(onMenuClick = openDrawer) }
                }

                null -> error("Unknown route")
            }
        },
        transitionSpec = {
            slideInHorizontally(initialOffsetX = { it }, animationSpec = smoothSpec) togetherWith
                    slideOutHorizontally(targetOffsetX = { -it }, animationSpec = smoothSpec)
        },
        popTransitionSpec = {
            slideInHorizontally(initialOffsetX = { -it }, animationSpec = smoothSpec) togetherWith
                    slideOutHorizontally(targetOffsetX = { it }, animationSpec = smoothSpec)
        },
        predictivePopTransitionSpec = {
            slideInHorizontally(initialOffsetX = { -it }, animationSpec = smoothSpec) togetherWith
                    slideOutHorizontally(targetOffsetX = { it }, animationSpec = smoothSpec)
        }
    )
}

fun NavBackStack<NavKey>.navigateToAdminDashboard() { clear(); add(AppNavKey.AdminDashboard) }

fun NavBackStack<NavKey>.navigateToAdminLogin() {
    clear()                     // remove AdminDashboard and everything above it
    add(AppNavKey.UserProfile)  // User Login sits at the bottom
    add(AppNavKey.AdminLogin)   // AdminLogin is the current (top) screen
}
fun NavBackStack<NavKey>.navigateToUserTourList() { clear(); add(AppNavKey.UserTourList) }
/** After logout: wipe the back stack and land on the login screen. */
fun NavBackStack<NavKey>.navigateToUserLogin() { clear(); add(AppNavKey.UserProfile) }
fun NavBackStack<NavKey>.navigateToProfileItem(item: ProfileDrawerItem) {
    clear()
    add(when (item) {
        ProfileDrawerItem.MyProfile   -> AppNavKey.ProfileMyProfile
        ProfileDrawerItem.Tours       -> AppNavKey.UserTourList
        ProfileDrawerItem.MyBooking   -> AppNavKey.ProfileMyBooking
        ProfileDrawerItem.Transaction -> AppNavKey.ProfileTransaction
        ProfileDrawerItem.Feedback    -> AppNavKey.ProfileFeedback
    })
}

val smoothSpec = tween<IntOffset>(durationMillis = 800, easing = FastOutSlowInEasing)