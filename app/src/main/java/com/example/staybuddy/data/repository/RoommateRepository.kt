package com.example.staybuddy.data.repository

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
class RoommateRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun getRoommatePosts(): Flow<List<RoommatePost>> = callbackFlow {
        val listener = firestore.collection(Constants.ROOMMATE_POSTS_COLLECTION)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val posts = snapshot?.toObjects(RoommatePost::class.java) ?: emptyList()
                trySend(posts)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addRoommatePost(post: RoommatePost): Result<String> {
        return try {
            val docRef = firestore.collection(Constants.ROOMMATE_POSTS_COLLECTION).document()
            val postWithId = post.copy(postId = docRef.id)
            docRef.set(postWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
