package com.miesport.app.ui.screens.tournament

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.miesport.app.data.model.Tournament
import com.miesport.app.ui.components.GlassCard
import com.miesport.app.ui.theme.*

@Composable
fun TournamentListScreen(
    viewModel: TournamentListViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onTournamentClick: (String) -> Unit
) {
    val tournaments by viewModel.filteredTournaments.collectAsState()
    val filterMode by viewModel.filterMode.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(BackgroundBlack)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                "Tournaments",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                modifier = Modifier.padding(20.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(null to "All", "SOLO" to "Solo", "DUO" to "Duo", "SQUAD" to "Squad").forEach { (mode, label) ->
                    val selected = filterMode == mode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selected) NeonGreen else SurfaceElevated)
                            .clickable { viewModel.setFilter(mode) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            label,
                            color = if (selected) BackgroundBlack else TextSecondary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (tournaments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Abhi koi tournament nahi hai", color = TextMuted)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tournaments) { t ->
                        TournamentRow(t, onClick = { onTournamentClick(t.id) })
                    }
                    item { Spacer(Modifier.height(100.dp)) }
                }
            }
        }
    }
}

@Composable
private fun TournamentRow(tournament: Tournament, onClick: () -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        tournament.mode,
                        color = NeonGreen,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(Modifier.width(8.dp))
                    if (tournament.status == "LIVE") {
                        Text("• LIVE", color = DangerRed, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
                Text(tournament.title, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${tournament.filledSlots}/${tournament.totalSlots} slots filled",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    if (tournament.type == "FREE") "FREE" else "Rs.${tournament.entryFee.toInt()}",
                    color = if (tournament.type == "FREE") NeonGreen else GoldPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text("🏆 Rs.${tournament.prizePool.toInt()}", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
