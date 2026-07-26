package com.miesport.app.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.miesport.app.data.model.AppNotification
import com.miesport.app.ui.components.GlassCard
import com.miesport.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val notifications by viewModel.notifications.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(BackgroundBlack)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                "Notifications",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                modifier = Modifier.padding(20.dp)
            )

            if (notifications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Koi notification nahi hai", color = TextMuted)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(notifications) { n ->
                        NotificationRow(n, onClick = { viewModel.markRead(n.id) })
                    }
                    item { Spacer(Modifier.height(40.dp)) }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(notification: AppNotification, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        borderColor = if (!notification.read) NeonGreen.copy(alpha = 0.5f) else SurfaceGlassBorder
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(notification.title, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            Text(notification.body, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            Text(
                SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(notification.createdAt)),
                color = TextMuted,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
