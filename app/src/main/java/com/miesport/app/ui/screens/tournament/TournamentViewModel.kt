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

    private val _loadError = MutableStateFlow<String?>(null)
    val loadError: StateFlow<String?> = _loadError.asStateFlow()

    val roomReveal: StateFlow<Map<String, String>?> =
        if (tournamentId.isBlank()) {
            MutableStateFlow(null)
        } else {
            rtdb.observeRoomReveal(tournamentId)
                .catch { emit(null) } // never let an RTDB error crash the screen
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        }

    init {
        if (tournamentId.isBlank()) {
            _loadError.value = "Tournament ID missing hai"
        } else {
            viewModelScope.launch {
                runCatching {
                    repo.getTournament(tournamentId)
                }.onSuccess { result ->
                    _tournament.value = result
                    if (result == null) _loadError.value = "Ye tournament nahi mila"
                }.onFailure { e ->
                    _loadError.value = e.message ?: "Tournament load nahi ho saka"
                }
            }
        }
    }
}
