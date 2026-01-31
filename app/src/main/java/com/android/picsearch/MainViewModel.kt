package com.android.picsearch

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.picsearch.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder

sealed class UiState {
    data object Idle : UiState()
    data object Loading : UiState()
    data class Success(val url: String) : UiState()
    data class Error(val message: String) : UiState()
}

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun handleIntent(intent: Intent?, contentResolver: ContentResolver) {
        if (intent?.action != Intent.ACTION_SEND) return

        when {
            // 處理純文字/網址分享
            intent.type == "text/plain" -> {
                val textUrl = intent.getStringExtra(Intent.EXTRA_TEXT)
                if (!textUrl.isNullOrBlank()) {
                    val finalUrl = "https://lens.google.com/uploadbyurl?url=${
                        URLEncoder.encode(
                            textUrl,
                            "UTF-8"
                        )
                    }"
                    _uiState.value = UiState.Success(finalUrl)
                } else {
                    _uiState.value = UiState.Error("Invalid text content")
                }
            }
            // 處理圖片分享
            intent.type?.startsWith("image/") == true -> {
                val uri = intent.getParcelableExtra(Intent.EXTRA_STREAM) as? Uri
                if (uri != null) {
                    uploadImage(uri, contentResolver)
                } else {
                    _uiState.value = UiState.Error("Failed to get image URI")
                }
            }

            else -> {
                _uiState.value = UiState.Error("Unsupported content type")
            }
        }
    }

    private fun uploadImage(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    val fileBytes = inputStream.readBytes()
                    val requestBody = fileBytes.toRequestBody(
                        contentResolver.getType(uri)?.toMediaTypeOrNull()
                    )

                    val multipartBody = MultipartBody.Part.createFormData(
                        "file",
                        getFileName(uri, contentResolver),
                        requestBody
                    )

                    val response = RetrofitInstance.api.uploadFile(multipartBody)

                    if (response.status == "success") {
                        val finalUrl = "https://lens.google.com/uploadbyurl?url=${
                            response.data.url
                                .replace("tmpfiles.org/", "tmpfiles.org/dl/")
                                .replaceFirst("http://", "https://")
                        }"

                        _uiState.value = UiState.Success(finalUrl)
                    } else {
                        _uiState.value = UiState.Error("Upload failed")
                    }
                } ?: run {
                    _uiState.value = UiState.Error("Cannot read file")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error: ${e.message}")
            }
        }
    }

    private fun getFileName(uri: Uri, contentResolver: ContentResolver): String {
        return contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            } ?: "uploaded_image"
    }
}