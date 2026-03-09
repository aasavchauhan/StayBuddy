package com.example.staybuddy.data.repository

import com.example.staybuddy.data.model.ChatRoom
import com.example.staybuddy.data.model.Message
import com.example.staybuddy.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    fun getChats(): Flow<List<ChatRoom>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = firestore.collection(Constants.CHATS_COLLECTION)
            .whereArrayContains("participants", userId)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val chats = snapshot?.toObjects(ChatRoom::class.java) ?: emptyList()
                trySend(chats)
            }
        awaitClose { listener.remove() }
    }

    fun getMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val listener = firestore.collection(Constants.CHATS_COLLECTION)
            .document(chatId)
            .collection(Constants.MESSAGES_COLLECTION)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val messages = snapshot?.toObjects(Message::class.java) ?: emptyList()
                trySend(messages)
            }
        awaitClose { listener.remove() }
    }

    suspend fun sendMessage(chatId: String, text: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("Not logged in")
            val messageRef = firestore.collection(Constants.CHATS_COLLECTION)
                .document(chatId)
                .collection(Constants.MESSAGES_COLLECTION)
                .document()

            val message = Message(
                messageId = messageRef.id,
                senderId = userId,
                text = text,
                timestamp = System.currentTimeMillis()
            )
            messageRef.set(message).await()

            // Update chat room's last message
            firestore.collection(Constants.CHATS_COLLECTION)
                .document(chatId)
                .update(
                    mapOf(
                        "lastMessage" to text,
                        "lastMessageTime" to System.currentTimeMillis()
                    )
                ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createOrGetChat(otherUserId: String, listingId: String): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("Not logged in")

            // Check if chat already exists
            val existing = firestore.collection(Constants.CHATS_COLLECTION)
                .whereArrayContains("participants", userId)
                .get()
                .await()

            val existingChat = existing.documents.firstOrNull { doc ->
                val participants = doc.get("participants") as? List<*>
                participants?.contains(otherUserId) == true &&
                        doc.getString("listingId") == listingId
            }

            if (existingChat != null) {
                return Result.success(existingChat.id)
            }

            // Create new chat
            val docRef = firestore.collection(Constants.CHATS_COLLECTION).document()
            val chat = ChatRoom(
                chatId = docRef.id,
                participants = listOf(userId, otherUserId),
                listingId = listingId
            )
            docRef.set(chat).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
