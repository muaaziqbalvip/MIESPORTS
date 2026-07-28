package com.miesport.app.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
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
fun EditProfileScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: EditProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val user by viewModel.user.collectAsState()
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var gamingName by remember { mutableStateOf("") }
    var uidGame by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var localAvatarUri by remember { mutableStateOf<Uri?>(null) }
    var isUploadingAvatar by remember { mutableStateOf(false) }
    var initialized by remember { mutableStateOf(false) }

    // Populate fields once profile loads, without overwriting user's edits on every recomposition
    LaunchedEffect(user) {
        if (!initialized && user != null) {
            gamingName = user?.gamingName ?: ""
            uidGame = user?.uidGame ?: ""
            region = user?.region ?: ""
            initialized = true
        }
    }

    LaunchedEffect(state) {
        if (state is EditProfileUiState.Success) onSaved()
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            localAvatarUri = uri
            isUploadingAvatar = true
            viewModel.uploadAvatar(context, uri) { isUploadingAvatar = false }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundBlack)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text("Edit Profile", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
            }

            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier.size(100.dp).clip(CircleShape).background(SurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        val displayImage = localAvatarUri ?: user?.avatarUrl?.takeIf { it.isNotBlank() }
                        if (displayImage != null) {
                            AsyncImage(
                                model = displayImage,
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(48.dp))
                        }
                        if (isUploadingAvatar) {
                            Box(
                                modifier = Modifier.fillMaxSize().background(BackgroundBlack.copy(alpha = 0.6f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = NeonGreen, modifier = Modifier.size(28.dp))
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(NeonGreen)
                            .then(Modifier),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = { imagePicker.launch("image/*") }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = "Change photo", tint = BackgroundBlack, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            GlassCard(modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    OutlinedTextField(
                        value = gamingName,
                        onValueChange = { gamingName = it },
                        label = { Text("Gaming Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = miTextFieldColors()
                    )
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = uidGame,
                        onValueChange = { uidGame = it },
                        label = { Text("In-game UID") },
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
                        value = user?.email ?: "",
                        onValueChange = {},
                        label = { Text("Email") },
                        enabled = false,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = miTextFieldColors()
                    )

                    if (state is EditProfileUiState.Error) {
                        Spacer(Modifier.height(10.dp))
                        Text((state as EditProfileUiState.Error).message, color = DangerRed, style = MaterialTheme.typography.bodyMedium)
                    }

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.saveProfile(gamingName, uidGame, region) },
                        enabled = state !is EditProfileUiState.Loading && !isUploadingAvatar,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                    ) {
                        if (state is EditProfileUiState.Loading) {
                            CircularProgressIndicator(color = BackgroundBlack, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Save Changes", color = BackgroundBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(60.dp))
        }
    }
}
