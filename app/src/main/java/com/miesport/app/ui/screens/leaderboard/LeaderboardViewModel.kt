package com.miesport.app.ui.screens.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miesport.app.data.firebase.FirestoreRepository
import com.miesport.app.data.model.LeaderboardEntry
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LeaderboardViewModel(
    private val repo: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _entries = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val entries: StateFlow<List<LeaderboardEntry>> = _entries.asStateFlow()

    private var job: Job? = null

    fun setPeriod(period: String) {
        job?.cancel()
        job = viewModelScope.launch {
            repo.observeLeaderboard(period).collect { _entries.value = it }
        }
    }

    init { setPeriod("season") }
}
