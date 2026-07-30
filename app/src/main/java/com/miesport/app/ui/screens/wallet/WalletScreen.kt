package com.miesport.app.ui.screens.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.miesport.app.data.model.WalletTransaction
import com.miesport.app.ui.components.GlassCard
import com.miesport.app.ui.components.NeonGlow
import com.miesport.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WalletScreen(
    viewModel: WalletViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val user by viewModel.user.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    var showDepositDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundBlack)) {
        NeonGlow(modifier = Modifier.align(Alignment.TopEnd))

        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                "Wallet",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                modifier = Modifier.padding(20.dp)
            )

            GlassCard(modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Balance", color = TextSecondary, style = MaterialTheme.typography.labelLarge)
                    Text(
                        "Rs. ${user?.walletBalance?.toInt() ?: 0}",
                        color = NeonGreen,
                        style = MaterialTheme.typography.displayLarge
                    )
                    if ((user?.bonusCoins ?: 0) > 0) {
                        Text("+ ${user?.bonusCoins} Bonus Coins", color = GoldPrimary, style = MaterialTheme.typography.bodyMedium)
                    }

                    Spacer(Modifier.height(20.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { showDepositDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Deposit", color = BackgroundBlack, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { showWithdrawDialog = true },
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceGlassBorder)
                        ) {
                            Text("Withdraw", color = TextPrimary)
                        }
                    }
                }
            }

            Text(
                "Transaction History",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                modifier = Modifier.padding(20.dp)
            )

            if (transactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                    Text("Koi transaction nahi hai abhi tak", color = TextMuted)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(transactions) { tx -> TransactionRow(tx) }
                    item { Spacer(Modifier.height(100.dp)) }
                }
            }
        }

        if (showDepositDialog) {
            TransactionDialog(
                title = "Deposit Funds",
                confirmLabel = "Request Deposit",
                isWithdraw = false,
                onDismiss = { showDepositDialog = false },
                onConfirm = { amount, method, _, _ ->
                    viewModel.requestDeposit(amount, method)
                    showDepositDialog = false
                }
            )
        }
        if (showWithdrawDialog) {
            TransactionDialog(
                title = "Withdraw Funds",
                confirmLabel = "Request Withdraw",
                isWithdraw = true,
                onDismiss = { showWithdrawDialog = false },
                onConfirm = { amount, method, accountTitle, accountNumber ->
                    viewModel.requestWithdraw(amount, method, accountTitle, accountNumber)
                    showWithdrawDialog = false
                }
            )
        }
    }
}

@Composable
private fun TransactionRow(tx: WalletTransaction) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(tx.type, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                Text(
                    SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(tx.createdAt)),
                    color = TextMuted,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                val isCredit = tx.type == "DEPOSIT" || tx.type == "PRIZE" || tx.type == "BONUS"
                Text(
                    "${if (isCredit) "+" else "-"}Rs. ${tx.amount.toInt()}",
                    color = if (isCredit) NeonGreen else DangerRed,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    tx.status,
                    color = when (tx.status) {
                        "APPROVED" -> NeonGreen
                        "REJECTED" -> DangerRed
                        else -> WarningAmber
                    },
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun TransactionDialog(
    title: String,
    confirmLabel: String,
    isWithdraw: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Double, String, String, String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("JazzCash") }
    var accountTitle by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = { Text(title, color = TextPrimary) },
        text = {
            Column {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() } },
                    label = { Text("Amount (Rs.)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = com.miesport.app.ui.screens.login.miTextFieldColors()
                )
                Spacer(Modifier.height(12.dp))
                Text("Method", color = TextSecondary, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("JazzCash", "EasyPaisa", "Bank").forEach { m ->
                        FilterChip(
                            selected = method == m,
                            onClick = { method = m },
                            label = { Text(m) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonGreen,
                                selectedLabelColor = BackgroundBlack
                            )
                        )
                    }
                }

                if (isWithdraw) {
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = accountTitle,
                        onValueChange = { accountTitle = it },
                        label = { Text("Account Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = com.miesport.app.ui.screens.login.miTextFieldColors()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = accountNumber,
                        onValueChange = { accountNumber = it },
                        label = { Text("$method Account Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = com.miesport.app.ui.screens.login.miTextFieldColors()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amt = amount.toDoubleOrNull() ?: 0.0
                if (amt <= 0) return@TextButton
                if (isWithdraw && (accountTitle.isBlank() || accountNumber.isBlank())) return@TextButton
                onConfirm(amt, method, accountTitle, accountNumber)
            }) { Text(confirmLabel, color = NeonGreen) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextMuted) }
        }
    )
}
