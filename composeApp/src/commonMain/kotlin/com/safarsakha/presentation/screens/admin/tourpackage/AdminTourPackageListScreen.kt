package com.safarsakha.presentation.screens.admin.tourpackage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.safarsakha.presentation.screens.admin.tourpackage.components.EmptyTourPackageState
import com.safarsakha.presentation.screens.admin.tourpackage.components.TourPackageCard
import com.safarsakha.domain.usecase.tourpackage.DeleteTourPackageUseCase
import com.safarsakha.data.repository.impl.TourPackageRepositoryImpl
import com.safarsakha.data.remote.firebase.FirebaseTourPackageDataSource
import com.safarsakha.domain.usecase.tourpackage.GetTourPackagesUseCase
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

// ── Design tokens (matching UserProfileScreen) ──────────────────────────────
private val NavyColor = Color(0xFF0F172A)
private val SkyColor = Color(0xFF0EA5E9)
private val SlateColor = Color(0xFF64748B)
private val BorderColor = Color(0xFFE2E8F0)
private val BgColor = Color(0xFFFFFFFF)
private val LightBgColor = Color(0xFFF8FAFC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTourPackageListScreen(
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateBack: () -> Unit = {}
) {
    val repository = remember {
        TourPackageRepositoryImpl(FirebaseTourPackageDataSource())
    }
    val getPackagesUseCase = remember { GetTourPackagesUseCase(repository) }
    val deletePackageUseCase = remember { DeleteTourPackageUseCase(repository) }

    val viewModel: AdminTourPackageListViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
                @Suppress("UNCHECKED_CAST")
                return AdminTourPackageListViewModel(getPackagesUseCase, deletePackageUseCase) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.showSnackbar.collect { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Tour Packages",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyColor,
                            letterSpacing = (-0.3f).sp
                        )
                        Text(
                            text = "Manage your tour packages",
                            fontSize = 12.sp,
                            color = SlateColor
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = NavyColor
                        )
                    }
                },
                actions = {
                    FloatingActionButton(
                        onClick = onNavigateToCreate,
                        containerColor = NavyColor,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Add Package",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgColor,
                    scrolledContainerColor = BgColor
                ),
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = BorderColor.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(0.dp)
                    )
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = NavyColor,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBgColor)
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is AdminTourPackageListUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = SkyColor,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Loading packages...",
                                fontSize = 14.sp,
                                color = SlateColor
                            )
                        }
                    }
                }

                is AdminTourPackageListUiState.Success -> {
                    if (state.packages.isEmpty()) {
                        EmptyTourPackageState(
                            onCreateClick = onNavigateToCreate
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(
                                items = state.packages,
                                key = { it.id.ifEmpty { "pkg_${it.hashCode()}" } }
                            ) { packageItem ->
                                TourPackageCard(
                                    tourPackage = packageItem,
                                    onEditClick = { onNavigateToEdit(packageItem.id) },
                                    onDeleteClick = {
                                        viewModel.handleEvent(AdminTourPackageListEvent.DeletePackage(packageItem.id))
                                    },
                                    onClick = { onNavigateToEdit(packageItem.id) }
                                )
                            }
                        }
                    }
                }

                is AdminTourPackageListUiState.Empty -> {
                    EmptyTourPackageState(
                        onCreateClick = onNavigateToCreate
                    )
                }

                is AdminTourPackageListUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(24.dp)
                        ) {
                            // Error icon
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.Red.copy(alpha = 0.08f))
                                    .border(1.dp, Color.Red.copy(alpha = 0.15f), RoundedCornerShape(50)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "⚠️",
                                    fontSize = 32.sp
                                )
                            }

                            Text(
                                text = "Failed to Load Packages",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyColor
                            )

                            Text(
                                text = state.message,
                                fontSize = 14.sp,
                                color = SlateColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(0.8f)
                            )

                            Button(
                                onClick = { viewModel.handleEvent(AdminTourPackageListEvent.LoadPackages) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NavyColor,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .padding(horizontal = 32.dp)
                            ) {
                                Text(
                                    text = "Retry",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}