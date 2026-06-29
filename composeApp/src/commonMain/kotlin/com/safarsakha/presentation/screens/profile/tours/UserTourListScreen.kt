package com.safarsakha.presentation.screens.profile.tours

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.safarsakha.data.remote.firebase.FirebaseTourPackageDataSource
import com.safarsakha.data.repository.impl.TourPackageRepositoryImpl
import com.safarsakha.domain.usecase.tourpackage.GetActiveTourPackagesUseCase
import com.safarsakha.presentation.screens.profile.profiledashboard.components.HamburgerMenuButton
import com.safarsakha.presentation.screens.profile.tours.components.UserTourCard
import kotlin.reflect.KClass

// ── Premium Design Tokens ────────────────────────────────────────────────────
private val NavyColor = Color(0xFF0F172A)
private val SkyColor = Color(0xFF0EA5E9)
private val SlateColor = Color(0xFF64748B)
private val BorderColor = Color(0xFFE2E8F0)
private val BgColor = Color(0xFFFFFFFF)
private val LightBgColor = Color(0xFFF8FAFC)
private val ErrorColor = Color(0xFFDC2626)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserTourListScreen(
    onTourClick: (String) -> Unit,
    onMenuClick: () -> Unit = {}
) {
    val repository = remember { TourPackageRepositoryImpl(FirebaseTourPackageDataSource()) }
    val getActiveTourPackagesUseCase = remember { GetActiveTourPackagesUseCase(repository) }

    // FIX: Encapsulated inside remember {} to guarantee factory references remain stable
    // across target architectural recompositions, preventing flow restarts.
    val viewModel: UserTourListViewModel = viewModel(
        factory = remember(getActiveTourPackagesUseCase) {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
                    @Suppress("UNCHECKED_CAST")
                    return UserTourListViewModel(getActiveTourPackagesUseCase) as T
                }
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Explore Tours",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyColor,
                            letterSpacing = (-0.3f).sp
                        )
                        Text(
                            text = "Discover your next adventure",
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
                is UserTourListUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = SkyColor,
                            strokeWidth = 3.dp
                        )
                    }
                }

                is UserTourListUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // ── City Filter UI (pinned as first item) ────────────────────────
                        item(key = "city_filter") {
                            CityFilterBar(
                                cityFilter = state.cityFilter,
                                onCityChanged = { city ->
                                    viewModel.handleEvent(UserTourListEvent.FilterByCity(city))
                                },
                                onClearFilter = {
                                    viewModel.handleEvent(UserTourListEvent.ClearCityFilter)
                                }
                            )
                        }

                        // ── No-match state (city typed but nothing found) ─────────────────
                        if (state.cityFilter.isNotBlank() && state.packages.isEmpty()) {
                            item(key = "city_no_match") {
                                CityNoMatchState(
                                    city = state.cityFilter,
                                    onClearFilter = {
                                        viewModel.handleEvent(UserTourListEvent.ClearCityFilter)
                                    }
                                )
                            }
                        } else {
                            // ── Tour cards ───────────────────────────────────────────────
                            items(
                                items = state.packages,
                                key = { it.id.ifEmpty { "pkg_${it.hashCode()}" } }
                            ) { packageItem ->
                                UserTourCard(
                                    tourPackage = packageItem,
                                    onClick = { onTourClick(packageItem.id) }
                                )
                            }
                        }
                    }
                }

                is UserTourListUiState.Empty -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(50))
                                .background(SkyColor.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🌍", fontSize = 28.sp)
                        }
                        Text(
                            text = "No Tours Available",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NavyColor
                        )
                        Text(
                            text = "Check back later for exciting new travel packages.",
                            fontSize = 13.sp,
                            color = SlateColor.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                is UserTourListUiState.Error -> {
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
                                text = "Couldn't load tours",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NavyColor,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = state.message,
                                fontSize = 13.sp,
                                color = SlateColor,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = { viewModel.handleEvent(UserTourListEvent.RefreshPackages) },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NavyColor)
                            ) {
                                Text(
                                    text = "Retry",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── City Filter Bar ──────────────────────────────────────────────────────────

/**
 * A self-contained, Material 3-styled city search bar.
 *
 * Design decisions:
 * - [OutlinedTextField] keeps it visually consistent with the rest of the app.
 * - The trailing clear icon (✕) appears only when there is text — avoids visual clutter.
 * - Keyboard IME action "Search" dismisses the keyboard and applies the filter immediately.
 * - Capitalises the first letter of every word automatically (cities), reducing friction.
 */
@Composable
private fun CityFilterBar(
    cityFilter: String,
    onCityChanged: (String) -> Unit,
    onClearFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = cityFilter,
        onValueChange = onCityChanged,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = "Filter by city…",
                fontSize = 14.sp,
                color = SlateColor.copy(alpha = 0.6f)
            )
        },
        leadingIcon = {
            Text(
                text = "📍",
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        },
        trailingIcon = {
            AnimatedVisibility(
                visible = cityFilter.isNotEmpty(),
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                IconButton(onClick = {
                    onClearFilter()
                    keyboardController?.hide()
                }) {
                    Text(
                        text = "✕",
                        fontSize = 14.sp,
                        color = SlateColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        label = {
            Text(
                text = "City",
                fontSize = 12.sp,
                color = SlateColor
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = { keyboardController?.hide() }
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SkyColor,
            unfocusedBorderColor = BorderColor,
            focusedLabelColor = SkyColor,
            unfocusedLabelColor = SlateColor,
            cursorColor = SkyColor
        )
    )
}

// ── No-Match State ───────────────────────────────────────────────────────────

/**
 * Shown when a city filter is active but no tour matches.
 * Includes a "Clear Filter" button to restore the full list immediately.
 */
@Composable
private fun CityNoMatchState(
    city: String,
    onClearFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(50))
                .background(SlateColor.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Text("🏙️", fontSize = 28.sp)
        }
        Text(
            text = "Sorry, currently service is not available for this city.",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = NavyColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Text(
            text = "We couldn't find any tours in \"$city\".",
            fontSize = 13.sp,
            color = SlateColor.copy(alpha = 0.75f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedButton(
            onClick = onClearFilter,
            shape = RoundedCornerShape(14.dp),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                // Reuse SkyColor for the outline to match the filter field accent
            ),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyColor)
        ) {
            Text(
                text = "Clear Filter",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}