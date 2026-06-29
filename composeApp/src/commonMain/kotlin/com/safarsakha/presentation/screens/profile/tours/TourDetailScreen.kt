package com.safarsakha.presentation.screens.profile.tours

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.safarsakha.data.remote.firebase.FirebaseEnquiryDataSource
import com.safarsakha.data.remote.firebase.FirebaseTourPackageDataSource
import com.safarsakha.data.repository.impl.EnquiryRepositoryImpl
import com.safarsakha.data.repository.impl.TourPackageRepositoryImpl
import com.safarsakha.domain.usecase.tourpackage.GetTourPackageByIdUseCase
import kotlinx.coroutines.flow.collectLatest
import kotlin.reflect.KClass

// ── Premium Design Tokens ────────────────────────────────────────────────────
private val NavyColor = Color(0xFF0F172A)
private val SkyColor = Color(0xFF0EA5E9)
private val SlateColor = Color(0xFF64748B)
private val BorderColor = Color(0xFFE2E8F0)
private val BgColor = Color(0xFFFFFFFF)
private val LightBgColor = Color(0xFFF8FAFC)
private val ErrorColor = Color(0xFFDC2626)
private val SuccessColor = Color(0xFF16A34A)
private val SuccessBgColor = Color(0xFFF0FDF4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TourDetailScreen(
    packageId: String,
    onNavigateBack: () -> Unit,
    onBookNow: ((com.safarsakha.domain.model.TourPackage) -> Unit)? = null
) {
    val repository = remember { TourPackageRepositoryImpl(FirebaseTourPackageDataSource()) }
    val enquiryRepository = remember { EnquiryRepositoryImpl(FirebaseEnquiryDataSource()) }
    val getTourPackageByIdUseCase = remember { GetTourPackageByIdUseCase(repository) }

    val viewModel: TourDetailViewModel = viewModel(
        factory = remember(getTourPackageByIdUseCase, enquiryRepository) {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
                    @Suppress("UNCHECKED_CAST")
                    return TourDetailViewModel(getTourPackageByIdUseCase, enquiryRepository) as T
                }
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(packageId) {
        viewModel.handleEvent(TourDetailEvent.LoadPackage(packageId))
    }

    LaunchedEffect(Unit) {
        viewModel.showSnackbar.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    if (uiState.showEnquiryDialog) {
        EnquiryDialog(
            tourName = uiState.tourPackage?.title ?: "",
            onDismiss = { viewModel.handleEvent(TourDetailEvent.DismissEnquiryDialog) },
            onSend = { message -> viewModel.handleEvent(TourDetailEvent.SubmitEnquiry(message)) }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Tour Details",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyColor,
                            letterSpacing = (-0.3f).sp
                        )
                        uiState.tourPackage?.let {
                            Text(
                                text = it.location,
                                fontSize = 12.sp,
                                color = SlateColor
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back",
                            tint = NavyColor
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.handleEvent(TourDetailEvent.OpenEnquiryDialog) },
                        enabled = !uiState.isSubmittingEnquiry,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .height(38.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SkyColor,
                            disabledContainerColor = BorderColor
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        if (uiState.isSubmittingEnquiry) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sending...",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "Send Enquiry",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                },
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
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = SkyColor,
                        strokeWidth = 3.dp
                    )
                }

                uiState.errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
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
                                text = "Couldn't load details",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NavyColor,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = uiState.errorMessage ?: "Unknown error occurred",
                                fontSize = 13.sp,
                                color = SlateColor,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = { viewModel.handleEvent(TourDetailEvent.Retry) },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NavyColor)
                            ) {
                                Text("Retry", color = Color.White, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                uiState.tourPackage != null -> {
                    val tourPackage = uiState.tourPackage!!

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Cover image section
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .background(BorderColor.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!tourPackage.imageUrl.isNullOrEmpty()) {
                                var isLoading by remember { mutableStateOf(true) }
                                var isError by remember { mutableStateOf(false) }

                                AsyncImage(
                                    model = tourPackage.imageUrl,
                                    contentDescription = tourPackage.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    onState = { state ->
                                        isLoading = state is AsyncImagePainter.State.Loading
                                        isError = state is AsyncImagePainter.State.Error
                                    }
                                )
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(36.dp), color = SkyColor)
                                }
                                if (isError) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("⚠️", fontSize = 28.sp)
                                        Text("Image failed to load", color = ErrorColor, fontSize = 12.sp)
                                    }
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("📷", fontSize = 40.sp)
                                    Text("No Image Available", color = SlateColor, fontSize = 13.sp)
                                }
                            }
                        }

                        // Details Core Container
                        Column(modifier = Modifier.padding(24.dp)) {
                            // Title + Price
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = tourPackage.title,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyColor,
                                    modifier = Modifier.weight(1f),
                                    letterSpacing = (-0.5f).sp
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = SuccessBgColor
                                ) {
                                    Text(
                                        text = "₹${tourPackage.price}",
                                        color = SuccessColor,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Horizontal metadata elements
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📍", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(tourPackage.location, fontSize = 14.sp, color = SlateColor)
                                Spacer(modifier = Modifier.width(24.dp))
                                Text("⏱️", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(tourPackage.duration, fontSize = 14.sp, color = SlateColor)
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            HorizontalDivider(color = BorderColor.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(24.dp))

                            // Description Section
                            Text("About this tour", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NavyColor)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = tourPackage.description,
                                fontSize = 14.sp,
                                color = NavyColor.copy(alpha = 0.8f),
                                lineHeight = 22.sp
                            )

                            Spacer(modifier = Modifier.height(24.dp))
                            HorizontalDivider(color = BorderColor.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(24.dp))

                            // What's Included Section
                            Text("What's Included", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NavyColor)
                            Spacer(modifier = Modifier.height(14.dp))

                            if (tourPackage.includedServices.isEmpty()) {
                                Text("No included services listed for this package.", fontSize = 13.sp, color = SlateColor)
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    tourPackage.includedServices.forEach { service ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(RoundedCornerShape(50))
                                                    .background(SuccessBgColor),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("✓", color = SuccessColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(service, fontSize = 14.sp, color = NavyColor.copy(alpha = 0.9f))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))
                            HorizontalDivider(color = BorderColor.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(24.dp))

                            // Action Booking Button
                            Button(
                                onClick = { onBookNow?.invoke(tourPackage) },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NavyColor),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(
                                    text = "Book Now",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}