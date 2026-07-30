package com.miesport.app.ui.screens.playersearch

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.miesport.app.ui.components.GlassCard
import com.miesport.app.ui.screens.login.miTextFieldColors
import com.miesport.app.ui.theme.*

@Composable
fun PlayerSearchScreen(
    onBack: () -> Unit,
    onOpenChat: (targetUid: String, targetName: String) -> Unit,
    viewModel: PlayerSearchViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var query by remember { mutableStateOf("") }
    val state by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(BackgroundBlack)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text("Find Players", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
            }

            Row(
                modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("In-game UID") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = miTextFieldColors()
                )
                Spacer(Modifier.width(10.dp))
                IconButton(
                    onClick = { if (query.isNotBlank()) viewModel.searchByUid(query) },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(NeonGreen)
                ) {
                    Icon(Icons.Filled.Search, contentDescription = "Search", tint = BackgroundBlack)
                }
            }

            Spacer(Modifier.height(20.dp))

            when (val s = state) {
                is PlayerSearchUiState.Idle -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("Player ka in-game UID daal kar search karein", color = TextMuted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
                is PlayerSearchUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NeonGreen)
                    }
                }
                is PlayerSearchUiState.NotFound -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("Is UID se koi player nahi mila", color = TextMuted)
                    }
                }
                is PlayerSearchUiState.Error -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text(s.message, color = DangerRed)
                    }
                }
                is PlayerSearchUiState.Found -> {
                    GlassCard(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .fillMaxWidth()
                            .clickable { onOpenChat(s.player.uid, s.player.gamingName) }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (s.player.avatarUrl.isNotBlank()) {
                                AsyncImage(
                                    model = s.player.avatarUrl,
                                    contentDescription = "Avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(52.dp).clip(CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier.size(52.dp).clip(CircleShape).background(SurfaceElevated),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Person, contentDescription = null, tint = NeonGreen)
                                }
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(s.player.gamingName, color = TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("UID: ${s.player.uidGame}", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                                Text("Rank: ${s.player.rank}", color = NeonGreen, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
