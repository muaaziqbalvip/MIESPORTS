package com.miesport.app.ui.screens.rewards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.miesport.app.ui.components.GlassCard
import com.miesport.app.ui.theme.*

@Composable
fun RewardsScreen(
    onClaimDaily: () -> Unit = {},
    onShareReferral: () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize().background(BackgroundBlack)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Text(
                "Rewards",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                modifier = Modifier.padding(20.dp)
            )

            RewardCard(
                icon = Icons.Filled.CardGiftcard,
                title = "Daily Reward",
                subtitle = "Roz login karke bonus coins hasil karein",
                actionLabel = "Claim",
                onAction = onClaimDaily
            )
            RewardCard(
                icon = Icons.Filled.Share,
                title = "Referral Reward",
                subtitle = "Dost ko invite karein aur bonus paayein",
                actionLabel = "Share",
                onAction = onShareReferral
            )
            RewardCard(
                icon = Icons.Filled.Casino,
                title = "Lucky Draw",
                subtitle = "Har hafte lucky draw mein hissa lein",
                actionLabel = "View",
                onAction = {}
            )

            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
private fun RewardCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    GlassCard(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp).fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, color = TextMuted, style = MaterialTheme.typography.bodyMedium)
            }
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(actionLabel, color = BackgroundBlack, fontWeight = FontWeight.Bold)
            }
        }
    }
}
