package com.miesport.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.miesport.app.ui.components.GlassCard
import com.miesport.app.ui.theme.*

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onSignedOut: () -> Unit
) {
    val user by viewModel.user.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(BackgroundBlack)) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            if (!user?.avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = user?.avatarUrl,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(96.dp).clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier.size(96.dp).clip(CircleShape).background(SurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(48.dp))
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(user?.gamingName ?: "Player", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
            Text("Rank: ${user?.rank ?: "Unranked"}", style = MaterialTheme.typography.bodyMedium, color = NeonGreen)

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("Wins", "${user?.wins ?: 0}", Modifier.weight(1f))
                StatCard("Matches", "${user?.matchesPlayed ?: 0}", Modifier.weight(1f))
                StatCard("Earnings", "Rs.${user?.totalEarnings?.toInt() ?: 0}", Modifier.weight(1f))
            }

            Spacer(Modifier.height(20.dp))

            GlassCard(modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ProfileInfoRow("UID", user?.uidGame ?: "—")
                    ProfileInfoRow("Region", user?.region ?: "—")
                    ProfileInfoRow("Email", user?.email ?: "—")
                }
            }

            if (!user?.badges.isNullOrEmpty()) {
                Spacer(Modifier.height(20.dp))
                Text("Badges", style = MaterialTheme.typography.titleLarge, color = TextPrimary,
                    modifier = Modifier.align(Alignment.Start).padding(start = 20.dp))
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.padding(horizontal = 20.dp)) {
                    user?.badges?.forEach { badge ->
                        Icon(Icons.Filled.EmojiEvents, contentDescription = badge, tint = GoldPrimary, modifier = Modifier.padding(end = 8.dp))
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            OutlinedButton(
                onClick = {
                    viewModel.signOut()
                    onSignedOut()
                },
                modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Filled.Logout, contentDescription = null, tint = DangerRed)
                Spacer(Modifier.width(8.dp))
                Text("Sign Out", color = DangerRed, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = NeonGreen, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, color = TextMuted, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextMuted, style = MaterialTheme.typography.bodyMedium)
        Text(value, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
    }
}
