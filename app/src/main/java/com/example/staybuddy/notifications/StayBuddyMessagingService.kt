package com.example.staybuddy.notifications

import com.example.staybuddy.data.repository.AuthRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.client.notifications.handler.NotificationHandlerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StayBuddyMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var chatClient: ChatClient

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            authRepository.updateFcmToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        // Stream Chat handling
        if (io.getstream.chat.android.push.firebase.FirebaseMessagingDelegate.handleRemoteMessage(message)) {
            // Stream handled it (e.g., standard chat notification)
            return
        }

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
