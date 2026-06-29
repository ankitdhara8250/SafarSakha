package com.safarsakha.presentation.screens.profile.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
private val WarningColor = Color(0xFFD97706)
private val WarningBgColor = Color(0xFFFFFBEB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(onMenuClick: () -> Unit) {
    val enquiryRepository = remember { EnquiryRepositoryImpl(FirebaseEnquiryDataSource()) }
    val currentUserId = remember { Firebase.auth.currentUser?.uid ?: "" }

    // FIX: Encapsulated inside remember {} to guarantee factory references remain stable
    // across target architectural recompositions, preserving active flow listener scopes.
    val viewModel: FeedbackViewModel = viewModel(
        factory = remember(enquiryRepository, currentUserId) {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
                    @Suppress("UNCHECKED_CAST")
                    return FeedbackViewModel(enquiryRepository, currentUserId) as T
                }
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Feedback",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyColor,
                            letterSpacing = (-0.3f).sp
                        )
                        Text(
                            text = "Track your enquiries and support tickets",
                            fontSize = 12.sp,
                            color = SlateColor
                        )
                    }
                },
                navigationIcon = { HamburgerMenuButton(onClick = onMenuClick) },
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
            when (val state = uiState) {
                is FeedbackUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = SkyColor,
                        strokeWidth = 3.dp
                    )
                }

                is FeedbackUiState.Empty -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(50))
                                .background(SkyColor.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💬", fontSize = 28.sp)
                        }
                        Text(
                            text = "No Enquiries Yet",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NavyColor
                        )
                        Text(
                            text = "Your enquiries and admin replies will appear here.",
                            fontSize = 13.sp,
                            color = SlateColor.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                is FeedbackUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.enquiries, key = { it.enquiryId }) { enquiry ->
                            FeedbackCard(enquiry = enquiry)
                        }
                    }
                }

                is FeedbackUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
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
                            text = state.message,
                            fontSize = 14.sp,
                            color = SlateColor,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// PREMIUM FEEDBACK CARD
// =============================================================================

@Composable
private fun FeedbackCard(enquiry: Enquiry) {
    var expanded by remember { mutableStateOf(false) }

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
        colors = CardDefaults.cardColors(containerColor = BgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = BorderColor.copy(alpha = 0.60f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = enquiry.tourPackageName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyColor,
                    letterSpacing = (-0.2f).sp,
                    modifier = Modifier.weight(1f)
                )

                val (statusText, bg, fg) = when (enquiry.enquiryStatus) {
                    EnquiryStatus.REPLIED -> Triple("Replied", SuccessBgColor, SuccessColor)
                    else -> Triple("Pending", WarningBgColor, WarningColor)
                }

                Surface(shape = RoundedCornerShape(8.dp), color = bg) {
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = fg,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Your Enquiry",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateColor,
                    letterSpacing = 0.2.sp
                )
                Text(
                    text = enquiry.enquiryMessage,
                    fontSize = 14.sp,
                    color = NavyColor,
                    lineHeight = 20.sp
                )
            }

            if (enquiry.adminReply != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(LightBgColor)
                        .border(1.dp, BorderColor.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Admin Reply",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SkyColor,
                            letterSpacing = 0.2.sp
                        )
                        Text(
                            text = if (expanded) enquiry.adminReply else enquiry.adminReply.take(100) + if (enquiry.adminReply.length > 100) "..." else "",
                            fontSize = 14.sp,
                            color = NavyColor,
                            lineHeight = 20.sp
                        )
                        if (enquiry.adminReply.length > 100) {
                            TextButton(
                                onClick = { expanded = !expanded },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(
                                    text = if (expanded) "Show less" else "Read more",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SkyColor
                                )
                            }
                        }
                        enquiry.repliedAt?.let { replied ->
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Replied: ${replied.toString().take(10)}",
                                fontSize = 11.sp,
                                color = SlateColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Sent: ${enquiry.createdAt.toString().take(10)}",
                    fontSize = 11.sp,
                    color = SlateColor.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}