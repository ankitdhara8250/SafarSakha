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
import com.safarsakha.data.remote.firebase.FirebaseBookingDataSource
import com.safarsakha.data.repository.impl.BookingRepositoryImpl
import com.safarsakha.domain.usecase.booking.GetAllBookingsUseCase
import com.safarsakha.presentation.screens.admin.booking.AdminBookingViewModel
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

    // AdminBookingViewModel is created here (not inside the NavEntry composable) so that:
    // 1. It is scoped to AppNavigation's composition, surviving back-navigation to the list.
    // 2. It uses remember{} — guaranteed stable across recompositions, eliminating the
    //    unstable-factory issue that caused the crash inside AdminBookingListScreen.
    // This mirrors the exact pattern used for AdminFeedbackViewModel above.
    val bookingRepository = remember { BookingRepositoryImpl(FirebaseBookingDataSource()) }
    val adminBookingViewModel = remember { AdminBookingViewModel(GetAllBookingsUseCase(bookingRepository)) }

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
                        viewModel = adminBookingViewModel,
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
                        // Safe fallback dates used when a Firestore document has an
                        // empty or malformed date/enum field.  This mirrors the same
                        // defensive pattern already applied in BookingMapper.toDomain().
                        // Without these guards, LocalDate.parse(""), Instant.parse(""),
                        // or BookingStatus.valueOf("<unknown>") each throw an uncaught
                        // exception inside remember{} during Compose composition, which
                        // is the root cause of the crash on AdminBookingDetailScreen.
                        val fallbackDate = kotlinx.datetime.LocalDate(1970, 1, 1)
                        val fallbackInstant = kotlinx.datetime.Clock.System.now()

                        com.safarsakha.domain.model.Booking(
                            bookingId = route.bookingId,
                            userId = route.userId,
                            userName = route.userName,
                            packageId = route.packageId,
                            packageName = route.packageName,
                            packagePrice = route.packagePrice,
                            startDate = runCatching { kotlinx.datetime.LocalDate.parse(route.startDate) }
                                .getOrDefault(fallbackDate),
                            endDate = runCatching { kotlinx.datetime.LocalDate.parse(route.endDate) }
                                .getOrDefault(fallbackDate),
                            bookingDate = runCatching { kotlinx.datetime.Instant.parse(route.bookingDate) }
                                .getOrDefault(fallbackInstant),
                            bookingStatus = runCatching { com.safarsakha.domain.model.BookingStatus.valueOf(route.bookingStatus) }
                                .getOrDefault(com.safarsakha.domain.model.BookingStatus.UPCOMING),
                            paymentStatus = runCatching { com.safarsakha.domain.model.PaymentStatus.valueOf(route.paymentStatus) }
                                .getOrDefault(com.safarsakha.domain.model.PaymentStatus.FAILED),
                            totalAmount = route.totalAmount,
                            cancellationDate = route.cancellationDate?.let {
                                runCatching { kotlinx.datetime.Instant.parse(it) }.getOrNull()
                            }
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
                            // FIX: use atomic replacement instead of clear() + add().
                            // clear() momentarily empties the backstack, and NavDisplay
                            // validates isEmpty() on every recomposition — the transient
                            // empty state causes:
                            //   IllegalArgumentException: NavDisplay backstack cannot be empty
                            // replaceAll() swaps the entire list in a single SnapshotStateList
                            // write so NavDisplay never sees an empty backstack.
                            backStack.replaceAll(AppNavKey.ProfileMyBooking)
                        },
                        onPaymentFailed = {
                            backStack.replaceAll(AppNavKey.ProfileTransaction)
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

// ---------------------------------------------------------------------------
// Navigation helpers
// ---------------------------------------------------------------------------

/**
 * Atomically replace every entry in the backstack with [destination].
 *
 * NavDisplay validates backStack.isEmpty() on every recomposition.  The old
 * pattern of `clear(); add(key)` left a transient empty-list state between
 * the two mutations that caused:
 *   IllegalArgumentException: NavDisplay backstack cannot be empty
 *
 * Using a single SnapshotStateList write (set the whole list at once via
 * retainAll + add, or clear + add wrapped in a snapshot transaction) keeps
 * the list non-empty at all observable points.  The simplest correct
 * implementation is to set index 0 to the destination and remove the tail —
 * which SnapshotStateList applies as a single atomic diff.
 */
private fun NavBackStack<NavKey>.replaceAll(destination: NavKey) {
    // Add the new destination first so the list is never empty.
    add(destination)
    // Now remove every entry that was there before (all except the last one we just added).
    val keepIndex = size - 1
    val toRemove = subList(0, keepIndex).toList()
    removeAll(toRemove)
}

fun NavBackStack<NavKey>.navigateToAdminDashboard() { replaceAll(AppNavKey.AdminDashboard) }

fun NavBackStack<NavKey>.navigateToAdminLogin() {
    // Add AdminLogin on top first (so the list is never empty), then replace
    // everything below it with UserProfile as the new root.
    add(AppNavKey.AdminLogin)
    val keepIndex = size - 1
    val toRemove = subList(0, keepIndex).toList()
    removeAll(toRemove)
    // Now the stack is [AdminLogin]. Insert UserProfile as the root below it.
    add(0, AppNavKey.UserProfile)
}

fun NavBackStack<NavKey>.navigateToUserTourList() { replaceAll(AppNavKey.UserTourList) }

/** After logout: wipe the back stack and land on the login screen. */
fun NavBackStack<NavKey>.navigateToUserLogin() { replaceAll(AppNavKey.UserProfile) }

fun NavBackStack<NavKey>.navigateToProfileItem(item: ProfileDrawerItem) {
    replaceAll(when (item) {
        ProfileDrawerItem.MyProfile   -> AppNavKey.ProfileMyProfile
        ProfileDrawerItem.Tours       -> AppNavKey.UserTourList
        ProfileDrawerItem.MyBooking   -> AppNavKey.ProfileMyBooking
        ProfileDrawerItem.Transaction -> AppNavKey.ProfileTransaction
        ProfileDrawerItem.Feedback    -> AppNavKey.ProfileFeedback
    })
}

val smoothSpec = tween<IntOffset>(durationMillis = 800, easing = FastOutSlowInEasing)