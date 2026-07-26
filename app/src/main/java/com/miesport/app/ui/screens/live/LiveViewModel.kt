package com.miesport.app.ui.screens.live

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miesport.app.data.firebase.FirestoreRepository
import com.miesport.app.data.firebase.RealtimeRepository
import com.miesport.app.data.model.LiveStatus
import com.miesport.app.data.model.Tournament
import kotlinx.coroutines.flow.*

class LiveViewModel(
    private val firestoreRepo: FirestoreRepository = FirestoreRepository(),
    private val rtdb: RealtimeRepository = RealtimeRepository()
) : ViewModel() {

    val liveTournaments: StateFlow<List<Tournament>> = firestoreRepo.observeTournaments(status = "LIVE")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Map of tournamentId -> LiveStatus, built by combining live status flows per tournament.
    val liveStatuses: StateFlow<Map<String, LiveStatus>> = liveTournaments
        .flatMapLatest { tournaments ->
            if (tournaments.isEmpty()) {
                flowOf(emptyMap())
            } else {
                combine(
                    tournaments.map { t ->
                        rtdb.observeLiveStatus(t.id).map { status -> t.id to status }
                    }
                ) { pairs ->
                    pairs.mapNotNull { (id, status) -> status?.let { id to it } }.toMap()
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
}
