package com.safarsakha.presentation.screens.admin.tourpackage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.safarsakha.data.remote.firebase.FirebaseTourPackageDataSource
import com.safarsakha.data.repository.impl.TourPackageRepositoryImpl
import com.safarsakha.domain.usecase.tourpackage.GetTourPackageByIdUseCase
import com.safarsakha.domain.usecase.tourpackage.UpdateTourPackageUseCase
import com.safarsakha.presentation.screens.admin.tourpackage.components.AdminTextField
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.decodeToImageBitmap
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

    // FIXED: Pass repository as third parameter
    val viewModel: EditTourPackageViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
                @Suppress("UNCHECKED_CAST")
                return EditTourPackageViewModel(getByIdUseCase, updateUseCase, repository) as T
            }
        }
    )
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Image Picker Launcher
    val filePickerLauncher = rememberFilePickerLauncher(
        type = PickerType.Image,
        onResult = { platformFile ->
            if (platformFile != null) {
                scope.launch {
                    try {
                        val fileName = platformFile.name
                        val imageExtensions = listOf("jpg", "jpeg", "png", "gif", "webp")
                        val extension = fileName.substringAfterLast(".").lowercase()
                        
                        if (extension in imageExtensions) {
                            val imageBytes = platformFile.readBytes()
                            viewModel.handleEvent(
                                EditTourPackageEvent.ImageSelected(
                                    imageBytes = imageBytes,
                                    fileName = fileName
                                )
                            )
                            snackbarHostState.showSnackbar("Image selected successfully")
                        } else {
                            snackbarHostState.showSnackbar("Please select a valid image file")
                        }
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Error loading image: ${e.message}")
                    }
                }
            }
        }
    )

    LaunchedEffect(packageId) {
        viewModel.handleEvent(EditTourPackageEvent.LoadPackage(packageId))
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect {
            onNavigateBack()
        }
    }

    // Show error message if any
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(error)
            }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit Tour Package",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )
                },
                navigationIcon = {
                    TextButton(onClick = { onNavigateBack() }) {
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
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Show form error if any
                    uiState.errorMessage?.let { error ->
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AdminTextField(
                            label = "Duration",
                            value = uiState.duration,
                            onValueChange = { viewModel.handleEvent(EditTourPackageEvent.DurationChanged(it)) },
                            modifier = Modifier.weight(1f),
                            error = uiState.errors["duration"]
                        )
                        AdminTextField(
                            label = "Price (₹)",
                            value = uiState.price,
                            onValueChange = { viewModel.handleEvent(EditTourPackageEvent.PriceChanged(it)) },
                            modifier = Modifier.weight(1f),
                            error = uiState.errors["price"]
                        )
                    }

                    // Image Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
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
                                    .height(180.dp)
                                    .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                                    .clip(RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    uiState.selectedImageBytes != null -> {
                                        // Preview new selection
                                        val bitmap = remember(uiState.selectedImageBytes) {
                                            uiState.selectedImageBytes?.decodeToImageBitmap()
                                        }
                                        if (bitmap != null) {
                                            Image(
                                                bitmap = bitmap,
                                                contentDescription = "Selected image",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                            Badge(
                                                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                                                containerColor = Color(0xFF059669)
                                            ) {
                                                Text("NEW", color = Color.White, fontSize = 10.sp)
                                            }
                                        } else {
                                            Text("Failed to load image", color = Color.Red)
                                        }
                                    }
                                    !uiState.imageUrl.isNullOrEmpty() -> {
                                        // Show existing image from Firebase
                                        AsyncImage(
                                            model = uiState.imageUrl,
                                            contentDescription = "Tour image",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    else -> {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("🖼️", fontSize = 48.sp)
                                            Text("No image available", color = Color.Gray, fontSize = 14.sp)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            TextButton(
                                onClick = {
                                    // Launch the actual image picker
                                    filePickerLauncher.launch()
                                },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = Color(0xFF1E3A8A)
                                )
                            ) {
                                Text("Change Image", color = Color(0xFF1E3A8A))
                            }
                        }
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
                        placeholder = "Hotel, Meals, Transport, Guide (comma-separated)",
                        error = uiState.errors["includedServices"]
                    )

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
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text("Update Package", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}