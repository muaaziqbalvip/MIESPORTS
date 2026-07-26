package com.miesport.app.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miesport.app.data.firebase.AuthRepository
import com.miesport.app.data.firebase.FirestoreRepository
import com.miesport.app.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(
    private val authRepo: AuthRepository = AuthRepository(),
    private val firestoreRepo: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun signInWithEmail(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Email aur password dono zaroori hain")
            return
        }
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val result = authRepo.signInWithEmail(email.trim(), password)
            result.onSuccess {
                _uiState.value = LoginUiState.Success
            }.onFailure { e ->
                _uiState.value = LoginUiState.Error(e.message ?: "Sign-in fail ho gaya")
            }
        }
    }

    fun signUpWithEmail(email: String, password: String, gamingName: String) {
        if (email.isBlank() || password.isBlank() || gamingName.isBlank()) {
            _uiState.value = LoginUiState.Error("Sab fields zaroori hain")
            return
        }
        if (password.length < 6) {
            _uiState.value = LoginUiState.Error("Password kam az kam 6 characters ka ho")
            return
        }
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val result = authRepo.signUpWithEmail(email.trim(), password)
            result.onSuccess { user ->
                firestoreRepo.upsertUser(
                    UserProfile(uid = user.uid, gamingName = gamingName, email = email.trim())
                )
                _uiState.value = LoginUiState.Success
            }.onFailure { e ->
                _uiState.value = LoginUiState.Error(e.message ?: "Signup fail ho gaya")
            }
        }
    }

    fun signInWithGoogleToken(idToken: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val result = authRepo.signInWithGoogle(idToken)
            result.onSuccess { user ->
                val existing = firestoreRepo.getUser(user.uid)
                if (existing == null) {
                    firestoreRepo.upsertUser(
                        UserProfile(
                            uid = user.uid,
                            gamingName = user.displayName ?: "Player",
                            email = user.email ?: "",
                            avatarUrl = user.photoUrl?.toString() ?: ""
                        )
                    )
                }
                _uiState.value = LoginUiState.Success
            }.onFailure { e ->
                _uiState.value = LoginUiState.Error(e.message ?: "Google sign-in fail ho gaya")
            }
        }
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _uiState.value = LoginUiState.Error("Email daalein")
            return
        }
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val result = authRepo.sendPasswordReset(email.trim())
            result.onSuccess {
                _uiState.value = LoginUiState.Success
            }.onFailure { e ->
                _uiState.value = LoginUiState.Error(e.message ?: "Reset email bhejne mein masla hua")
            }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}
