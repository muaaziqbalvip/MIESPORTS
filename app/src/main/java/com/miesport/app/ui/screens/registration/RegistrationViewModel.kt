package com.miesport.app.ui.screens.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.miesport.app.data.firebase.FirestoreRepository
import com.miesport.app.data.model.Registration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class RegistrationUiState {
    object Idle : RegistrationUiState()
    object Loading : RegistrationUiState()
    object Success : RegistrationUiState()
    data class Error(val message: String) : RegistrationUiState()
}

class RegistrationViewModel(
    private val tournamentId: String,
    private val repo: FirestoreRepository = FirestoreRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegistrationUiState>(RegistrationUiState.Idle)
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    private var screenshotUrl: String = ""

    fun setScreenshotUrl(url: String) { screenshotUrl = url }

    fun register(inGameName: String, uidGame: String, region: String, teamName: String) {
        if (inGameName.isBlank() || uidGame.isBlank() || region.isBlank()) {
            _uiState.value = RegistrationUiState.Error("In-game name, UID aur region zaroori hain")
            return
        }
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _uiState.value = RegistrationUiState.Error("Pehle login karein")
            return
        }
        viewModelScope.launch {
            _uiState.value = RegistrationUiState.Loading
            runCatching {
                repo.registerForTournament(
                    tournamentId,
                    Registration(
                        userId = uid,
                        inGameName = inGameName.trim(),
                        uidGame = uidGame.trim(),
                        region = region.trim(),
                        teamName = teamName.trim(),
                        screenshotUrl = screenshotUrl
                    )
                )
            }.onSuccess {
                _uiState.value = RegistrationUiState.Success
            }.onFailure { e ->
                _uiState.value = RegistrationUiState.Error(e.message ?: "Registration fail ho gayi")
            }
        }
    }
}
