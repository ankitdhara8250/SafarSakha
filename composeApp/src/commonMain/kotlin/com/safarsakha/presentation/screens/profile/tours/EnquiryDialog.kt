package com.safarsakha.presentation.screens.profile.tours

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

// ── Premium Design Tokens ────────────────────────────────────────────────────
private val NavyColor = Color(0xFF0F172A)
private val SkyColor = Color(0xFF0EA5E9)
private val SlateColor = Color(0xFF64748B)
private val BorderColor = Color(0xFFE2E8F0)
private val BgColor = Color(0xFFFFFFFF)
private val ErrorColor = Color(0xFFDC2626)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnquiryDialog(
    tourName: String,
    onDismiss: () -> Unit,
    onSend: (message: String) -> Unit
) {
    var message by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BgColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Your Enquiry",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyColor,
                    letterSpacing = (-0.3f).sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = tourName,
                    fontSize = 13.sp,
                    color = SlateColor,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = message,
                    onValueChange = {
                        message = it
                        if (it.isNotBlank()) showError = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    placeholder = {
                        Text(
                            text = "e.g. Is food included? What is the hotel category?",
                            fontSize = 13.sp,
                            color = SlateColor.copy(alpha = 0.6f),
                            lineHeight = 18.sp
                        )
                    },
                    maxLines = 6,
                    isError = showError,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SkyColor,
                        unfocusedBorderColor = BorderColor,
                        cursorColor = SkyColor,
                        errorBorderColor = ErrorColor,
                        focusedTextColor = NavyColor,
                        unfocusedTextColor = NavyColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Bottom Meta Row for Field Metadata & Count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (showError) {
                            Text(
                                text = "Please enter your enquiry message.",
                                color = ErrorColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Text(
                        text = "${message.length} characters",
                        fontSize = 11.sp,
                        color = SlateColor.copy(alpha = 0.8f),
                        textAlign = TextAlign.End,
                        modifier = Modifier.padding(start = 8.0.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Primary Action Button
                Button(
                    onClick = {
                        if (message.isBlank()) {
                            showError = true
                        } else {
                            onSend(message.trim())
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NavyColor,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Send Enquiry",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Secondary Cancel Button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Cancel",
                        color = SlateColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}