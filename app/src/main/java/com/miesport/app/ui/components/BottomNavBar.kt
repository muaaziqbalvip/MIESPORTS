package com.miesport.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.miesport.app.navigation.BottomNavItem
import com.miesport.app.ui.theme.NeonGreen
import com.miesport.app.ui.theme.SurfaceGlass
import com.miesport.app.ui.theme.SurfaceGlassBorder
import com.miesport.app.ui.theme.TextMuted

private fun iconFor(item: BottomNavItem) = when (item) {
    BottomNavItem.HOME -> Icons.Filled.Home
    BottomNavItem.TOURNAMENT -> Icons.Filled.EmojiEvents
    BottomNavItem.LIVE -> Icons.Filled.PlayCircle
    BottomNavItem.WALLET -> Icons.Filled.AccountBalanceWallet
    BottomNavItem.PROFILE -> Icons.Filled.Person
}

@Composable
fun MiBottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    listOf(SurfaceGlass, SurfaceGlass.copy(alpha = 0.06f))
                )
            )
            .background(androidx.compose.ui.graphics.Color(0xCC0D1215), RoundedCornerShape(28.dp))
            .border(1.dp, SurfaceGlassBorder, RoundedCornerShape(28.dp))
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem.values().forEach { item ->
            val selected = currentRoute == item.route
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onNavigate(item.route) }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = iconFor(item),
                    contentDescription = item.label,
                    tint = if (selected) NeonGreen else TextMuted,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) NeonGreen else TextMuted
                )
            }
        }
    }
}
