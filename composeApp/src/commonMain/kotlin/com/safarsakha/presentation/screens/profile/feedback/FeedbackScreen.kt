package com.safarsakha.presentation.screens.profile.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.safarsakha.data.remote.firebase.FirebaseEnquiryDataSource
import com.safarsakha.data.repository.impl.EnquiryRepositoryImpl
import com.safarsakha.domain.model.Enquiry
import com.safarsakha.domain.model.EnquiryStatus
import com.safarsakha.presentation.screens.profile.profiledashboard.components.HamburgerMenuButton
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlin.reflect.KClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(onMenuClick: () -> Unit) {
    val enquiryRepository = remember { EnquiryRepositoryImpl(FirebaseEnquiryDataSource()) }
    val currentUserId = remember { Firebase.auth.currentUser?.uid ?: "" }

    val viewModel: FeedbackViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
                @Suppress("UNCHECKED_CAST")
                return FeedbackViewModel(enquiryRepository, currentUserId) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Feedback", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                },
                navigationIcon = { HamburgerMenuButton(onClick = onMenuClick) },
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
            when (val state = uiState) {
                is FeedbackUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF1E3A8A))
                }

                is FeedbackUiState.Empty -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("💬", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No Enquiries Yet", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Your enquiries and admin replies will appear here.", fontSize = 14.sp, color = Color(0xFF64748B))
                    }
                }

                is FeedbackUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.enquiries, key = { it.enquiryId }) { enquiry ->
                            FeedbackCard(enquiry = enquiry)
                        }
                    }
                }

                is FeedbackUiState.Error -> {
                    Text(
                        text = state.message,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center).padding(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedbackCard(enquiry: Enquiry) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = enquiry.tourPackageName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (enquiry.enquiryStatus == EnquiryStatus.REPLIED) Color(0xFFDCFCE7) else Color(0xFFFEF9C3)
                ) {
                    Text(
                        text = enquiry.enquiryStatus.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (enquiry.enquiryStatus == EnquiryStatus.REPLIED) Color(0xFF15803D) else Color(0xFF92400E),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text("Your Enquiry", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF64748B))
            Spacer(modifier = Modifier.height(4.dp))
            Text(enquiry.enquiryMessage, fontSize = 14.sp, color = Color(0xFF334155))

            if (enquiry.adminReply != null) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFE2E8F0))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Admin Reply", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E3A8A))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (expanded) enquiry.adminReply else enquiry.adminReply.take(100) + if (enquiry.adminReply.length > 100) "..." else "",
                    fontSize = 14.sp,
                    color = Color(0xFF334155)
                )
                if (enquiry.adminReply.length > 100) {
                    TextButton(
                        onClick = { expanded = !expanded },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(if (expanded) "Show less" else "Read more", fontSize = 12.sp, color = Color(0xFF1E3A8A))
                    }
                }
                enquiry.repliedAt?.let { replied ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Replied: ${replied.toString().take(10)}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sent: ${enquiry.createdAt.toString().take(10)}",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}