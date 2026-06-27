package com.safarsakha.presentation.screens.admin.feedbackenquiry

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safarsakha.domain.model.Enquiry
import com.safarsakha.domain.model.EnquiryStatus

// ── Design tokens ───────────────────────────────────────────────────────────
private val NavyColor = Color(0xFF0F172A)
private val SkyColor = Color(0xFF0EA5E9)
private val SlateColor = Color(0xFF64748B)
private val BorderColor = Color(0xFFE2E8F0)
private val BgColor = Color(0xFFFFFFFF)
private val LightBgColor = Color(0xFFF8FAFC)

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
                    Column {
                        Text(
                            text = "Enquiry Management",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyColor,
                            letterSpacing = (-0.3f).sp
                        )
                        Text(
                            text = "Manage user enquiries",
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBgColor)
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is AdminEnquiryListUiState.Loading -> {
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
                                text = "Loading enquiries...",
                                fontSize = 14.sp,
                                color = SlateColor
                            )
                        }
                    }
                }

                is AdminEnquiryListUiState.Empty -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(BgColor)
                                    .border(1.dp, BorderColor, RoundedCornerShape(50)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "📭",
                                    fontSize = 32.sp
                                )
                            }

                            Text(
                                text = "No Enquiries Yet",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyColor
                            )

                            Text(
                                text = "User enquiries will appear here.",
                                fontSize = 14.sp,
                                color = SlateColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(0.8f)
                            )
                        }
                    }
                }

                is AdminEnquiryListUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.Red.copy(alpha = 0.08f))
                                    .border(1.dp, Color.Red.copy(alpha = 0.15f), RoundedCornerShape(50)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "⚠️",
                                    fontSize = 32.sp
                                )
                            }

                            Text(
                                text = "Failed to Load Enquiries",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyColor
                            )

                            Text(
                                text = state.message,
                                fontSize = 14.sp,
                                color = SlateColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(0.8f)
                            )
                        }
                    }
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
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = BorderColor,
                shape = RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = enquiry.userName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = enquiry.tourPackageName,
                    fontSize = 13.sp,
                    color = SkyColor,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = enquiry.enquiryMessage.take(80) + if (enquiry.enquiryMessage.length > 80) "..." else "",
                    fontSize = 13.sp,
                    color = SlateColor
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = enquiry.createdAt.toString().take(10),
                    fontSize = 11.sp,
                    color = SlateColor.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
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
    }
}