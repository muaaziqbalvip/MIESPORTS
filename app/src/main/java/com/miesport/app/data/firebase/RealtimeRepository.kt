package com.miesport.app.data.firebase

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.miesport.app.data.model.LiveStatus
import com.miesport.app.data.model.Presence
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Realtime Database is used ONLY for fast-changing / low-latency data:
 * - /live_status/{tournamentId}: live match phase + YouTube video id
 * - /room_reveal/{tournamentId}: room ID + password, pushed at reveal time
 * - /presence/{uid}: online/offline
 *
 * Everything structured (tournament details, users, wallet, teams) stays in Firestore.
 */
class RealtimeRepository(
    private val rtdb: FirebaseDatabase = FirebaseDatabase.getInstance()
) {
    fun observeLiveStatus(tournamentId: String): Flow<LiveStatus?> = callbackFlow {
        val ref = rtdb.getReference("live_status").child(tournamentId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(LiveStatus::class.java))
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun setLiveStatus(tournamentId: String, status: LiveStatus) {
        rtdb.getReference("live_status").child(tournamentId).setValue(status).await()
    }

    /** Room ID/password revealed only shortly before match start (admin publishes it here). */
    fun observeRoomReveal(tournamentId: String): Flow<Map<String, String>?> = callbackFlow {
        val ref = rtdb.getReference("room_reveal").child(tournamentId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                @Suppress("UNCHECKED_CAST")
                trySend(snapshot.value as? Map<String, String>)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun publishRoomReveal(tournamentId: String, roomId: String, password: String) {
        rtdb.getReference("room_reveal").child(tournamentId)
            .setValue(mapOf("roomId" to roomId, "roomPassword" to password)).await()
    }

    fun setPresence(uid: String) {
        val ref = rtdb.getReference("presence").child(uid)
        ref.setValue(Presence(online = true, lastSeen = System.currentTimeMillis()))
        ref.onDisconnect().setValue(Presence(online = false, lastSeen = System.currentTimeMillis()))
    }
}
