package com.miesport.app.data.model

import com.google.firebase.firestore.PropertyName

/** Firestore: users/{uid} */
data class UserProfile(
    val uid: String = "",
    val gamingName: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val uidGame: String = "", // in-game UID (PUBG/FreeFire/etc.)
    val region: String = "",
    val rank: String = "Unranked",
    val level: Int = 1,
    val wins: Int = 0,
    val matchesPlayed: Int = 0,
    val totalEarnings: Double = 0.0,
    val walletBalance: Double = 0.0,
    val bonusCoins: Int = 0,
    val teamId: String = "",
    @get:PropertyName("isBanned") @set:PropertyName("isBanned")
    var isBanned: Boolean = false,
    val fcmToken: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val badges: List<String> = emptyList()
)

enum class TournamentMode { SOLO, DUO, SQUAD }
enum class TournamentType { FREE, PAID }
enum class TournamentStatus { UPCOMING, LIVE, COMPLETED, CANCELLED }

/** Firestore: tournaments/{tournamentId} */
data class Tournament(
    val id: String = "",
    val title: String = "",
    val game: String = "PUBG Mobile",
    val bannerUrl: String = "",
    val mode: String = TournamentMode.SQUAD.name,
    val type: String = TournamentType.FREE.name,
    val status: String = TournamentStatus.UPCOMING.name,
    val entryFee: Double = 0.0,
    val prizePool: Double = 0.0,
    val prizeBreakdown: Map<String, Double> = emptyMap(), // "1st" -> amount, etc.
    val totalSlots: Int = 100,
    val filledSlots: Int = 0,
    val matchDateTime: Long = 0L,
    val roomId: String = "",
    val roomPassword: String = "",
    val roomVisibleAt: Long = 0L, // timestamp when room details become visible
    val rules: String = "",
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/** Firestore: tournaments/{id}/registrations/{regId} */
data class Registration(
    val id: String = "",
    val userId: String = "",
    val inGameName: String = "",
    val uidGame: String = "",
    val region: String = "",
    val teamName: String = "",
    val screenshotUrl: String = "",
    val paymentStatus: String = "PENDING", // PENDING, VERIFIED, REJECTED (for paid)
    val registeredAt: Long = System.currentTimeMillis()
)

/** Firestore: wallet_transactions/{txId} */
data class WalletTransaction(
    val id: String = "",
    val userId: String = "",
    val type: String = "", // DEPOSIT, WITHDRAW, ENTRY_FEE, PRIZE, BONUS
    val amount: Double = 0.0,
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val method: String = "", // JazzCash, EasyPaisa, Bank, etc.
    val referenceId: String = "",
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/** Firestore: teams/{teamId} */
data class Team(
    val id: String = "",
    val name: String = "",
    val logoUrl: String = "",
    val captainId: String = "",
    val members: List<String> = emptyList(), // list of uids
    val wins: Int = 0,
    val totalEarnings: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

/** Firestore: leaderboard entries are computed / cached under leaderboard/{period}/entries/{uid} */
data class LeaderboardEntry(
    val uid: String = "",
    val gamingName: String = "",
    val avatarUrl: String = "",
    val teamName: String = "",
    val wins: Int = 0,
    val earnings: Double = 0.0,
    val points: Int = 0,
    val rankPosition: Int = 0
)

enum class NotificationType { TOURNAMENT_REMINDER, ROOM_ID_ALERT, PRIZE_ALERT, ANNOUNCEMENT }

/** Firestore: notifications/{userId}/items/{notifId} */
data class AppNotification(
    val id: String = "",
    val type: String = NotificationType.ANNOUNCEMENT.name,
    val title: String = "",
    val body: String = "",
    val relatedId: String = "", // e.g. tournamentId
    val read: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/** RTDB: /live_status/{tournamentId} — fast-changing live match state */
data class LiveStatus(
    val tournamentId: String = "",
    val isLive: Boolean = false,
    val youtubeVideoId: String = "",
    val currentPhase: String = "", // e.g. "Waiting", "In Progress", "Finished"
    val updatedAt: Long = System.currentTimeMillis()
)

/** RTDB: /presence/{uid} — online status */
data class Presence(
    val online: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis()
)

/** Firestore: chats/{chatId}/messages/{messageId}
 * chatId formats: "team_{teamId}" for team chat, "support_{uid}" for player-support chat
 */
data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderAvatarUrl: String = "",
    val text: String = "",
    val imageUrl: String = "",
    val isAdmin: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/** Firestore: payment_methods/{methodId} — admin-managed QR codes / account details */
data class PaymentMethod(
    val id: String = "",
    val name: String = "", // e.g. "JazzCash", "EasyPaisa", "Bank Transfer"
    val accountTitle: String = "",
    val accountNumber: String = "",
    val qrCodeUrl: String = "",
    val instructions: String = "",
    val isActive: Boolean = true,
    val sortOrder: Int = 0
)
