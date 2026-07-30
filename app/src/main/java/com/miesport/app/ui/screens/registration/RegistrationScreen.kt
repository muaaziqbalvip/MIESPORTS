package com.miesport.app.ui.screens.registration

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.miesport.app.ui.components.GlassCard
import com.miesport.app.ui.screens.login.miTextFieldColors
import com.miesport.app.ui.theme.*

@Composable
fun RegistrationScreen(
    tournamentId: String,
    viewModel: RegistrationViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                RegistrationViewModel(tournamentId) as T
        }
    ),
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    var inGameName by remember { mutableStateOf("") }
    var uidGame by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var teamName by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploadingImage by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val tournament by viewModel.tournament.collectAsState()
    val walletBalance by viewModel.walletBalance.collectAsState()
    val entryFee = tournament?.entryFee ?: 0.0
    val isPaid = entryFee > 0

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            isUploadingImage = true
            viewModel.uploadScreenshot(context, uri) { isUploadingImage = false }
        }
    }

    LaunchedEffect(state) {
        if (state is RegistrationUiState.Success) onSuccess()
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundBlack)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text("Register", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
            }

            GlassCard(modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    if (isPaid) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Entry Fee", color = TextSecondary, style = MaterialTheme.typography.labelLarge)
                                Text("Rs. ${entryFee.toInt()}", color = GoldPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Wallet Balance", color = TextSecondary, style = MaterialTheme.typography.labelLarge)
                                Text(
                                    "Rs. ${walletBalance.toInt()}",
                                    color = if (walletBalance >= entryFee) NeonGreen else DangerRed,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        if (walletBalance < entryFee) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Balance kam hai. Wallet mein deposit karke wapas aayein.",
                                color = DangerRed,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = SurfaceGlassBorder)
                        Spacer(Modifier.height(16.dp))
                    } else {
                        Text("FREE Tournament", color = NeonGreen, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))
                    }

                    OutlinedTextField(
                        value = inGameName,
                        onValueChange = { inGameName = it },
                        label = { Text("In-game Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = miTextFieldColors()
                    )
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = uidGame,
                        onValueChange = { uidGame = it },
                        label = { Text("UID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = miTextFieldColors()
                    )
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = region,
                        onValueChange = { region = it },
                        label = { Text("Region") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = miTextFieldColors()
                    )
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = teamName,
                        onValueChange = { teamName = it },
                        label = { Text("Team Name (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = miTextFieldColors()
                    )

                    Spacer(Modifier.height(14.dp))

                    // Real screenshot upload -> ImgBB, URL stored via viewModel.uploadScreenshot
                    if (selectedImageUri != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(14.dp))
                        ) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Payment screenshot",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            if (isUploadingImage) {
                                Box(
                                    modifier = Modifier.fillMaxSize().background(BackgroundBlack.copy(alpha = 0.6f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = NeonGreen)
                                }
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize().padding(8.dp),
                                    contentAlignment = Alignment.TopEnd
                                ) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = "Uploaded", tint = NeonGreen)
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }

                    OutlinedButton(
                        onClick = { imagePicker.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceGlassBorder)
                    ) {
                        Icon(Icons.Filled.CloudUpload, contentDescription = null, tint = TextSecondary)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (selectedImageUri == null) "Upload Payment Screenshot (Optional)" else "Change Screenshot",
                            color = TextSecondary
                        )
                    }

                    if (state is RegistrationUiState.Error) {
                        Spacer(Modifier.height(10.dp))
                        Text((state as RegistrationUiState.Error).message, color = DangerRed, style = MaterialTheme.typography.bodyMedium)
                    }

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (isPaid) showConfirmDialog = true
                            else viewModel.register(inGameName, uidGame, region, teamName)
                        },
                        enabled = state !is RegistrationUiState.Loading && !isUploadingImage &&
                            inGameName.isNotBlank() && uidGame.isNotBlank() && region.isNotBlank() &&
                            (!isPaid || walletBalance >= entryFee),
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                    ) {
                        if (state is RegistrationUiState.Loading) {
                            CircularProgressIndicator(color = BackgroundBlack, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        } else {
                            Text(
                                if (isPaid) "Pay Rs.${entryFee.toInt()} & Register" else "Confirm Registration",
                                color = BackgroundBlack,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(60.dp))
        }

        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                containerColor = SurfaceDark,
                title = { Text("Confirm Payment", color = TextPrimary) },
                text = {
                    Text(
                        "Rs. ${entryFee.toInt()} aapke wallet se kat jayenge aur aap is tournament mein register ho jayenge. Continue karein?",
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showConfirmDialog = false
                        viewModel.register(inGameName, uidGame, region, teamName)
                    }) { Text("Confirm & Pay", color = NeonGreen, fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDialog = false }) { Text("Cancel", color = TextMuted) }
                }
            )
        }
    }
}
