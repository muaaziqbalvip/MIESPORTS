package com.miesport.app.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.miesport.app.data.model.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Central Firestore data access. Collections:
 * users, tournaments, tournaments/{id}/registrations, wallet_transactions,
 * teams, leaderboard/{period}/entries, notifications/{uid}/items
 */
class FirestoreRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    // ---------- Users ----------
    suspend fun getUser(uid: String): UserProfile? =
        db.collection("users").document(uid).get().await().toObject(UserProfile::class.java)

    fun observeUser(uid: String): Flow<UserProfile?> = callbackFlow {
        val reg = db.collection("users").document(uid)
            .addSnapshotListener { snap, _ -> trySend(snap?.toObject(UserProfile::class.java)) }
        awaitClose { reg.remove() }
    }

    suspend fun upsertUser(profile: UserProfile) {
        db.collection("users").document(profile.uid).set(profile).await()
    }

    // ---------- Tournaments ----------
    fun observeTournaments(status: String? = null): Flow<List<Tournament>> = callbackFlow {
        var query: Query = db.collection("tournaments").orderBy("matchDateTime")
        if (status != null) query = query.whereEqualTo("status", status)
        val reg = query.addSnapshotListener { snap, _ ->
            trySend(snap?.toObjects(Tournament::class.java) ?: emptyList())
        }
        awaitClose { reg.remove() }
    }

    suspend fun getTournament(id: String): Tournament? =
        db.collection("tournaments").document(id).get().await().toObject(Tournament::class.java)

    suspend fun createTournament(t: Tournament): String {
        val ref = db.collection("tournaments").document()
        db.collection("tournaments").document(ref.id).set(t.copy(id = ref.id)).await()
        return ref.id
    }

    suspend fun updateTournament(id: String, updates: Map<String, Any>) {
        db.collection("tournaments").document(id).update(updates).await()
    }

    suspend fun deleteTournament(id: String) {
        db.collection("tournaments").document(id).delete().await()
    }

    // ---------- Registrations ----------
    suspend fun registerForTournament(tournamentId: String, reg: Registration): String {
        val ref = db.collection("tournaments").document(tournamentId)
            .collection("registrations").document()
        db.runTransaction { txn ->
            val tRef = db.collection("tournaments").document(tournamentId)
            val snap = txn.get(tRef)
            val filled = snap.getLong("filledSlots") ?: 0
            val total = snap.getLong("totalSlots") ?: 0
            if (filled >= total) throw IllegalStateException("Tournament full")
            txn.set(ref, reg.copy(id = ref.id))
            txn.update(tRef, "filledSlots", filled + 1)
        }.await()
        return ref.id
    }

    fun observeRegistrations(tournamentId: String): Flow<List<Registration>> = callbackFlow {
        val reg = db.collection("tournaments").document(tournamentId).collection("registrations")
            .addSnapshotListener { snap, _ -> trySend(snap?.toObjects(Registration::class.java) ?: emptyList()) }
        awaitClose { reg.remove() }
    }

    // ---------- Wallet ----------
    suspend fun requestTransaction(tx: WalletTransaction): String {
        val ref = db.collection("wallet_transactions").document()
        db.collection("wallet_transactions").document(ref.id).set(tx.copy(id = ref.id)).await()
        return ref.id
    }

    fun observeUserTransactions(uid: String): Flow<List<WalletTransaction>> = callbackFlow {
        val reg = db.collection("wallet_transactions")
            .whereEqualTo("userId", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ -> trySend(snap?.toObjects(WalletTransaction::class.java) ?: emptyList()) }
        awaitClose { reg.remove() }
    }

    suspend fun approveTransaction(txId: String, uid: String, amountDelta: Double) {
        db.runTransaction { txn ->
            val txRef = db.collection("wallet_transactions").document(txId)
            val userRef = db.collection("users").document(uid)
            val userSnap = txn.get(userRef)
            val balance = userSnap.getDouble("walletBalance") ?: 0.0
            txn.update(txRef, "status", "APPROVED")
            txn.update(userRef, "walletBalance", balance + amountDelta)
        }.await()
    }

    // ---------- Teams ----------
    suspend fun createTeam(team: Team): String {
        val ref = db.collection("teams").document()
        db.collection("teams").document(ref.id).set(team.copy(id = ref.id)).await()
        return ref.id
    }

    fun observeTeam(teamId: String): Flow<Team?> = callbackFlow {
        val reg = db.collection("teams").document(teamId)
            .addSnapshotListener { snap, _ -> trySend(snap?.toObject(Team::class.java)) }
        awaitClose { reg.remove() }
    }

    // ---------- Leaderboard ----------
    fun observeLeaderboard(period: String = "season"): Flow<List<LeaderboardEntry>> = callbackFlow {
        val reg = db.collection("leaderboard").document(period).collection("entries")
            .orderBy("points", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snap, _ -> trySend(snap?.toObjects(LeaderboardEntry::class.java) ?: emptyList()) }
        awaitClose { reg.remove() }
    }

    // ---------- Notifications ----------
    fun observeNotifications(uid: String): Flow<List<AppNotification>> = callbackFlow {
        val reg = db.collection("notifications").document(uid).collection("items")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ -> trySend(snap?.toObjects(AppNotification::class.java) ?: emptyList()) }
        awaitClose { reg.remove() }
    }

    suspend fun markNotificationRead(uid: String, notifId: String) {
        db.collection("notifications").document(uid).collection("items")
            .document(notifId).update("read", true).await()
    }
}
