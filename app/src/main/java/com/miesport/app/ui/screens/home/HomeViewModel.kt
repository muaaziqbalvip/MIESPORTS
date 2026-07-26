package com.miesport.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miesport.app.data.firebase.FirestoreRepository
import com.miesport.app.data.model.Tournament
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val repo: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    val tournaments: StateFlow<List<Tournament>> = repo.observeTournaments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
