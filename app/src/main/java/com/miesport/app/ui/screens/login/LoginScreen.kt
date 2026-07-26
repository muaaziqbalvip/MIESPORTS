package com.miesport.app.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.miesport.app.ui.components.GlassCard
import com.miesport.app.ui.components.NeonGlow
import com.miesport.app.ui.theme.*

enum class LoginMode { SIGN_IN, SIGN_UP }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onGoogleSignInClick: () -> Unit,
    onForgotPassword: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var mode by remember { mutableStateOf(LoginMode.SIGN_IN) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var gamingName by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()
    val isLoading = uiState is LoginUiState.Loading

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) onLoginSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
    ) {
        NeonGlow(
            modifier = Modifier.align(Alignment.TopStart).offset(x = (-60).dp, y = (-40).dp),
            color = NeonGreenGlow
        )
        NeonGlow(
            modifier = Modifier.align(Alignment.BottomEnd).offset(x = 60.dp, y = 60.dp),
            color = AccentRedGlow,
            size = 260.dp
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(64.dp))

            Text(
                "MI ESPORT",
                style = MaterialTheme.typography.displayLarge,
                color = NeonGreen,
                textAlign = TextAlign.Center
            )
            Text(
                "Play. Compete. Win.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(40.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {

                    // Tab switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceElevated),
                    ) {
                        listOf(LoginMode.SIGN_IN to "Sign In", LoginMode.SIGN_UP to "Sign Up").forEach { (m, label) ->
                            val selected = mode == m
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (selected) NeonGreen else Color.Transparent)
                                    .clickable { mode = m; viewModel.resetState() }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    label,
                                    color = if (selected) BackgroundBlack else TextSecondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    if (mode == LoginMode.SIGN_UP) {
                        OutlinedTextField(
                            value = gamingName,
                            onValueChange = { gamingName = it },
                            label = { Text("Gaming Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = miTextFieldColors()
                        )
                        Spacer(Modifier.height(14.dp))
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        colors = miTextFieldColors()
                    )

                    Spacer(Modifier.height(14.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = miTextFieldColors()
                    )

                    if (mode == LoginMode.SIGN_IN) {
                        TextButton(
                            onClick = onForgotPassword,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Forgot Password?", color = NeonGreen, style = MaterialTheme.typography.labelLarge)
                        }
                    } else {
                        Spacer(Modifier.height(8.dp))
                    }

                    if (uiState is LoginUiState.Error) {
                        Text(
                            (uiState as LoginUiState.Error).message,
                            color = DangerRed,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (mode == LoginMode.SIGN_IN) viewModel.signInWithEmail(email, password)
                            else viewModel.signUpWithEmail(email, password, gamingName)
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = BackgroundBlack, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        } else {
                            Text(
                                if (mode == LoginMode.SIGN_IN) "Sign In" else "Create Account",
                                color = BackgroundBlack,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceGlassBorder)
                        Text("  OR  ", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                        HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceGlassBorder)
                    }

                    Spacer(Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = onGoogleSignInClick,
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceGlassBorder)
                    ) {
                        Text("Continue with Google", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Sign in karke aap hamari User Agreement aur Privacy Policy se agree karte hain",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun miTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NeonGreen,
    unfocusedBorderColor = SurfaceGlassBorder,
    focusedLabelColor = NeonGreen,
    unfocusedLabelColor = TextMuted,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = NeonGreen,
    focusedLeadingIconColor = NeonGreen,
    unfocusedLeadingIconColor = TextMuted
)
