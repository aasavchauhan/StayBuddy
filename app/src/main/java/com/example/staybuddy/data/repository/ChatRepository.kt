package com.example.staybuddy.data.repository

import com.example.staybuddy.data.model.ChatRoom
import com.example.staybuddy.data.model.Message
import com.example.staybuddy.data.model.RoommatePost
import com.example.staybuddy.utils.Constants
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
    private val firestore: FirebaseFirestore
) {
    fun getUserChatRooms(userId: String): Flow<List<ChatRoom>> = callbackFlow {
        val listener = firestore.collection(Constants.CHATS_COLLECTION)
            .whereArrayContains("participants", userId)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val rooms = snapshot?.toObjects(ChatRoom::class.java) ?: emptyList()
                trySend(rooms)
            }
        awaitClose { listener.remove() }
    }
    
    suspend fun getChatRoom(chatId: String): Result<ChatRoom?> {
        return try {
            val doc = firestore.collection(Constants.CHATS_COLLECTION).document(chatId).get().await()
            Result.success(doc.toObject(ChatRoom::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getChatRoomFlow(chatId: String): Flow<ChatRoom?> = callbackFlow {
        val listener = firestore.collection(Constants.CHATS_COLLECTION)
            .document(chatId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val room = snapshot?.toObject(ChatRoom::class.java)
                trySend(room)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getOrCreateChatRoom(
        user1Id: String, 
        user2Id: String, 
        listingId: String? = null,
        roommatePostId: String? = null
    ): Result<String> {
        return try {
            val existingRooms = firestore.collection(Constants.CHATS_COLLECTION)
                .whereArrayContains("participants", user1Id)
                .get()
                .await()
                .toObjects(ChatRoom::class.java)
                
            val room = existingRooms.find { it.participants.contains(user2Id) && it.roommatePostId == (roommatePostId ?: "") }
            
            if (room != null) {
                Result.success(room.roomId)
            } else {
                val docRef = firestore.collection(Constants.CHATS_COLLECTION).document()
                val newRoom = ChatRoom(
                    roomId = docRef.id,
                    participants = listOf(user1Id, user2Id),
                    listingId = listingId ?: "",
                    roommatePostId = roommatePostId ?: "",
                    lastMessage = "",
                    lastMessageTime = System.currentTimeMillis()
                )
                docRef.set(newRoom).await()
                Result.success(docRef.id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getMessagesForRoom(roomId: String): Flow<List<Message>> = callbackFlow {
        val listener = firestore.collection(Constants.CHATS_COLLECTION)
            .document(roomId)
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

    suspend fun confirmMatch(chatId: String, userId: String): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val roomRef = firestore.collection(Constants.CHATS_COLLECTION).document(chatId)
                val room = transaction.get(roomRef).toObject(ChatRoom::class.java)
                    ?: throw Exception("Chat not found")
                
                if (!room.confirmedBy.contains(userId)) {
                    val updatedConfirmedBy = room.confirmedBy + userId
                    val isConfirmed = updatedConfirmedBy.size >= 2
                    
                    transaction.update(roomRef, "confirmedBy", updatedConfirmedBy)
                    if (isConfirmed) {
                        transaction.update(roomRef, "isMatchConfirmed", true)
                        
                        // If it's a roommate match, decrement beds
                        if (room.roommatePostId.isNotEmpty()) {
                            val postRef = firestore.collection(Constants.ROOMMATE_POSTS_COLLECTION)
                                .document(room.roommatePostId)
                            val post = transaction.get(postRef).toObject(RoommatePost::class.java)
                            if (post != null && post.availableBeds > 0) {
                                val newAvailable = post.availableBeds - 1
                                transaction.update(postRef, "availableBeds", newAvailable)
                                if (newAvailable == 0) {
                                    transaction.update(postRef, "isActive", false)
                                }
                            }
                        }
                    }
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendMessage(roomId: String, senderId: String, text: String): Result<Unit> {
        return try {
            val messageRef = firestore.collection(Constants.CHATS_COLLECTION)
                .document(roomId)
                .collection(Constants.MESSAGES_COLLECTION)
                .document()
                
            val message = Message(
                messageId = messageRef.id,
                roomId = roomId,
                senderId = senderId,
                text = text,
                timestamp = System.currentTimeMillis(),
                isRead = false
            )
            
            firestore.runTransaction { transaction ->
                val roomRef = firestore.collection(Constants.CHATS_COLLECTION).document(roomId)
                
                // Write message
                transaction.set(messageRef, message)
                
                // Update room lastMessage
                transaction.update(
                    roomRef,
                    mapOf(
                        "lastMessage" to text,
                        "lastMessageTime" to message.timestamp
                    )
                )
            }.await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
