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
                        .setApiKey("AIzaSyDummyKeyForYallaFirebase12345")
                        .setApplicationId("1:1234567890:android:abcdef123456789")
                        .setProjectId("zomatoarchitecture")
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
                    .setApiKey("AIzaSyDummyKeyForYallaFirebase12345")
                    .setApplicationId("1:1234567890:android:abcdef123456789")
                    .setProjectId("zomatoarchitecture")
                    .build()
                FirebaseApp.initializeApp(this, options)
                Log.d("YallaApplication", "FirebaseApp initialized with fallback options after exception")
            } catch (ex: Throwable) {
                Log.e("YallaApplication", "Fallback FirebaseApp init failed: ${ex.message}")
            }
        }
    }
}
