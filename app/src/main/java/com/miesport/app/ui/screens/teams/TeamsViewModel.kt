package com.miesport.app.ui.screens.teams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.miesport.app.data.firebase.FirestoreRepository
import com.miesport.app.data.model.Team
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TeamsViewModel(
    private val repo: FirestoreRepository = FirestoreRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _myTeam = MutableStateFlow<Team?>(null)
    val myTeam: StateFlow<Team?> = _myTeam.asStateFlow()

    init {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            val user = repo.getUser(uid)
            if (!user?.teamId.isNullOrBlank()) {
                repo.observeTeam(user!!.teamId).collect { _myTeam.value = it }
            }
        }
    }

    fun createTeam(name: String) {
        if (name.isBlank()) return
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val teamId = repo.createTeam(Team(name = name.trim(), captainId = uid, members = listOf(uid)))
            db.collection("users").document(uid).update("teamId", teamId)
            repo.observeTeam(teamId).collect { _myTeam.value = it }
        }
    }

    fun joinTeam(teamIdOrCode: String) {
        if (teamIdOrCode.isBlank()) return
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val teamRef = db.collection("teams").document(teamIdOrCode.trim())
            db.runTransaction { txn ->
                val snap = txn.get(teamRef)
                @Suppress("UNCHECKED_CAST")
                val members = (snap.get("members") as? List<String>) ?: emptyList()
                if (!members.contains(uid)) {
                    txn.update(teamRef, "members", members + uid)
                }
                txn.update(db.collection("users").document(uid), "teamId", teamIdOrCode.trim())
            }
            repo.observeTeam(teamIdOrCode.trim()).collect { _myTeam.value = it }
        }
    }
}
