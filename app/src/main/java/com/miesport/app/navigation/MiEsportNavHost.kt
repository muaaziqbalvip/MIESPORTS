package com.miesport.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.miesport.app.ui.components.MiBottomNavBar
import com.miesport.app.ui.screens.chat.ChatScreen
import com.miesport.app.ui.screens.home.HomeScreen
import com.miesport.app.ui.screens.leaderboard.LeaderboardScreen
import com.miesport.app.ui.screens.live.LiveScreen
import com.miesport.app.ui.screens.login.LoginScreen
import com.miesport.app.ui.screens.notifications.NotificationsScreen
import com.miesport.app.ui.screens.playersearch.PlayerSearchScreen
import com.miesport.app.ui.screens.profile.ProfileScreen
import com.miesport.app.ui.screens.registration.RegistrationScreen
import com.miesport.app.ui.screens.rewards.RewardsScreen
import com.miesport.app.ui.screens.splash.SplashScreen
import com.miesport.app.ui.screens.support.SupportScreen
import com.miesport.app.ui.screens.teams.TeamsScreen
import com.miesport.app.ui.screens.tournament.TournamentDetailScreen
import com.miesport.app.ui.screens.tournament.TournamentListScreen
import com.miesport.app.ui.screens.wallet.WalletScreen

private val bottomNavRoutes = setOf(
    NavRoutes.Home.route,
    NavRoutes.TournamentList.route,
    NavRoutes.Live.route,
    NavRoutes.Wallet.route,
    NavRoutes.Profile.route
)

@Composable
fun MiEsportNavHost(
    onGoogleSignInRequested: (onToken: (String?) -> Unit) -> Unit
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomNavRoutes) {
                MiBottomNavBar(currentRoute = currentRoute) { route ->
                    navController.navigate(route) {
                        popUpTo(NavRoutes.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            }
        },
        containerColor = com.miesport.app.ui.theme.BackgroundBlack
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = NavRoutes.Splash.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(NavRoutes.Splash.route) {
                    SplashScreen(
                        onFinished = {
                            val destination = if (FirebaseAuth.getInstance().currentUser != null)
                                NavRoutes.Home.route else NavRoutes.Login.route
                            navController.navigate(destination) {
                                popUpTo(NavRoutes.Splash.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(NavRoutes.Login.route) {
                    val loginViewModel: com.miesport.app.ui.screens.login.LoginViewModel =
                        androidx.lifecycle.viewmodel.compose.viewModel()

                    LoginScreen(
                        viewModel = loginViewModel,
                        onGoogleSignInClick = {
                            onGoogleSignInRequested { idToken ->
                                if (idToken != null) {
                                    loginViewModel.signInWithGoogleToken(idToken)
                                }
                            }
                        },
                        onForgotPassword = { navController.navigate(NavRoutes.ForgotPassword.route) },
                        onLoginSuccess = {
                            navController.navigate(NavRoutes.Home.route) {
                                popUpTo(NavRoutes.Login.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(NavRoutes.ForgotPassword.route) {
                    com.miesport.app.ui.screens.login.ForgotPasswordScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(NavRoutes.Home.route) {
                    HomeScreen(
                        onTournamentClick = { id ->
                            navController.navigate(NavRoutes.TournamentDetail.build(id))
                        }
                    )
                }

                composable(NavRoutes.TournamentList.route) {
                    TournamentListScreen(
                        onTournamentClick = { id ->
                            navController.navigate(NavRoutes.TournamentDetail.build(id))
                        }
                    )
                }

                composable(
                    NavRoutes.TournamentDetail.route,
                    arguments = listOf(navArgument("tournamentId") { type = NavType.StringType })
                ) { backStack ->
                    val id = backStack.arguments?.getString("tournamentId") ?: ""
                    TournamentDetailScreen(
                        tournamentId = id,
                        onRegisterClick = { navController.navigate(NavRoutes.Registration.build(id)) },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    NavRoutes.Registration.route,
                    arguments = listOf(navArgument("tournamentId") { type = NavType.StringType })
                ) { backStack ->
                    val id = backStack.arguments?.getString("tournamentId") ?: ""
                    RegistrationScreen(
                        tournamentId = id,
                        onBack = { navController.popBackStack() },
                        onSuccess = { navController.popBackStack() }
                    )
                }

                composable(NavRoutes.Wallet.route) { WalletScreen() }
                composable(NavRoutes.Live.route) { LiveScreen() }
                composable(NavRoutes.Leaderboard.route) { LeaderboardScreen() }
                composable(NavRoutes.Teams.route) {
                    TeamsScreen(
                        onOpenTeamChat = { teamId ->
                            navController.navigate(NavRoutes.TeamChat.build(teamId))
                        }
                    )
                }
                composable(
                    NavRoutes.TeamChat.route,
                    arguments = listOf(navArgument("teamId") { type = NavType.StringType })
                ) { backStack ->
                    val teamId = backStack.arguments?.getString("teamId") ?: ""
                    ChatScreen(
                        chatId = "team_$teamId",
                        title = "Team Chat",
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(NavRoutes.Support.route) {
                    SupportScreen(
                        onOpenSupportChat = { navController.navigate(NavRoutes.SupportChat.route) }
                    )
                }
                composable(NavRoutes.SupportChat.route) {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
                    ChatScreen(
                        chatId = "support_$uid",
                        title = "Support Chat",
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(NavRoutes.PlayerSearch.route) {
                    PlayerSearchScreen(
                        onBack = { navController.popBackStack() },
                        onOpenChat = { targetUid, targetName ->
                            navController.navigate(NavRoutes.PlayerChat.build(targetUid, targetName))
                        }
                    )
                }
                composable(
                    NavRoutes.PlayerChat.route,
                    arguments = listOf(
                        navArgument("targetUid") { type = NavType.StringType },
                        navArgument("targetName") { type = NavType.StringType }
                    )
                ) { backStack ->
                    val targetUid = backStack.arguments?.getString("targetUid") ?: ""
                    val targetNameRaw = backStack.arguments?.getString("targetName") ?: "Player"
                    val targetName = runCatching {
                        java.net.URLDecoder.decode(targetNameRaw, "UTF-8")
                    }.getOrDefault(targetNameRaw)
                    val myUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    // Deterministic chat id so both users land in the same conversation
                    val chatId = "dm_" + listOf(myUid, targetUid).sorted().joinToString("_")
                    ChatScreen(
                        chatId = chatId,
                        title = targetName,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(NavRoutes.Rewards.route) { RewardsScreen() }
                composable(NavRoutes.Notifications.route) { NotificationsScreen() }
                composable(NavRoutes.Profile.route) {
                    ProfileScreen(
                        onSignedOut = {
                            navController.navigate(NavRoutes.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onOpenSupport = { navController.navigate(NavRoutes.Support.route) },
                        onEditProfile = { navController.navigate(NavRoutes.EditProfile.route) },
                        onFindPlayers = { navController.navigate(NavRoutes.PlayerSearch.route) }
                    )
                }
                composable(NavRoutes.EditProfile.route) {
                    com.miesport.app.ui.screens.profile.EditProfileScreen(
                        onBack = { navController.popBackStack() },
                        onSaved = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
