package com.example.staybuddy.notifications

import com.example.staybuddy.notifications.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.staybuddy.data.repository.AuthRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StayBuddyMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var authRepository: AuthRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            authRepository.updateFcmToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        // Custom handling for other notifications
        val title = message.notification?.title ?: message.data["title"] ?: "StayBuddy"
        val body = message.notification?.body ?: message.data["body"] ?: "You have a new update."
        val channelId = message.data["channelId"] ?: NotificationHelper.CHANNEL_MESSAGES

        NotificationHelper.showNotification(
            context = this,
            channelId = channelId,
            title = title,
            message = body,
            notificationId = System.currentTimeMillis().toInt()
        )
    }
}
