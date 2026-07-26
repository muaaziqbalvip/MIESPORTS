package com.miesport.app.navigation

sealed class NavRoutes(val route: String) {
    object Splash : NavRoutes("splash")
    object Login : NavRoutes("login")
    object ForgotPassword : NavRoutes("forgot_password")

    object Home : NavRoutes("home")
    object TournamentList : NavRoutes("tournament_list")
    object TournamentDetail : NavRoutes("tournament_detail/{tournamentId}") {
        fun build(id: String) = "tournament_detail/$id"
    }
    object Registration : NavRoutes("registration/{tournamentId}") {
        fun build(id: String) = "registration/$id"
    }

    object Wallet : NavRoutes("wallet")
    object Leaderboard : NavRoutes("leaderboard")
    object Teams : NavRoutes("teams")
    object TeamDetail : NavRoutes("team_detail/{teamId}") {
        fun build(id: String) = "team_detail/$id"
    }
    object Live : NavRoutes("live")
    object Rewards : NavRoutes("rewards")
    object Profile : NavRoutes("profile")
    object Notifications : NavRoutes("notifications")

    // Admin
    object AdminDashboard : NavRoutes("admin_dashboard")
    object AdminCreateTournament : NavRoutes("admin_create_tournament")
}

/** Bottom nav destinations only. */
enum class BottomNavItem(val route: String, val label: String) {
    HOME(NavRoutes.Home.route, "Home"),
    TOURNAMENT(NavRoutes.TournamentList.route, "Tournament"),
    LIVE(NavRoutes.Live.route, "Live"),
    WALLET(NavRoutes.Wallet.route, "Wallet"),
    PROFILE(NavRoutes.Profile.route, "Profile")
}
