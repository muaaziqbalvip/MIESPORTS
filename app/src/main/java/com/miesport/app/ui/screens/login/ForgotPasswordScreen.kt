package com.miesport.app.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.miesport.app.ui.components.GlassCard
import com.miesport.app.ui.theme.*

@Composable
fun ForgotPasswordScreen(
    viewModel: LoginViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val isLoading = uiState is LoginUiState.Loading
    val isSuccess = uiState is LoginUiState.Success

    Box(modifier = Modifier.fillMaxSize().background(BackgroundBlack)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text("Reset Password", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
            }

            GlassCard(modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    if (isSuccess) {
                        Text(
                            "Reset link aapki email pe bhej di gayi hai. Inbox check karein.",
                            color = NeonGreen,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        Text(
                            "Apna email daalein, hum aapko password reset link bhejenge",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = miTextFieldColors()
                        )

                        if (uiState is LoginUiState.Error) {
                            Spacer(Modifier.height(8.dp))
                            Text((uiState as LoginUiState.Error).message, color = DangerRed, style = MaterialTheme.typography.bodyMedium)
                        }

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.sendPasswordReset(email) },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = BackgroundBlack, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Send Reset Link", color = BackgroundBlack, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
