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
import com.safarsakha.domain.usecase.tourpackage.CreateTourPackageUseCase
import kotlin.reflect.KClass

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
                return CreateTourPackageViewModel(createUseCase) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()

    // Handle initial reset and one-time navigation event
    LaunchedEffect(Unit) {
        // IMPORTANT: Reset the form every time this screen is entered.
        // This ensures the form is blank on the second "Add" attempt.
        viewModel.handleEvent(CreateTourPackageEvent.ResetSuccess)
        
        viewModel.navigationEvent.collect {
            onNavigateBack()
        }
    }

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
                        // Clear state on manual back/cancel as well
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
                            .height(120.dp)
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.selectedImageBytes != null) {
                            Text("✅ Image Selected", color = Color(0xFF059669))
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🖼️", fontSize = 32.sp)
                                Text("No image selected", color = Color(0xFF64748B), fontSize = 12.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = { /* Implement image picker */ }) {
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
