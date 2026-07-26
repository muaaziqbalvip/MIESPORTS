package com.miesport.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.initialize

class MiEsportApp : Application() {
    override fun onCreate() {
        super.onCreate()

        Firebase.initialize(this)

        // Play Integrity in release builds protects Firestore/RTDB/Storage from abuse.
        // Debug provider is used automatically for debug builds (register the printed
        // debug token in Firebase Console > App Check during development).
        Firebase.appCheck.installAppCheckProviderFactory(
            if (BuildConfig.DEBUG) {
                DebugAppCheckProviderFactory.getInstance()
            } else {
                PlayIntegrityAppCheckProviderFactory.getInstance()
            }
        )

        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "mi_esport_default",
                "MI ESPORT Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Tournament reminders, room ID alerts, prize alerts"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
