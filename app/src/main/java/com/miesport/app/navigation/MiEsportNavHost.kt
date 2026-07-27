package com.miesport.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.miesport.app.ui.components.MiBottomNavBar
import com.miesport.app.ui.screens.home.HomeScreen
import com.miesport.app.ui.screens.leaderboard.LeaderboardScreen
import com.miesport.app.ui.screens.live.LiveScreen
import com.miesport.app.ui.screens.login.LoginScreen
import com.miesport.app.ui.screens.notifications.NotificationsScreen
import com.miesport.app.ui.screens.profile.ProfileScreen
import com.miesport.app.ui.screens.registration.RegistrationScreen
import com.miesport.app.ui.screens.rewards.RewardsScreen
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

    val startDestination = if (FirebaseAuth.getInstance().currentUser != null)
        NavRoutes.Home.route else NavRoutes.Login.route

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
                startDestination = startDestination,
                modifier = Modifier.fillMaxSize()
            ) {
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

                composable(NavRoutes.TournamentDetail.route) { backStack ->
                    val id = backStack.arguments?.getString("tournamentId") ?: ""
                    TournamentDetailScreen(
                        tournamentId = id,
                        onRegisterClick = { navController.navigate(NavRoutes.Registration.build(id)) },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(NavRoutes.Registration.route) { backStack ->
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
                composable(NavRoutes.Teams.route) { TeamsScreen() }
                composable(NavRoutes.Rewards.route) { RewardsScreen() }
                composable(NavRoutes.Notifications.route) { NotificationsScreen() }
                composable(NavRoutes.Profile.route) {
                    ProfileScreen(
                        onSignedOut = {
                            navController.navigate(NavRoutes.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}
