package com.safarsakha.presentation.screens.admin.tourpackage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.decodeToImageBitmap
import kotlin.reflect.KClass

// ── Design tokens (matching UserProfileScreen) ──────────────────────────────
private val NavyColor = Color(0xFF0F172A)
private val SkyColor = Color(0xFF0EA5E9)
private val SlateColor = Color(0xFF64748B)
private val BorderColor = Color(0xFFE2E8F0)
private val BgColor = Color(0xFFFFFFFF)
private val LightBgColor = Color(0xFFF8FAFC)
private val ErrorColor = Color(0xFFDC2626)

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
                    Column {
                        Text(
                            text = "Edit Package",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyColor,
                            letterSpacing = (-0.3f).sp
                        )
                        Text(
                            text = "Update tour package details",
                            fontSize = 12.sp,
                            color = SlateColor
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = NavyColor
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
            if (uiState.isLoading) {
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
                            text = "Loading package...",
                            fontSize = 14.sp,
                            color = SlateColor
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // ── FORM ERROR ──────────────────────────────────────────
                    uiState.errorMessage?.let { error ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ErrorColor.copy(alpha = 0.06f))
                                .border(1.dp, ErrorColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("⚠️", fontSize = 16.sp)
                                Text(
                                    text = error,
                                    color = ErrorColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // ── FORM FIELDS ─────────────────────────────────────────
                    EditPremiumTextField(
                        label = "Tour Title",
                        value = uiState.title,
                        onValueChange = { newValue -> viewModel.handleEvent(EditTourPackageEvent.TitleChanged(newValue)) },
                        error = uiState.errors["title"]
                    )

                    EditPremiumTextField(
                        label = "Location",
                        value = uiState.location,
                        onValueChange = { newValue -> viewModel.handleEvent(EditTourPackageEvent.LocationChanged(newValue)) },
                        placeholder = "e.g., Manali, Himachal Pradesh",
                        error = uiState.errors["location"]
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        EditPremiumTextField(
                            label = "Duration",
                            value = uiState.duration,
                            onValueChange = { newValue -> viewModel.handleEvent(EditTourPackageEvent.DurationChanged(newValue)) },
                            placeholder = "5 Days, 4 Nights",
                            modifier = Modifier.weight(1f),
                            error = uiState.errors["duration"]
                        )
                        EditPremiumTextField(
                            label = "Price (₹)",
                            value = uiState.price,
                            onValueChange = { newValue -> viewModel.handleEvent(EditTourPackageEvent.PriceChanged(newValue)) },
                            placeholder = "0.00",
                            modifier = Modifier.weight(1f),
                            error = uiState.errors["price"]
                        )
                    }

                    // ── IMAGE UPLOAD SECTION ──────────────────────────────
                    EditImageUploadSection(
                        selectedImageBytes = uiState.selectedImageBytes,
                        imageUrl = uiState.imageUrl,
                        isUpdating = uiState.isUpdating,
                        onImagePickerClick = { filePickerLauncher.launch() }
                    )

                    EditPremiumTextField(
                        label = "Description",
                        value = uiState.description,
                        onValueChange = { newValue -> viewModel.handleEvent(EditTourPackageEvent.DescriptionChanged(newValue)) },
                        placeholder = "Describe the tour experience...",
                        minLines = 4,
                        error = uiState.errors["description"]
                    )

                    EditPremiumTextField(
                        label = "Included Services",
                        value = uiState.includedServices,
                        onValueChange = { newValue -> viewModel.handleEvent(EditTourPackageEvent.IncludedServicesChanged(newValue)) },
                        placeholder = "Hotel, Meals, Transport, Guide (comma-separated)",
                        error = uiState.errors["includedServices"]
                    )

                    // ── UPDATE BUTTON ──────────────────────────────────────
                    Button(
                        onClick = { viewModel.handleEvent(EditTourPackageEvent.UpdatePackage) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = !uiState.isUpdating,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NavyColor,
                            contentColor = Color.White,
                            disabledContainerColor = NavyColor.copy(alpha = 0.45f),
                            disabledContentColor = Color.White.copy(alpha = 0.60f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                            hoveredElevation = 2.dp
                        )
                    ) {
                        if (uiState.isUpdating) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Text(
                                text = "Update Package",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                letterSpacing = 0.1f.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

// =============================================================================
// EDIT PREMIUM TEXT FIELD
// =============================================================================

@Composable
private fun EditPremiumTextField(
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
            color = NavyColor
        )
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                if (placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder,
                        fontSize = 14.sp,
                        color = SlateColor.copy(alpha = 0.45f)
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            isError = error != null,
            minLines = minLines,
            singleLine = minLines == 1,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SkyColor,
                focusedLabelColor = SkyColor,
                focusedTextColor = NavyColor,
                focusedContainerColor = Color.Transparent,
                unfocusedBorderColor = BorderColor,
                unfocusedLabelColor = SlateColor,
                unfocusedTextColor = NavyColor,
                unfocusedContainerColor = Color.Transparent,
                disabledBorderColor = BorderColor.copy(alpha = 0.45f),
                disabledLabelColor = SlateColor.copy(alpha = 0.35f),
                disabledTextColor = NavyColor.copy(alpha = 0.35f),
                disabledContainerColor = BorderColor.copy(alpha = 0.12f),
                errorBorderColor = ErrorColor,
                errorLabelColor = ErrorColor,
                errorTextColor = NavyColor,
                errorContainerColor = Color.Transparent
            )
        )

        if (error != null) {
            Text(
                text = error,
                color = ErrorColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// =============================================================================
// EDIT IMAGE UPLOAD SECTION
// =============================================================================

@Composable
private fun EditImageUploadSection(
    selectedImageBytes: ByteArray?,
    imageUrl: String?,
    isUpdating: Boolean,
    onImagePickerClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = NavyColor.copy(alpha = 0.04f),
                spotColor = NavyColor.copy(alpha = 0.08f)
            )
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = NavyColor.copy(alpha = 0.02f),
                spotColor = NavyColor.copy(alpha = 0.04f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = BgColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            Color(0xFFF4E7D3).copy(alpha = 0.85f),
                            BorderColor.copy(alpha = 0.60f),
                            BorderColor.copy(alpha = 0.25f)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tour Image",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = NavyColor,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Image preview box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFF1F5F9),
                                Color(0xFFF8FAFC)
                            )
                        )
                    )
                    .border(1.dp, BorderColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                when {
                    selectedImageBytes != null -> {
                        // Preview new selection
                        val bitmap = remember(selectedImageBytes) {
                            selectedImageBytes.decodeToImageBitmap()
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap,
                                contentDescription = "Selected image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            // New badge
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SkyColor.copy(alpha = 0.9f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "NEW",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            Text("Failed to load image", color = ErrorColor)
                        }
                    }

                    !imageUrl.isNullOrEmpty() -> {
                        // Show existing image from Firebase
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Tour image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    else -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(SkyColor.copy(alpha = 0.1f))
                                    .border(1.dp, SkyColor.copy(alpha = 0.2f), RoundedCornerShape(50)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AddAPhoto,
                                    contentDescription = null,
                                    tint = SkyColor,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Text(
                                text = "No image available",
                                color = SlateColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onImagePickerClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isUpdating,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = NavyColor
                ),
                border = BorderStroke(1.dp, SkyColor)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AddAPhoto,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = SkyColor
                    )
                    Text(
                        text = if (selectedImageBytes != null || !imageUrl.isNullOrEmpty()) "Change Image" else "Select Image",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = SkyColor
                    )
                }
            }

            if (!imageUrl.isNullOrEmpty() && selectedImageBytes == null) {
                Text(
                    text = "Current image from server",
                    color = SlateColor.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}