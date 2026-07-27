package com.miesport.app.ui.screens.live

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.miesport.app.ui.components.GlassCard
import com.miesport.app.ui.components.PulsingDot
import com.miesport.app.ui.theme.*

@Composable
fun LiveScreen(
    viewModel: LiveViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val liveTournaments by viewModel.liveTournaments.collectAsState()
    val liveStatuses by viewModel.liveStatuses.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(BackgroundBlack)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Text(
                "Live",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                modifier = Modifier.padding(20.dp)
            )

            if (liveTournaments.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔴", style = MaterialTheme.typography.displayLarge)
                        Spacer(Modifier.height(12.dp))
                        Text("Abhi koi tournament live nahi hai", color = TextMuted)
                    }
                }
            } else {
                liveTournaments.forEach { tournament ->
                    val videoId = liveStatuses[tournament.id]?.youtubeVideoId
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PulsingDot()
                            Spacer(Modifier.width(6.dp))
                            Text("LIVE NOW", color = DangerRed, style = MaterialTheme.typography.labelLarge)
                        }
                        Text(tournament.title, color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(8.dp))

                        if (!videoId.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(16.dp))
                            ) {
                                YouTubeEmbed(videoId)
                            }
                        } else {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                                    Text("Stream jald shuru hogi", color = TextMuted)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(100.dp))
        }
    }
}

/**
 * Lightweight YouTube embed using WebView + YouTube's iframe embed API.
 * Avoids any third-party player library and its dependency-resolution issues,
 * while still supporting autoplay and standard playback controls.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun YouTubeEmbed(videoId: String) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                webViewClient = WebViewClient()
                val embedHtml = """
                    <html>
                      <body style="margin:0;padding:0;background:#000;">
                        <iframe
                          width="100%"
                          height="100%"
                          src="https://www.youtube.com/embed/$videoId?autoplay=1&playsinline=1"
                          frameborder="0"
                          allow="accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture"
                          allowfullscreen>
                        </iframe>
                      </body>
                    </html>
                """.trimIndent()
                loadDataWithBaseURL(
                    "https://www.youtube.com",
                    embedHtml,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        },
        modifier = Modifier.fillMaxWidth().height(220.dp)
    )
}
