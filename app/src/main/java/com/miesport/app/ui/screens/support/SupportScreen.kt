package com.miesport.app.ui.screens.support

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.miesport.app.data.model.PaymentMethod
import com.miesport.app.ui.components.GlassCard
import com.miesport.app.ui.theme.*

@Composable
fun SupportScreen(
    viewModel: SupportViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onOpenSupportChat: () -> Unit
) {
    val paymentMethods by viewModel.paymentMethods.collectAsState()
    val clipboard = LocalClipboardManager.current

    Box(modifier = Modifier.fillMaxSize().background(BackgroundBlack)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Text(
                "Help & Support",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                modifier = Modifier.padding(20.dp)
            )

            // Support chat entry
            GlassCard(modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .then(Modifier),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.ChatBubble, contentDescription = null, tint = NeonGreen)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Chat with Support", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                        Text("Payment ya kisi bhi masle ke liye", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                    }
                    Button(
                        onClick = onOpenSupportChat,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Open", color = BackgroundBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text(
                "Payment Methods",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                modifier = Modifier.padding(20.dp)
            )

            if (paymentMethods.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                    Text("Payment methods jald add honge", color = TextMuted)
                }
            } else {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    paymentMethods.forEach { method ->
                        PaymentMethodCard(method, onCopyAccount = {
                            clipboard.setText(AnnotatedString(method.accountNumber))
                        })
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            GlassCard(modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Payment ke baad kya karein?", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "QR code scan karke payment karein, phir uska screenshot registration form mein upload kar dein. Admin verify karne ke baad aapki entry confirm ho jayegi.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
private fun PaymentMethodCard(method: PaymentMethod, onCopyAccount: () -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(method.name, color = NeonGreen, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            if (method.qrCodeUrl.isNotBlank()) {
                AsyncImage(
                    model = method.qrCodeUrl,
                    contentDescription = "${method.name} QR Code",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(TextPrimary)
                )
                Spacer(Modifier.height(10.dp))
            }

            if (method.accountTitle.isNotBlank()) {
                Text("Account Title: ${method.accountTitle}", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            }
            if (method.accountNumber.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Account: ${method.accountNumber}", color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                    IconButton(onClick = onCopyAccount, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }
            if (method.instructions.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(method.instructions, color = TextMuted, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
