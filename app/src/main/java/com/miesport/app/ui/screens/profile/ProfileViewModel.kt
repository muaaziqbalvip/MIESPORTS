package com.miesport.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.miesport.app.data.firebase.AuthRepository
import com.miesport.app.data.firebase.FirestoreRepository
import com.miesport.app.data.model.UserProfile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ProfileViewModel(
    private val repo: FirestoreRepository = FirestoreRepository(),
    private val authRepo: AuthRepository = AuthRepository(),
    auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val uid = auth.currentUser?.uid ?: ""

    val user: StateFlow<UserProfile?> = repo.observeUser(uid)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun signOut() {
        authRepo.signOut()
    }
}
