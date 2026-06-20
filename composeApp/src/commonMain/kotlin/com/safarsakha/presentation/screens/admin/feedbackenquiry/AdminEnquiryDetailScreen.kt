package com.safarsakha.presentation.screens.admin.feedbackenquiry

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
import com.safarsakha.domain.model.EnquiryStatus
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEnquiryDetailScreen(
    viewModel: AdminFeedbackViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.detailState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.showSnackbar.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val enquiry = state.enquiry

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text("Enquiry Detail", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) { Text("Back", color = Color(0xFF1E3A8A)) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        if (enquiry == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF1E3A8A))
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7FB))
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Enquiry Info Card
            Card(
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
                        Text("Enquiry Info", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
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

                    Spacer(modifier = Modifier.height(14.dp))

                    InfoRow(label = "User", value = enquiry.userName)
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow(label = "Tour", value = enquiry.tourPackageName)
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow(label = "Date", value = enquiry.createdAt.toString().take(10))

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Enquiry Message", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(enquiry.enquiryMessage, fontSize = 15.sp, color = Color(0xFF0F172A), lineHeight = 22.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Reply Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Admin Reply", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = state.replyText,
                        onValueChange = { viewModel.onReplyTextChange(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        placeholder = {
                            Text("Type your reply here...", fontSize = 13.sp, color = Color(0xFF94A3B8))
                        },
                        maxLines = 6,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1E3A8A),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            cursorColor = Color(0xFF1E3A8A)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.sendReply() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isSending,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (state.isSending) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (state.isSending) "Sending..." else "Send Feedback",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row {
        Text("$label: ", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF64748B))
        Text(value, fontSize = 13.sp, color = Color(0xFF0F172A))
    }
}