package com.miesport.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.miesport.app.data.model.Tournament
import com.miesport.app.ui.components.GlassCard
import com.miesport.app.ui.components.NeonGlow
import com.miesport.app.ui.components.PulsingDot
import com.miesport.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onTournamentClick: (String) -> Unit
) {
    val tournaments by viewModel.tournaments.collectAsState()
    val liveTournaments = tournaments.filter { it.status == "LIVE" }
    val upcoming = tournaments.filter { it.status == "UPCOMING" }.take(1)
    val featured = tournaments.take(6)

    Box(modifier = Modifier.fillMaxSize().background(BackgroundBlack)) {
        NeonGlow(modifier = Modifier.align(Alignment.TopCenter))

        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Assalam-o-Alaikum 👋", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Text("MI ESPORT", style = MaterialTheme.typography.headlineLarge, color = NeonGreen)
                }
            }

            // Hero banner - upcoming tournament
            upcoming.firstOrNull()?.let { t ->
                item { HeroBanner(t, onClick = { onTournamentClick(t.id) }) }
            }

            if (liveTournaments.isNotEmpty()) {
                item { SectionTitle("🔴 Live Tournaments") }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(liveTournaments) { t ->
                            TournamentCard(t, onClick = { onTournamentClick(t.id) })
                        }
                    }
                }
            }

            item { SectionTitle("Featured Events") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(featured) { t ->
                        TournamentCard(t, onClick = { onTournamentClick(t.id) })
                    }
                }
            }

            item { SectionTitle("🏆 Latest Winners") }
            item {
                GlassCard(modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = GoldPrimary)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Winners jald hi update honge", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                            Text("Tournament complete hone ke baad yahan dikhega", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleLarge,
        color = TextPrimary,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    )
}

@Composable
private fun HeroBanner(tournament: Tournament, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(listOf(GradientGreenStart, GradientDarkEnd))
            )
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.align(Alignment.BottomStart)) {
            Text("UPCOMING TOURNAMENT", color = BackgroundBlack, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text(tournament.title, color = BackgroundBlack, style = MaterialTheme.typography.headlineMedium)
            Text(
                "Prize Pool: Rs. ${tournament.prizePool.toInt()}",
                color = BackgroundBlack,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TournamentCard(tournament: Tournament, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier
            .width(220.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (tournament.status == "LIVE") {
                    PulsingDot()
                    Spacer(Modifier.width(6.dp))
                    Text("LIVE", color = DangerRed, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                } else {
                    Text(tournament.mode, color = NeonGreen, style = MaterialTheme.typography.labelSmall)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(tournament.title, color = TextPrimary, style = MaterialTheme.typography.titleMedium, maxLines = 1)
            Spacer(Modifier.height(4.dp))
            Text(
                "${tournament.filledSlots}/${tournament.totalSlots} slots",
                color = TextMuted,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    if (tournament.type == "FREE") "FREE" else "Rs. ${tournament.entryFee.toInt()}",
                    color = if (tournament.type == "FREE") NeonGreen else GoldPrimary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "🏆 ${tournament.prizePool.toInt()}",
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
