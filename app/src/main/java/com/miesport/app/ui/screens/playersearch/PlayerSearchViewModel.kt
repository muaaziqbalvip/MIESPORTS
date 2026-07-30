package com.miesport.app.ui.screens.playersearch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.miesport.app.data.firebase.FirestoreRepository
import com.miesport.app.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PlayerSearchUiState {
    object Idle : PlayerSearchUiState()
    object Loading : PlayerSearchUiState()
    object NotFound : PlayerSearchUiState()
    data class Found(val player: UserProfile) : PlayerSearchUiState()
    data class Error(val message: String) : PlayerSearchUiState()
}

class PlayerSearchViewModel(
    private val repo: FirestoreRepository = FirestoreRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlayerSearchUiState>(PlayerSearchUiState.Idle)
    val uiState: StateFlow<PlayerSearchUiState> = _uiState.asStateFlow()

    fun searchByUid(uidGame: String) {
        viewModelScope.launch {
            _uiState.value = PlayerSearchUiState.Loading
            runCatching {
                repo.findUserByGameUid(uidGame)
            }.onSuccess { player ->
                _uiState.value = when {
                    player == null -> PlayerSearchUiState.NotFound
                    player.uid == auth.currentUser?.uid -> PlayerSearchUiState.Error("Ye aapka apna UID hai")
                    else -> PlayerSearchUiState.Found(player)
                }
            }.onFailure { e ->
                _uiState.value = PlayerSearchUiState.Error(e.message ?: "Search fail ho gayi")
            }
        }
    }
}
