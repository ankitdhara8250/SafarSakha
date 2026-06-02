package com.safarsakha.presentation.screens.admin.tourpackage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.safarsakha.presentation.screens.admin.tourpackage.components.EmptyTourPackageState
import com.safarsakha.presentation.screens.admin.tourpackage.components.TourPackageCard
import com.safarsakha.domain.usecase.tourpackage.DeleteTourPackageUseCase
import com.safarsakha.data.repository.impl.TourPackageRepositoryImpl
import com.safarsakha.data.remote.firebase.FirebaseTourPackageDataSource
import com.safarsakha.domain.usecase.tourpackage.GetTourPackagesUseCase
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTourPackageListScreen(
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (String) -> Unit
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
                    Text(
                        text = "Tour Packages",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )
                },
                actions = {
                    TextButton(
                        onClick = onNavigateToCreate,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFF1E3A8A)
                        )
                    ) {
                        Text("+ Add", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7FB))
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is AdminTourPackageListUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF1E3A8A))
                    }
                }

                is AdminTourPackageListUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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

                is AdminTourPackageListUiState.Empty -> {
                    EmptyTourPackageState(onCreateClick = onNavigateToCreate)
                }

                is AdminTourPackageListUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "❌ Error loading packages",
                                fontSize = 18.sp,
                                color = Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.message,
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { viewModel.handleEvent(AdminTourPackageListEvent.LoadPackages) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A))
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
    }
}
