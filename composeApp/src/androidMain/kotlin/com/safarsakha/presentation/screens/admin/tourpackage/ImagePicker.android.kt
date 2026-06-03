package com.safarsakha.presentation.screens.admin.tourpackage

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun ImagePicker(
    show: Boolean,
    onImagePicked: (ByteArray) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                // Read the image bytes from the selected gallery URI
                val bytes = context.contentResolver.openInputStream(it)?.readBytes()
                if (bytes != null) {
                    onImagePicked(bytes)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        onDismiss()
    }

    LaunchedEffect(show) {
        if (show) {
            launcher.launch("image/*")
        }
    }
}
