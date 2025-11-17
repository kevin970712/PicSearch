package com.android.picsearch

import android.content.res.Configuration
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.android.picsearch.ui.theme.PicSearchTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Enable Edge-to-Edge display
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

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
                    MainScreen(uiState = uiState)
                }
            }
        }
    }
}

@Composable
fun MainScreen(uiState: UiState) {
    Box(
        // Apply padding for system bars to ensure content is not obscured.
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.safeDrawing.asPaddingValues()),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is UiState.Idle -> IdleScreen()
            is UiState.Loading -> CircularProgressIndicator()
            is UiState.Success -> WebViewScreen(url = uiState.url)
            is UiState.Error -> Text(text = "Error: ${uiState.message}")
        }
    }
}

@Composable
fun IdleScreen() {
    val configuration = LocalConfiguration.current

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

        when (configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                // Use a Row for landscape orientation.
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InstructionBlock(
                        modifier = Modifier.weight(1f),
                        drawableId = R.drawable.ic_share_image,
                        text = "Find an image in another app,\n tap \"Share\" and select PicSearch."
                    )
                    InstructionBlock(
                        modifier = Modifier.weight(1f),
                        drawableId = R.drawable.ic_share_link,
                        text = "Or, share an image URL\n to PicSearch."
                    )
                }
            }
            else -> {
                // Use a Column for portrait orientation.
                InstructionBlock(
                    drawableId = R.drawable.ic_share_image,
                    text = "Find an image in another app,\n tap \"Share\" and select PicSearch."
                )
                Spacer(modifier = Modifier.height(24.dp))
                InstructionBlock(
                    drawableId = R.drawable.ic_share_link,
                    text = "Or, share an image URL\n to PicSearch."
                )
            }
        }
    }
}

@Composable
private fun InstructionBlock(
    modifier: Modifier = Modifier,
    drawableId: Int,
    text: String
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = drawableId),
            contentDescription = null, // Decorative image
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


@Composable
fun WebViewScreen(url: String) {
    val backgroundColor = MaterialTheme.colorScheme.background
    var webView: WebView? by remember { mutableStateOf(null) }
    var canGoBack: Boolean by remember { mutableStateOf(false) }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                // Set background color to prevent white flicker before page loads.
                setBackgroundColor(backgroundColor.toArgb())

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String?) {
                        super.onPageFinished(view, url)
                        canGoBack = view.canGoBack()
                    }
                }

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Mobile Safari/537.36"
                }

                loadUrl(url)
                webView = this
            }
        },
        update = {
            it.loadUrl(url)
        }
    )

    // Handle back button press to navigate back in WebView history.
    BackHandler(enabled = canGoBack) {
        webView?.goBack()
    }
}