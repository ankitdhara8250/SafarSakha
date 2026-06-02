package com.safarsakha.presentation.screens.admin.tourpackage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.safarsakha.data.remote.firebase.FirebaseTourPackageDataSource
import com.safarsakha.data.repository.impl.TourPackageRepositoryImpl
import com.safarsakha.domain.usecase.tourpackage.GetTourPackageByIdUseCase
import com.safarsakha.domain.usecase.tourpackage.UpdateTourPackageUseCase
import com.safarsakha.presentation.screens.admin.tourpackage.components.AdminTextField
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTourPackageScreen(
    packageId: String,
    onNavigateBack: () -> Unit
) {
    val repository = remember {
        TourPackageRepositoryImpl(FirebaseTourPackageDataSource())
    }
    val getByIdUseCase = remember { GetTourPackageByIdUseCase(repository) }
    val updateUseCase = remember { UpdateTourPackageUseCase(repository) }

    val viewModel: EditTourPackageViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
                @Suppress("UNCHECKED_CAST")
                return EditTourPackageViewModel(getByIdUseCase, updateUseCase) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Load package data on start
    LaunchedEffect(packageId) {
        viewModel.handleEvent(EditTourPackageEvent.LoadPackage(packageId))
    }

    // Handle one-time navigation event
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect {
            snackbarHostState.showSnackbar("Package updated successfully")
            onNavigateBack()
        }
    }

    // Handle error messages
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit Tour Package",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )
                },
                navigationIcon = {
                    TextButton(onClick = {
                        viewModel.handleEvent(EditTourPackageEvent.ResetSuccess)
                        onNavigateBack()
                    }) {
                        Text("Cancel", color = Color(0xFF1E3A8A))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
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
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF1E3A8A)
                )
            } else if (uiState.tourPackage == null && uiState.errorMessage != null) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("❌ Failed to load package", color = Color.Red, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.handleEvent(EditTourPackageEvent.LoadPackage(packageId)) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A))
                    ) {
                        Text("Retry")
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Show form error if any (global errors)
                    uiState.errors["form"]?.let { error ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3F3)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = error,
                                color = Color.Red,
                                modifier = Modifier.padding(12.dp),
                                fontSize = 14.sp
                            )
                        }
                    }

                    AdminTextField(
                        label = "Tour Title",
                        value = uiState.title,
                        onValueChange = { viewModel.handleEvent(EditTourPackageEvent.TitleChanged(it)) },
                        error = uiState.errors["title"]
                    )

                    AdminTextField(
                        label = "Location",
                        value = uiState.location,
                        onValueChange = { viewModel.handleEvent(EditTourPackageEvent.LocationChanged(it)) },
                        placeholder = "e.g. Manali, Himachal Pradesh",
                        error = uiState.errors["location"]
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AdminTextField(
                            label = "Duration",
                            value = uiState.duration,
                            onValueChange = { viewModel.handleEvent(EditTourPackageEvent.DurationChanged(it)) },
                            placeholder = "e.g. 5 Days, 4 Nights",
                            modifier = Modifier.weight(1f),
                            error = uiState.errors["duration"]
                        )

                        AdminTextField(
                            label = "Price (₹)",
                            value = uiState.price,
                            onValueChange = { viewModel.handleEvent(EditTourPackageEvent.PriceChanged(it)) },
                            placeholder = "0.00",
                            modifier = Modifier.weight(1f),
                            error = uiState.errors["price"]
                        )
                    }

                    AdminTextField(
                        label = "Description",
                        value = uiState.description,
                        onValueChange = { viewModel.handleEvent(EditTourPackageEvent.DescriptionChanged(it)) },
                        minLines = 4,
                        error = uiState.errors["description"]
                    )

                    AdminTextField(
                        label = "Included Services",
                        value = uiState.includedServices,
                        onValueChange = { viewModel.handleEvent(EditTourPackageEvent.IncludedServicesChanged(it)) },
                        placeholder = "Hotel, Meals, Transport, Guide (comma separated)",
                        error = uiState.errors["includedServices"]
                    )

                    // Image Card (consistent with Create screen)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Tour Image",
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFF334155)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (uiState.selectedImageBytes != null) {
                                    Text("✅ New Image Selected", color = Color(0xFF059669))
                                } else if (!uiState.imageUrl.isNullOrEmpty()) {
                                    Text("🖼️ Existing Image Loaded", color = Color(0xFF1E3A8A))
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("🖼️", fontSize = 32.sp)
                                        Text("No image selected", color = Color(0xFF64748B), fontSize = 12.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            TextButton(
                                onClick = {
                                    // TODO: Implement image picker
                                }
                            ) {
                                Text("Change Image", color = Color(0xFF1E3A8A))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.handleEvent(EditTourPackageEvent.UpdatePackage) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                        enabled = !uiState.isUpdating,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState.isUpdating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Update Tour Package", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
