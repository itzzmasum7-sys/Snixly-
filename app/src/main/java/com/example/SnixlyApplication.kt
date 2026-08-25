package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class SnixlyApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    initFirebaseSafely()
  }

  private fun initFirebaseSafely() {
    try {
      if (FirebaseApp.getApps(this).isEmpty()) {
        try {
          FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
          Log.w("SnixlyApplication", "Default Firebase init skipped or failed: ${e.message}")
        }
      }

      if (FirebaseApp.getApps(this).isEmpty()) {
        val options = FirebaseOptions.Builder()
          .setApplicationId("1:966191723194:android:34e2d3a2eba2e372768883")
          .setApiKey("AIzaSyDW7HxbyDv8TTyp2sn2BF9Af-UPqKlqvDA")
          .setProjectId("snixly-2cfe0")
          .setStorageBucket("snixly-2cfe0.firebasestorage.app")
          .build()
        FirebaseApp.initializeApp(this, options)
        Log.i("SnixlyApplication", "Fallback FirebaseApp successfully initialized.")
      }
    } catch (e: Exception) {
      Log.e("SnixlyApplication", "FirebaseApp init error: ${e.message}", e)
    }
  }
}
