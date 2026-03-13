package com.example.staybuddy.data.repository

import com.example.staybuddy.data.model.User
import com.example.staybuddy.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import android.net.Uri
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.User as StreamUser
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val chatClient: ChatClient
) {
    val currentUser: FirebaseUser? get() = auth.currentUser

    suspend fun signUpWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveUserToFirestore(user: User): Result<Unit> {
        return try {
            firestore.collection(Constants.USERS_COLLECTION)
                .document(user.userId)
                .set(user)
                .await()
            
            // Connect to Stream Chat
            connectStreamUser(user)
            
            // Sync FCM token
            getAndSyncFcmToken()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun connectStreamUser(user: User) {
        val streamUser = StreamUser(
            id = user.userId,
            name = user.name,
            image = user.profileImage
        )
        
        // Using development token (API Secret needed for server-side, but 'Disable Auth Checks' allows any string or dev token)
        // For development with "Auth Checks Disabled", we can use any string. 
        // For development with "Auth Checks Enabled", we'd need a dev token or server token.
        // I will use a dummy token for now, assuming user disables auth checks as planned.
        val token = chatClient.devToken(user.userId) 
        
        chatClient.connectUser(streamUser, token).await()
    }

    suspend fun getAndSyncFcmToken() {
        try {
            val token = com.google.firebase.messaging.FirebaseMessaging.getInstance().token.await()
            updateFcmToken(token)
        } catch (e: Exception) {
            // Log or handle error
        }
    }

    suspend fun updateFcmToken(token: String) {
        val userId = auth.currentUser?.uid ?: return
        try {
            firestore.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .update("fcmToken", token)
                .await()
            
            // Also notify Stream Chat about the FCM token
            chatClient.addDevice(
                io.getstream.chat.android.models.Device(
                    token = token,
                    pushProvider = io.getstream.chat.android.models.PushProvider.FIREBASE,
                    providerName = null
                )
            ).await()
        } catch (e: Exception) {
            // Handle error, maybe the doc doesn't exist yet
        }
    }

    suspend fun updateUser(user: User): Result<Unit> {
        return try {
            firestore.collection(Constants.USERS_COLLECTION)
                .document(user.userId)
                .set(user) // Or update() if partial
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserFromFirestore(userId: String): Result<User?> {
        return try {
            val doc = firestore.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .get()
                .await()
            val user = doc.toObject(User::class.java)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadProfileImage(uri: Uri): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: UUID.randomUUID().toString()
            
            val secureUrl = kotlinx.coroutines.suspendCancellableCoroutine<String> { continuation ->
                val requestId = com.cloudinary.android.MediaManager.get().upload(uri)
                    .option("folder", "staybuddy/profile_images")
                    .option("public_id", "avatar_$userId") // Overwrite avatar for same user
                    .callback(object : com.cloudinary.android.callback.UploadCallback {
                        override fun onStart(requestId: String) {}
                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                        
                        override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                            val url = resultData["secure_url"] as String
                            continuation.resume(url) {}
                        }
                        
                        override fun onError(requestId: String, error: com.cloudinary.android.callback.ErrorInfo) {
                            continuation.resumeWithException(Exception(error.description))
                        }
                        
                        override fun onReschedule(requestId: String, error: com.cloudinary.android.callback.ErrorInfo) {}
                    })
                    .dispatch()
                    
                continuation.invokeOnCancellation {
                    com.cloudinary.android.MediaManager.get().cancelRequest(requestId)
                }
            }
            
            Result.success(secureUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
