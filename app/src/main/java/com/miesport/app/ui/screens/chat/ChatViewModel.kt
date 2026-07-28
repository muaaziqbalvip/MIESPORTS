package com.miesport.app.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.miesport.app.data.firebase.FirestoreRepository
import com.miesport.app.data.model.ChatMessage
import com.miesport.app.data.model.UserProfile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatId: String,
    private val repo: FirestoreRepository = FirestoreRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    val messages: StateFlow<List<ChatMessage>> = repo.observeChatMessages(chatId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var cachedProfile: UserProfile? = null

    /** Support chats are indexed so the admin panel can discover active conversations. */
    private fun touchSupportIndexIfNeeded() {
        if (!chatId.startsWith("support_")) return
        db.collection("support_chat_index").document(chatId)
            .set(mapOf("lastMessageAt" to System.currentTimeMillis()))
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val profile = cachedProfile ?: repo.getUser(uid)?.also { cachedProfile = it }
            repo.sendChatMessage(
                chatId,
                ChatMessage(
                    senderId = uid,
                    senderName = profile?.gamingName ?: "Player",
                    senderAvatarUrl = profile?.avatarUrl ?: "",
                    text = text.trim()
                )
            )
            touchSupportIndexIfNeeded()
        }
    }

    fun sendImageMessage(imageUrl: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val profile = cachedProfile ?: repo.getUser(uid)?.also { cachedProfile = it }
            repo.sendChatMessage(
                chatId,
                ChatMessage(
                    senderId = uid,
                    senderName = profile?.gamingName ?: "Player",
                    senderAvatarUrl = profile?.avatarUrl ?: "",
                    imageUrl = imageUrl
                )
            )
            touchSupportIndexIfNeeded()
        }
    }

    val currentUserId: String get() = auth.currentUser?.uid ?: ""
}
