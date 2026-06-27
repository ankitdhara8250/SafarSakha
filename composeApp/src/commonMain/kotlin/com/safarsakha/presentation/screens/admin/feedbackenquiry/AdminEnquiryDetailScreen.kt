package com.safarsakha.presentation.screens.admin.feedbackenquiry

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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

// ── Design tokens ───────────────────────────────────────────────────────────
private val NavyColor = Color(0xFF0F172A)
private val SkyColor = Color(0xFF0EA5E9)
private val SlateColor = Color(0xFF64748B)
private val BorderColor = Color(0xFFE2E8F0)
private val BgColor = Color(0xFFFFFFFF)
private val LightBgColor = Color(0xFFF8FAFC)

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
        },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Enquiry Detail",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyColor,
                            letterSpacing = (-0.3f).sp
                        )
                        Text(
                            text = "View and reply to enquiry",
                            fontSize = 12.sp,
                            color = SlateColor
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
        }
    ) { paddingValues ->
        if (enquiry == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightBgColor)
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = SkyColor,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading detail...", fontSize = 14.sp, color = SlateColor)
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBgColor)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Enquiry Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderColor, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = BgColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Enquiry Info",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyColor
                        )
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (enquiry.enquiryStatus == EnquiryStatus.REPLIED)
                                Color(0xFFDCFCE7)
                            else
                                Color(0xFFFEF9C3)
                        ) {
                            Text(
                                text = enquiry.enquiryStatus.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (enquiry.enquiryStatus == EnquiryStatus.REPLIED)
                                    Color(0xFF15803D)
                                else
                                    Color(0xFF92400E),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    InfoRow(label = "User", value = enquiry.userName)
                    Spacer(modifier = Modifier.height(10.dp))
                    InfoRow(label = "Tour", value = enquiry.tourPackageName, isAccent = true)
                    Spacer(modifier = Modifier.height(10.dp))
                    InfoRow(label = "Date", value = enquiry.createdAt.toString().take(10))

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = BorderColor)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Enquiry Message",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = SlateColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = enquiry.enquiryMessage,
                        fontSize = 15.sp,
                        color = NavyColor,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Reply Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderColor, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = BgColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Admin Reply",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = state.replyText,
                        onValueChange = { viewModel.onReplyTextChange(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        placeholder = {
                            Text("Type your reply here...", fontSize = 14.sp, color = SlateColor)
                        },
                        maxLines = 6,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NavyColor,
                            unfocusedBorderColor = BorderColor,
                            cursorColor = NavyColor,
                            focusedTextColor = NavyColor,
                            unfocusedTextColor = NavyColor
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.sendReply() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = !state.isSending,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NavyColor,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (state.isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        Text(
                            text = if (state.isSending) "Sending..." else "Send Feedback",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, isAccent: Boolean = false) {
    Row {
        Text(
            text = "$label: ",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = SlateColor
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = if (isAccent) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isAccent) SkyColor else NavyColor
        )
    }
}