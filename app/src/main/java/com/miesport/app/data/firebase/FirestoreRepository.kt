package com.miesport.app.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.miesport.app.data.model.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Central Firestore data access. Collections:
 * users, tournaments, tournaments/{id}/registrations, wallet_transactions,
 * teams, leaderboard/{period}/entries, notifications/{uid}/items, chats, payment_methods
 *
 * Every listener below is defensive: Firestore errors (missing index, permission
 * denial, offline) or malformed documents never crash the app — they resolve to
 * an empty/null result instead, and the UI shows an empty/loading state.
 */
class FirestoreRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    /** Safely converts a snapshot to a list of T, skipping any doc that fails to parse. */
    private inline fun <reified T> QuerySnapshot?.toSafeList(): List<T> {
        if (this == null) return emptyList()
        return documents.mapNotNull { doc ->
            runCatching { doc.toObject(T::class.java) }.getOrNull()
        }
    }

    // ---------- Users ----------
    suspend fun getUser(uid: String): UserProfile? = runCatching {
        db.collection("users").document(uid).get().await().toObject(UserProfile::class.java)
    }.getOrNull()

    fun observeUser(uid: String): Flow<UserProfile?> = callbackFlow {
        if (uid.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val reg = db.collection("users").document(uid)
            .addSnapshotListener { snap, _ ->
                trySend(runCatching { snap?.toObject(UserProfile::class.java) }.getOrNull())
            }
        awaitClose { reg.remove() }
    }

    suspend fun upsertUser(profile: UserProfile) {
        db.collection("users").document(profile.uid).set(profile).await()
    }

    // ---------- Tournaments ----------
    fun observeTournaments(status: String? = null): Flow<List<Tournament>> = callbackFlow {
        // Filtering by status client-side avoids requiring a Firestore composite index
        // (status + matchDateTime), which otherwise silently breaks the listener.
        val reg = db.collection("tournaments").orderBy("matchDateTime")
            .addSnapshotListener { snap, _ ->
                val all: List<Tournament> = snap.toSafeList()
                trySend(if (status == null) all else all.filter { it.status == status })
            }
        awaitClose { reg.remove() }
    }

    suspend fun getTournament(id: String): Tournament? = runCatching {
        if (id.isBlank()) return@runCatching null
        db.collection("tournaments").document(id).get().await().toObject(Tournament::class.java)
    }.getOrNull()

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
    /**
     * Registers a player and (for paid tournaments) atomically deducts the entry
     * fee from their wallet in the same transaction — so a user is never charged
     * without a confirmed registration, and never registered without payment.
     */
    suspend fun registerForTournament(
        tournamentId: String,
        userId: String,
        reg: Registration,
        entryFee: Double
    ): String {
        val ref = db.collection("tournaments").document(tournamentId)
            .collection("registrations").document()
        val tRef = db.collection("tournaments").document(tournamentId)
        val userRef = db.collection("users").document(userId)

        db.runTransaction { txn ->
            val tSnap = txn.get(tRef)
            val filled = tSnap.getLong("filledSlots") ?: 0
            val total = tSnap.getLong("totalSlots") ?: 0
            if (filled >= total) throw IllegalStateException("Slots full ho chuke hain")

            if (entryFee > 0) {
                val userSnap = txn.get(userRef)
                val balance = userSnap.getDouble("walletBalance") ?: 0.0
                if (balance < entryFee) {
                    throw IllegalStateException("Wallet balance kam hai. Pehle deposit karein.")
                }
                txn.update(userRef, "walletBalance", balance - entryFee)
                txn.set(
                    db.collection("wallet_transactions").document(),
                    mapOf(
                        "userId" to userId,
                        "type" to "ENTRY_FEE",
                        "amount" to entryFee,
                        "status" to "APPROVED",
                        "note" to "Entry fee for tournament $tournamentId",
                        "createdAt" to System.currentTimeMillis()
                    )
                )
            }

            txn.set(ref, reg.copy(id = ref.id))
            txn.update(tRef, "filledSlots", filled + 1)
        }.await()
        return ref.id
    }

    fun observeRegistrations(tournamentId: String): Flow<List<Registration>> = callbackFlow {
        val reg = db.collection("tournaments").document(tournamentId).collection("registrations")
            .addSnapshotListener { snap, _ -> trySend(snap.toSafeList<Registration>()) }
        awaitClose { reg.remove() }
    }

    // ---------- Wallet ----------
    suspend fun requestTransaction(tx: WalletTransaction): String {
        val ref = db.collection("wallet_transactions").document()
        db.collection("wallet_transactions").document(ref.id).set(tx.copy(id = ref.id)).await()
        return ref.id
    }

    fun observeUserTransactions(uid: String): Flow<List<WalletTransaction>> = callbackFlow {
        if (uid.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val reg = db.collection("wallet_transactions")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snap, _ ->
                val list: List<WalletTransaction> = snap.toSafeList()
                trySend(list.sortedByDescending { it.createdAt })
            }
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
        if (teamId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val reg = db.collection("teams").document(teamId)
            .addSnapshotListener { snap, _ ->
                trySend(runCatching { snap?.toObject(Team::class.java) }.getOrNull())
            }
        awaitClose { reg.remove() }
    }

    // ---------- Leaderboard ----------
    fun observeLeaderboard(period: String = "season"): Flow<List<LeaderboardEntry>> = callbackFlow {
        val reg = db.collection("leaderboard").document(period).collection("entries")
            .orderBy("points", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snap, _ -> trySend(snap.toSafeList<LeaderboardEntry>()) }
        awaitClose { reg.remove() }
    }

    // ---------- Notifications ----------
    fun observeNotifications(uid: String): Flow<List<AppNotification>> = callbackFlow {
        if (uid.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val reg = db.collection("notifications").document(uid).collection("items")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ -> trySend(snap.toSafeList<AppNotification>()) }
        awaitClose { reg.remove() }
    }

    suspend fun markNotificationRead(uid: String, notifId: String) {
        db.collection("notifications").document(uid).collection("items")
            .document(notifId).update("read", true).await()
    }

    // ---------- Chat ----------
    fun observeChatMessages(chatId: String): Flow<List<ChatMessage>> = callbackFlow {
        if (chatId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val reg = db.collection("chats").document(chatId).collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .limitToLast(200)
            .addSnapshotListener { snap, _ -> trySend(snap.toSafeList<ChatMessage>()) }
        awaitClose { reg.remove() }
    }

    suspend fun sendChatMessage(chatId: String, message: ChatMessage) {
        val ref = db.collection("chats").document(chatId).collection("messages").document()
        db.collection("chats").document(chatId).collection("messages")
            .document(ref.id).set(message.copy(id = ref.id)).await()
    }

    // ---------- Player search (for direct player-to-player chat) ----------
    /** Finds a user by their exact in-game UID (uidGame field). */
    suspend fun findUserByGameUid(uidGame: String): UserProfile? = runCatching {
        if (uidGame.isBlank()) return@runCatching null
        val snap = db.collection("users")
            .whereEqualTo("uidGame", uidGame.trim())
            .limit(1)
            .get()
            .await()
        snap.documents.firstOrNull()?.toObject(UserProfile::class.java)
    }.getOrNull()

    // ---------- Payment methods (admin-managed QR codes / account details) ----------
    fun observePaymentMethods(): Flow<List<PaymentMethod>> = callbackFlow {
        // Client-side filter + sort avoids requiring a composite index on
        // (isActive, sortOrder), which otherwise silently breaks this listener
        // whenever the index hasn't been created in the Firebase console.
        val reg = db.collection("payment_methods")
            .addSnapshotListener { snap, _ ->
                val list: List<PaymentMethod> = snap.toSafeList()
                trySend(list.filter { it.isActive }.sortedBy { it.sortOrder })
            }
        awaitClose { reg.remove() }
    }
}
