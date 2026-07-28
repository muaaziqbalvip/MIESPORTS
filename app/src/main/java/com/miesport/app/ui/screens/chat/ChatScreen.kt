package com.miesport.app.ui.screens.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Send
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
import com.miesport.app.data.model.ChatMessage
import com.miesport.app.data.remote.ImgBbRepository
import com.miesport.app.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreen(
    chatId: String,
    title: String,
    onBack: () -> Unit,
    viewModel: ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                ChatViewModel(chatId) as T
        }
    )
) {
    val messages by viewModel.messages.collectAsState()
    var input by remember { mutableStateOf("") }
    var isUploadingImage by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val imgBbRepo = remember { ImgBbRepository() }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploadingImage = true
            scope.launch {
                val result = imgBbRepo.uploadImage(context, uri)
                result.onSuccess { url -> viewModel.sendImageMessage(url) }
                isUploadingImage = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundBlack)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text(title, color = TextPrimary, style = MaterialTheme.typography.titleLarge)
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { msg ->
                    ChatBubble(msg, isMine = msg.senderId == viewModel.currentUserId)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { imagePicker.launch("image/*") },
                    enabled = !isUploadingImage
                ) {
                    if (isUploadingImage) {
                        CircularProgressIndicator(color = NeonGreen, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.Image, contentDescription = "Send image", tint = TextSecondary)
                    }
                }
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Message likhein...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = com.miesport.app.ui.screens.login.miTextFieldColors()
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (input.isNotBlank()) {
                            viewModel.sendMessage(input)
                            input = ""
                        }
                    }
                ) {
                    Icon(Icons.Filled.Send, contentDescription = "Send", tint = NeonGreen)
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage, isMine: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 260.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isMine) NeonGreen.copy(alpha = 0.15f) else SurfaceElevated)
                .padding(10.dp)
        ) {
            if (!isMine) {
                Text(
                    message.senderName,
                    color = if (message.isAdmin) GoldPrimary else NeonGreen,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(2.dp))
            }
            if (message.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = message.imageUrl,
                    contentDescription = "Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            }
            if (message.text.isNotBlank()) {
                Text(message.text, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.createdAt)),
                color = TextMuted,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.End).padding(top = 2.dp)
            )
        }
    }
}
