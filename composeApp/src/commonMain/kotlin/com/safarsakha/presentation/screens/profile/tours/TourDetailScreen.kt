package com.safarsakha.presentation.screens.profile.tours

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TourDetailScreen(
    packageId: String,
    onNavigateBack: () -> Unit
) {
    val repository = remember { TourPackageRepositoryImpl(FirebaseTourPackageDataSource()) }
    val enquiryRepository = remember { EnquiryRepositoryImpl(FirebaseEnquiryDataSource()) }
    val getTourPackageByIdUseCase = remember { GetTourPackageByIdUseCase(repository) }

    val viewModel: TourDetailViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
                @Suppress("UNCHECKED_CAST")
                return TourDetailViewModel(getTourPackageByIdUseCase, enquiryRepository) as T
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
                    Text(
                        text = "Tour Details",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("Back", color = Color(0xFF1E3A8A))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7FB))
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF1E3A8A)
                    )
                }

                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("❌ Error loading tour", fontSize = 18.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(uiState.errorMessage ?: "Unknown error", fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.handleEvent(TourDetailEvent.Retry) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A))
                        ) { Text("Retry") }
                    }
                }

                uiState.tourPackage != null -> {
                    val tourPackage = uiState.tourPackage!!

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Cover image
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .background(Color(0xFFE2E8F0)),
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
                                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(36.dp), color = Color(0xFF1E3A8A))
                                if (isError) Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("⚠️", fontSize = 28.sp)
                                    Text("Image failed to load", color = Color.Red, fontSize = 12.sp)
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("📷", fontSize = 40.sp)
                                    Text("No Image", color = Color(0xFF94A3B8), fontSize = 13.sp)
                                }
                            }
                        }

                        Column(modifier = Modifier.padding(20.dp)) {
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
                                    color = Color(0xFF0F172A),
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF1E3A8A)) {
                                    Text(
                                        text = "₹${tourPackage.price}",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📍", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(tourPackage.location, fontSize = 14.sp, color = Color(0xFF64748B))
                                Spacer(modifier = Modifier.width(20.dp))
                                Text("⏱️", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(tourPackage.duration, fontSize = 14.sp, color = Color(0xFF64748B))
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            HorizontalDivider(color = Color(0xFFE2E8F0))
                            Spacer(modifier = Modifier.height(20.dp))

                            Text("About this tour", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(tourPackage.description, fontSize = 14.sp, color = Color(0xFF475569), lineHeight = 20.sp)

                            Spacer(modifier = Modifier.height(20.dp))
                            HorizontalDivider(color = Color(0xFFE2E8F0))
                            Spacer(modifier = Modifier.height(20.dp))

                            Text("What's Included", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Spacer(modifier = Modifier.height(12.dp))

                            if (tourPackage.includedServices.isEmpty()) {
                                Text("No included services listed for this package.", fontSize = 13.sp, color = Color(0xFF94A3B8))
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    tourPackage.includedServices.forEach { service ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(shape = RoundedCornerShape(50), color = Color(0xFFE0ECFF)) {
                                                Text("✓", color = Color(0xFF1E3A8A), fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(6.dp))
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(service, fontSize = 14.sp, color = Color(0xFF334155))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(28.dp))
                            HorizontalDivider(color = Color(0xFFE2E8F0))
                            Spacer(modifier = Modifier.height(20.dp))

                            // ── SEND ENQUIRY BUTTON ──
                            Button(
                                onClick = { viewModel.handleEvent(TourDetailEvent.OpenEnquiryDialog) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !uiState.isSubmittingEnquiry
                            ) {
                                if (uiState.isSubmittingEnquiry) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    text = if (uiState.isSubmittingEnquiry) "Sending..." else "Send Enquiry",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}