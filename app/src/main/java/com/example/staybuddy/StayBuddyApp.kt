package com.example.staybuddy

import android.app.Application
import android.content.Context
import com.example.staybuddy.notifications.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration

@HiltAndroidApp
class StayBuddyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize notification channels
        NotificationHelper.createNotificationChannels(this)
        
        // Initialize Osmdroid configuration
        Configuration.getInstance().load(
            this,
            getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = packageName
        
        // Initialize Cloudinary
        val config = mapOf(
            "cloud_name" to "dufri0nc6",
            "api_key" to "289581127678316",
            "api_secret" to "_UCbYliWoYp-IBPvwPs9HiJYZUw",
            "secure" to true
        )
        com.cloudinary.android.MediaManager.init(this, config)

        // Initialize Stream Chat
        val offlinePluginFactory = io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory(this)
        val statePluginFactory = io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory(
            config = io.getstream.chat.android.state.plugin.config.StatePluginConfig(),

            appContext = this
        )
        
        val chatClient = io.getstream.chat.android.client.ChatClient.Builder("h8n378wctxvm", this)
            .withPlugins(offlinePluginFactory, statePluginFactory)
            .build()
    }
}
