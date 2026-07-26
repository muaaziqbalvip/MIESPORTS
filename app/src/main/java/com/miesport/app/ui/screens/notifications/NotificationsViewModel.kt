package com.miesport.app.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.miesport.app.data.firebase.FirestoreRepository
import com.miesport.app.data.model.AppNotification
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val repo: FirestoreRepository = FirestoreRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val uid get() = auth.currentUser?.uid ?: ""

    val notifications: StateFlow<List<AppNotification>> = repo.observeNotifications(uid)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun markRead(id: String) {
        viewModelScope.launch { repo.markNotificationRead(uid, id) }
    }
}
