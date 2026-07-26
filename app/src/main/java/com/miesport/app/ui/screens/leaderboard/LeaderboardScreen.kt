package com.miesport.app.ui.screens.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.miesport.app.data.model.LeaderboardEntry
import com.miesport.app.ui.components.GlassCard
import com.miesport.app.ui.theme.*

@Composable
fun LeaderboardScreen(
    viewModel: LeaderboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var period by remember { mutableStateOf("season") }
    val entries by viewModel.entries.collectAsState()

    androidx.compose.runtime.LaunchedEffect(period) { viewModel.setPeriod(period) }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundBlack)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                "Leaderboard",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                modifier = Modifier.padding(20.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf("month" to "Monthly", "season" to "Season").forEach { (key, label) ->
                    val selected = period == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selected) NeonGreen else SurfaceElevated)
                            .clickable { period = key }
                            .padding(horizontal = 18.dp, vertical = 8.dp)
                    ) {
                        Text(label, color = if (selected) BackgroundBlack else TextSecondary, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (entries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Rankings jald update hongi", color = TextMuted)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(entries) { index, entry ->
                        LeaderboardRow(index + 1, entry)
                    }
                    item { Spacer(Modifier.height(100.dp)) }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardRow(position: Int, entry: LeaderboardEntry) {
    val medalColor = when (position) {
        1 -> GoldPrimary
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> TextMuted
    }
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(SurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Text("$position", color = medalColor, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.gamingName, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                if (entry.teamName.isNotBlank()) {
                    Text(entry.teamName, color = TextMuted, style = MaterialTheme.typography.labelSmall)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${entry.points} pts", color = NeonGreen, fontWeight = FontWeight.Bold)
                Text("${entry.wins} wins", color = TextMuted, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
