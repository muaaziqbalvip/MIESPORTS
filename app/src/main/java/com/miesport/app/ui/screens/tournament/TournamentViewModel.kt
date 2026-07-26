package com.miesport.app.ui.screens.tournament

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miesport.app.data.firebase.FirestoreRepository
import com.miesport.app.data.firebase.RealtimeRepository
import com.miesport.app.data.model.LiveStatus
import com.miesport.app.data.model.Tournament
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TournamentListViewModel(
    private val repo: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _filterMode = MutableStateFlow<String?>(null) // SOLO/DUO/SQUAD or null = all
    val filterMode: StateFlow<String?> = _filterMode.asStateFlow()

    private val allTournaments: StateFlow<List<Tournament>> = repo.observeTournaments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredTournaments: StateFlow<List<Tournament>> =
        combine(allTournaments, _filterMode) { list, mode ->
            if (mode == null) list else list.filter { it.mode == mode }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(mode: String?) { _filterMode.value = mode }
}

class TournamentDetailViewModel(
    private val tournamentId: String,
    private val repo: FirestoreRepository = FirestoreRepository(),
    private val rtdb: RealtimeRepository = RealtimeRepository()
) : ViewModel() {

    private val _tournament = MutableStateFlow<Tournament?>(null)
    val tournament: StateFlow<Tournament?> = _tournament.asStateFlow()

    val roomReveal: StateFlow<Map<String, String>?> = rtdb.observeRoomReveal(tournamentId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            _tournament.value = repo.getTournament(tournamentId)
        }
    }
}
