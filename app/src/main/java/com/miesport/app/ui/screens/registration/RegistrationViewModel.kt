package com.miesport.app.ui.screens.registration

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.miesport.app.data.firebase.FirestoreRepository
import com.miesport.app.data.model.Registration
import com.miesport.app.data.model.Tournament
import com.miesport.app.data.remote.ImgBbRepository
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
    private val imgBbRepo: ImgBbRepository = ImgBbRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegistrationUiState>(RegistrationUiState.Idle)
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    private val _tournament = MutableStateFlow<Tournament?>(null)
    val tournament: StateFlow<Tournament?> = _tournament.asStateFlow()

    private val _walletBalance = MutableStateFlow(0.0)
    val walletBalance: StateFlow<Double> = _walletBalance.asStateFlow()

    private var screenshotUrl: String = ""

    init {
        viewModelScope.launch {
            _tournament.value = repo.getTournament(tournamentId)
            auth.currentUser?.uid?.let { uid ->
                _walletBalance.value = repo.getUser(uid)?.walletBalance ?: 0.0
            }
        }
    }

    fun setScreenshotUrl(url: String) { screenshotUrl = url }

    fun uploadScreenshot(context: Context, uri: Uri, onDone: () -> Unit) {
        viewModelScope.launch {
            val result = imgBbRepo.uploadImage(context, uri)
            result.onSuccess { url ->
                screenshotUrl = url
            }.onFailure { e ->
                _uiState.value = RegistrationUiState.Error(e.message ?: "Screenshot upload nahi ho saka")
            }
            onDone()
        }
    }

    /** Called after the user confirms the entry-fee dialog (for paid tournaments). */
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
        val entryFee = _tournament.value?.entryFee ?: 0.0

        viewModelScope.launch {
            _uiState.value = RegistrationUiState.Loading
            runCatching {
                repo.registerForTournament(
                    tournamentId = tournamentId,
                    userId = uid,
                    reg = Registration(
                        userId = uid,
                        inGameName = inGameName.trim(),
                        uidGame = uidGame.trim(),
                        region = region.trim(),
                        teamName = teamName.trim(),
                        screenshotUrl = screenshotUrl
                    ),
                    entryFee = entryFee
                )
            }.onSuccess {
                _uiState.value = RegistrationUiState.Success
            }.onFailure { e ->
                _uiState.value = RegistrationUiState.Error(e.message ?: "Registration fail ho gayi")
            }
        }
    }
}
