package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class YallaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val app = FirebaseApp.initializeApp(this)
                if (app == null) {
                    val options = FirebaseOptions.Builder()
                        .setApiKey("AIzaSyAjiJ0_LG3GIfiwF_qfGWanYmyr4og23LI")
                        .setApplicationId("1:240483728829:android:c66f50511f9de091a8e6d4")
                        .setProjectId("advance-athlete-z224x")
                        .setStorageBucket("advance-athlete-z224x.firebasestorage.app")
                        .build()
                    FirebaseApp.initializeApp(this, options)
                    Log.d("YallaApplication", "FirebaseApp initialized with fallback options")
                } else {
                    Log.d("YallaApplication", "Default FirebaseApp initialized successfully")
                }
            }
        } catch (e: Throwable) {
            Log.e("YallaApplication", "Default FirebaseApp init failed: ${e.message}")
            try {
                val options = FirebaseOptions.Builder()
                    .setApiKey("AIzaSyAjiJ0_LG3GIfiwF_qfGWanYmyr4og23LI")
                    .setApplicationId("1:240483728829:android:c66f50511f9de091a8e6d4")
                    .setProjectId("advance-athlete-z224x")
                    .setStorageBucket("advance-athlete-z224x.firebasestorage.app")
                    .build()
                FirebaseApp.initializeApp(this, options)
                Log.d("YallaApplication", "FirebaseApp initialized with fallback options after exception")
            } catch (ex: Throwable) {
                Log.e("YallaApplication", "Fallback FirebaseApp init failed: ${ex.message}")
            }
        }
    }
}
