package com.safarsakha.presentation.screens.admin.feedbackenquiry

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.safarsakha.domain.model.Enquiry
import com.safarsakha.domain.model.EnquiryStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEnquiryListScreen(
    viewModel: AdminFeedbackViewModel,
    onEnquiryClick: (Enquiry) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.listState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Enquiry Management", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) { Text("Back", color = Color(0xFF1E3A8A)) }
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
            when (val state = uiState) {
                is AdminEnquiryListUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF1E3A8A))
                }
                is AdminEnquiryListUiState.Empty -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📭", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No Enquiries Yet", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("User enquiries will appear here.", fontSize = 14.sp, color = Color(0xFF64748B))
                    }
                }
                is AdminEnquiryListUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.enquiries, key = { it.enquiryId }) { enquiry ->
                            AdminEnquiryListItem(
                                enquiry = enquiry,
                                onClick = {
                                    viewModel.loadEnquiryDetail(enquiry)
                                    onEnquiryClick(enquiry)
                                }
                            )
                        }
                    }
                }
                is AdminEnquiryListUiState.Error -> {
                    Text(state.message, color = Color.Red, modifier = Modifier.align(Alignment.Center).padding(24.dp))
                }
            }
        }
    }
}

@Composable
private fun AdminEnquiryListItem(enquiry: Enquiry, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(enquiry.userName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                Spacer(modifier = Modifier.height(2.dp))
                Text(enquiry.tourPackageName, fontSize = 13.sp, color = Color(0xFF1E3A8A))
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = enquiry.enquiryMessage.take(80) + if (enquiry.enquiryMessage.length > 80) "..." else "",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(enquiry.createdAt.toString().take(10), fontSize = 11.sp, color = Color(0xFF94A3B8))
            }
            Spacer(modifier = Modifier.width(12.dp))
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
    }
}