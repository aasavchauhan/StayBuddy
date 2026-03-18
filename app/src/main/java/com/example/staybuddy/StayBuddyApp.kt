package com.example.staybuddy

import android.app.Application
import android.content.Context
import com.example.staybuddy.notifications.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration

@HiltAndroidApp
class StayBuddyApp : Application() {
    override fun onCreate() {
        android.util.Log.d("StayBuddyApp", "onCreate: PRE-INITIALIZATION (Entry Point)")
        
        // Final fallback to capture any crash that prevents the app from opening
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("STAYBUDDY_FATAL", "FATAL EXCEPTION on thread ${thread.name}", throwable)
            // Still let the app crash so we don't end up in an undefined state
        }

        super.onCreate()
        android.util.Log.d("StayBuddyApp", "onCreate: Starting initialization")
        
        try {
            // Initialize notification channels as early as possible
            NotificationHelper.createNotificationChannels(this)
            android.util.Log.d("StayBuddyApp", "onCreate: Notification channels created")
            
            // Initialize Osmdroid configuration
            Configuration.getInstance().load(
                this,
                getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
            )
            Configuration.getInstance().userAgentValue = packageName
            android.util.Log.d("StayBuddyApp", "onCreate: Osmdroid initialized")
            
            // Initialize Cloudinary safely
            try {
                val config = mapOf(
                    "cloud_name" to "dufri0nc6",
                    "api_key" to "289581127678316",
                    "api_secret" to "_UCbYliWoYp-IBPvwPs9HiJYZUw",
                    "secure" to true
                )
                com.cloudinary.android.MediaManager.init(this, config)
                android.util.Log.d("StayBuddyApp", "onCreate: Cloudinary initialized")
            } catch (e: IllegalStateException) {
                android.util.Log.w("StayBuddyApp", "onCreate: Cloudinary already initialized")
            }
            
            android.util.Log.d("StayBuddyApp", "onCreate: Initialization complete")
        } catch (t: Throwable) {
            // Catching Throwable to handle even severe initialization errors from libraries
            android.util.Log.e("StayBuddyApp", "Critical initialization error", t)
        }
    }
}
