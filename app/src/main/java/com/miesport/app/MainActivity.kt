package com.miesport.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.miesport.app.navigation.MiEsportNavHost
import com.miesport.app.ui.theme.MiEsportTheme

class MainActivity : ComponentActivity() {

    // TODO: replace with your actual Web Client ID from Firebase Console
    // (Project settings -> General -> Web SDK config, or google-services.json "client_type":3 entry)
    private val webClientId = "REPLACE_WITH_FIREBASE_WEB_CLIENT_ID"

    private var onGoogleTokenReceived: ((String?) -> Unit)? = null

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            onGoogleTokenReceived?.invoke(account?.idToken)
        } catch (e: ApiException) {
            onGoogleTokenReceived?.invoke(null)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            MiEsportTheme {
                MiEsportNavHost(
                    onGoogleSignInRequested = { onTokenReceived ->
                        onGoogleTokenReceived = onTokenReceived
                        val client = com.miesport.app.data.firebase.AuthRepository()
                            .getGoogleSignInClient(this, webClientId)
                        googleSignInLauncher.launch(client.signInIntent)
                    }
                )
            }
        }
    }
}
