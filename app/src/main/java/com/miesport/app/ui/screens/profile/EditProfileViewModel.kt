package com.miesport.app.ui.screens.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.miesport.app.data.firebase.FirestoreRepository
import com.miesport.app.data.model.UserProfile
import com.miesport.app.data.remote.ImgBbRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class EditProfileUiState {
    object Idle : EditProfileUiState()
    object Loading : EditProfileUiState()
    object Success : EditProfileUiState()
    data class Error(val message: String) : EditProfileUiState()
}

class EditProfileViewModel(
    private val repo: FirestoreRepository = FirestoreRepository(),
    private val imgBbRepo: ImgBbRepository = ImgBbRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val uid get() = auth.currentUser?.uid ?: ""

    val user: StateFlow<UserProfile?> = repo.observeUser(uid)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _uiState = MutableStateFlow<EditProfileUiState>(EditProfileUiState.Idle)
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private var pendingAvatarUrl: String? = null

    fun uploadAvatar(context: Context, uri: Uri, onDone: () -> Unit) {
        viewModelScope.launch {
            val result = imgBbRepo.uploadImage(context, uri)
            result.onSuccess { url -> pendingAvatarUrl = url }
                .onFailure { e -> _uiState.value = EditProfileUiState.Error(e.message ?: "Photo upload nahi ho saka") }
            onDone()
        }
    }

    fun saveProfile(gamingName: String, uidGame: String, region: String) {
        if (gamingName.isBlank()) {
            _uiState.value = EditProfileUiState.Error("Gaming Name khaali nahi ho sakta")
            return
        }
        if (uid.isBlank()) {
            _uiState.value = EditProfileUiState.Error("Session expire ho gaya, dobara login karein")
            return
        }
        viewModelScope.launch {
            _uiState.value = EditProfileUiState.Loading
            runCatching {
                val updates = mutableMapOf<String, Any>(
                    "gamingName" to gamingName.trim(),
                    "uidGame" to uidGame.trim(),
                    "region" to region.trim()
                )
                pendingAvatarUrl?.let { updates["avatarUrl"] = it }
                db.collection("users").document(uid).update(updates).await()
            }.onSuccess {
                _uiState.value = EditProfileUiState.Success
            }.onFailure { e ->
                _uiState.value = EditProfileUiState.Error(e.message ?: "Save nahi ho saka, dobara try karein")
            }
        }
    }
}

private suspend fun com.google.android.gms.tasks.Task<Void>.await() =
    kotlinx.coroutines.tasks.await(this)
