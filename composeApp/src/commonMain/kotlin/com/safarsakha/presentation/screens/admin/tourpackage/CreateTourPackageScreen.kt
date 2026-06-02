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
import com.safarsakha.domain.usecase.tourpackage.CreateTourPackageUseCase
import kotlinx.datetime.Clock
import kotlin.reflect.KClass

@Composable
expect fun ImagePicker(
    show: Boolean,
    onImagePicked: (ByteArray) -> Unit,
    onDismiss: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTourPackageScreen(
    onNavigateBack: () -> Unit
) {
    val repository = remember { TourPackageRepositoryImpl(FirebaseTourPackageDataSource()) }
    val createUseCase = remember { CreateTourPackageUseCase(repository) }

    val viewModel: CreateTourPackageViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
                @Suppress("UNCHECKED_CAST")
                return CreateTourPackageViewModel(createUseCase, repository) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()
    var showImagePicker by remember { mutableStateOf(false) }

    // Handle initial reset and one-time navigation event
    LaunchedEffect(Unit) {
        viewModel.handleEvent(CreateTourPackageEvent.ResetSuccess)
        viewModel.navigationEvent.collect {
            onNavigateBack()
        }
    }

    ImagePicker(
        show = showImagePicker,
        onImagePicked = { bytes ->
            viewModel.handleEvent(CreateTourPackageEvent.ImageSelected(bytes, "tour_${Clock.System.now().toEpochMilliseconds()}.jpg"))
            showImagePicker = false
        },
        onDismiss = { showImagePicker = false }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Create Tour Package",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )
                },
                navigationIcon = {
                    TextButton(onClick = {
                        viewModel.handleEvent(CreateTourPackageEvent.ResetSuccess)
                        onNavigateBack()
                    }) {
                        Text("Back", color = Color(0xFF1E3A8A))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7FB))
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                onValueChange = { viewModel.handleEvent(CreateTourPackageEvent.TitleChanged(it)) },
                error = uiState.errors["title"]
            )

            AdminTextField(
                label = "Location",
                value = uiState.location,
                onValueChange = { viewModel.handleEvent(CreateTourPackageEvent.LocationChanged(it)) },
                placeholder = "e.g. Manali, Himachal Pradesh",
                error = uiState.errors["location"]
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminTextField(
                    label = "Duration",
                    value = uiState.duration,
                    onValueChange = { viewModel.handleEvent(CreateTourPackageEvent.DurationChanged(it)) },
                    placeholder = "e.g. 5 Days, 4 Nights",
                    modifier = Modifier.weight(1f),
                    error = uiState.errors["duration"]
                )

                AdminTextField(
                    label = "Price (₹)",
                    value = uiState.price,
                    onValueChange = { viewModel.handleEvent(CreateTourPackageEvent.PriceChanged(it)) },
                    placeholder = "0.00",
                    modifier = Modifier.weight(1f),
                    error = uiState.errors["price"]
                )
            }

            AdminTextField(
                label = "Description",
                value = uiState.description,
                onValueChange = { viewModel.handleEvent(CreateTourPackageEvent.DescriptionChanged(it)) },
                minLines = 4,
                error = uiState.errors["description"]
            )

            AdminTextField(
                label = "Included Services",
                value = uiState.includedServices,
                onValueChange = { viewModel.handleEvent(CreateTourPackageEvent.IncludedServicesChanged(it)) },
                placeholder = "Hotel, Meals, Transport, Guide",
                error = uiState.errors["includedServices"]
            )

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
                            .height(180.dp)
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.selectedImageBytes != null) {
                            AsyncImage(
                                model = uiState.selectedImageBytes,
                                contentDescription = "Selected Tour Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            // Success badge overlay
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("✅ Selected", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        } else if (!uiState.imageUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = uiState.imageUrl,
                                contentDescription = "Tour Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🖼️", fontSize = 40.sp)
                                Text("No image selected", color = Color(0xFF64748B), fontSize = 12.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = { showImagePicker = true }) {
                        Text("Select Image", color = Color(0xFF1E3A8A))
                    }
                }
            }

            Button(
                onClick = { viewModel.handleEvent(CreateTourPackageEvent.SavePackage) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save Tour Package", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun AdminTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    error: String? = null
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF334155)
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                if (placeholder.isNotEmpty()) {
                    Text(placeholder, fontSize = 14.sp, color = Color(0xFF94A3B8))
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1E3A8A),
                unfocusedBorderColor = Color(0xFFCBD5E1),
                errorBorderColor = Color.Red
            ),
            isError = error != null,
            minLines = minLines
        )
        if (error != null) {
            Text(
                text = error,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
