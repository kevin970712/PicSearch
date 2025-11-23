package com.android.picsearch

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.picsearch.ui.theme.PicSearchTheme
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null && intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val textUrl = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!textUrl.isNullOrBlank()) {
                val encodedUrl = URLEncoder.encode(textUrl, StandardCharsets.UTF_8.toString())
                val finalUrl = "https://lens.google.com/uploadbyurl?url=$encodedUrl"

                launchCustomTab(this, finalUrl)
                finish()
                overridePendingTransition(0, 0)
                return
            }
        }

        if (savedInstanceState == null) {
            viewModel.handleIntent(intent, contentResolver)
        }

        setContent {
            PicSearchTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val uiState by viewModel.uiState.collectAsState()
                    MainScreen(
                        uiState = uiState,
                        onFinishApp = {
                            finish()
                            overridePendingTransition(0, 0)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    uiState: UiState,
    onFinishApp: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.safeDrawing.asPaddingValues()),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is UiState.Idle -> IdleScreen()
            is UiState.Error -> Text(text = "Error: ${uiState.message}")
            is UiState.Loading, is UiState.Success -> {
                CircularProgressIndicator()

                if (uiState is UiState.Success) {
                    LaunchedEffect(uiState.url) {
                        launchCustomTab(context, uiState.url)
                        onFinishApp()
                    }
                }
            }
        }
    }
}

fun launchCustomTab(context: Context, url: String) {
    try {
        CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(url))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun IdleScreen() {
    val configuration = LocalConfiguration.current

    // 共用內容區塊
    val imageBlock = @Composable { modifier: Modifier ->
        InstructionBlock(
            modifier = modifier,
            drawableId = R.drawable.ic_share_image,
            text = "Find an image in another app,\n tap \"Share\" and select PicSearch."
        )
    }
    val linkBlock = @Composable { modifier: Modifier ->
        InstructionBlock(
            modifier = modifier,
            drawableId = R.drawable.ic_share_link,
            text = "Or, share an image URL\n to PicSearch."
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "How to use PicSearch?",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                imageBlock(Modifier.weight(1f))
                linkBlock(Modifier.weight(1f))
            }
        } else {
            imageBlock(Modifier)
            Spacer(modifier = Modifier.height(24.dp))
            linkBlock(Modifier)
        }
    }
}

@Composable
private fun InstructionContent() {

    InstructionBlock(
        drawableId = R.drawable.ic_share_image,
        text = "Find an image in another app,\n tap \"Share\" and select PicSearch."
    )

}

@Composable
private fun InstructionBlock(
    modifier: Modifier = Modifier,
    drawableId: Int,
    text: String
) {
    Column(
        modifier = modifier, // 允許外部傳入 weight
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = drawableId),
            contentDescription = null,
            modifier = Modifier.size(240.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}