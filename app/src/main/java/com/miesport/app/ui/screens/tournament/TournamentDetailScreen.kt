package com.miesport.app.ui.screens.tournament

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.miesport.app.data.model.Tournament
import com.miesport.app.ui.components.GlassCard
import com.miesport.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TournamentDetailScreen(
    tournamentId: String,
    onRegisterClick: () -> Unit,
    onBack: () -> Unit
) {
    val viewModel: TournamentDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                TournamentDetailViewModel(tournamentId) as T
        }
    )

    val tournament by viewModel.tournament.collectAsState()
    val roomReveal by viewModel.roomReveal.collectAsState()
    val loadError by viewModel.loadError.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(BackgroundBlack)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text("Tournament Details", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
            }

            val t = tournament
            when {
                t != null -> TournamentDetailContent(
                    tournament = t,
                    roomReveal = roomReveal,
                    onRegisterClick = onRegisterClick
                )
                loadError != null -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text(loadError ?: "Kuch masla hua", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                else -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NeonGreen)
                    }
                }
            }
        }
    }
}

@Composable
private fun TournamentDetailContent(
    tournament: Tournament,
    roomReveal: Map<String, String>?,
    onRegisterClick: () -> Unit
) {
    val t = tournament
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(t.title, style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
        Text(t.game, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatChip("Mode", t.mode)
            StatChip("Slots", "${t.filledSlots}/${t.totalSlots}")
            StatChip("Entry", if (t.type == "FREE") "FREE" else "Rs.${t.entryFee.toInt()}")
        }

        Spacer(Modifier.height(16.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Prize Pool", color = TextSecondary, style = MaterialTheme.typography.labelLarge)
                Text(
                    "Rs. ${t.prizePool.toInt()}",
                    color = GoldPrimary,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                if (t.prizeBreakdown.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    t.prizeBreakdown.forEach { (place, amount) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(place, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                            Text("Rs. ${amount.toInt()}", color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Match Date & Time", color = TextSecondary, style = MaterialTheme.typography.labelLarge)
                Text(
                    if (t.matchDateTime > 0)
                        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(t.matchDateTime))
                    else "TBA",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Room ID / Password - only visible once admin publishes to RTDB
        GlassCard(modifier = Modifier.fillMaxWidth(), borderColor = NeonGreen.copy(alpha = 0.4f)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Room Details", color = NeonGreen, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                if (roomReveal != null) {
                    Spacer(Modifier.height(8.dp))
                    Text("Room ID: ${roomReveal["roomId"] ?: "-"}", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                    Text("Password: ${roomReveal["roomPassword"] ?: "-"}", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                } else {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Match se thodi der pehle yahan reveal hoga",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        if (t.rules.isNotBlank()) {
            Spacer(Modifier.height(16.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Rules", color = TextSecondary, style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(4.dp))
                    Text(t.rules, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onRegisterClick,
            enabled = t.filledSlots < t.totalSlots && t.status != "COMPLETED" && t.status != "CANCELLED",
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, disabledContainerColor = SurfaceElevated)
        ) {
            Text(
                when {
                    t.filledSlots >= t.totalSlots -> "Slots Full"
                    t.status == "COMPLETED" -> "Tournament Ended"
                    t.status == "CANCELLED" -> "Cancelled"
                    else -> "Register Now"
                },
                color = BackgroundBlack,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun StatChip(label: String, value: String) {
    GlassCard {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(label, color = TextMuted, style = MaterialTheme.typography.labelSmall)
        }
    }
}
