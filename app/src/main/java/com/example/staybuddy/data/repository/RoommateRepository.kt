package com.example.staybuddy.data.repository

import com.example.staybuddy.data.model.RoommatePost
import com.example.staybuddy.data.model.RoommatePostType
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
    fun getRoommatePosts(type: RoommatePostType? = null): Flow<List<RoommatePost>> = callbackFlow {
        val baseQuery = firestore.collection(Constants.ROOMMATE_POSTS_COLLECTION)
        val query = if (type != null) {
            baseQuery.whereEqualTo("postType", type.name)
        } else {
            baseQuery
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val posts = snapshot?.toObjects(RoommatePost::class.java)
                ?.sortedByDescending { it.createdAt } ?: emptyList()
            trySend(posts)
        }
        awaitClose { listener.remove() }
    }

    suspend fun getRoommatePostById(postId: String): Result<RoommatePost?> {
        return try {
            val doc = firestore.collection(Constants.ROOMMATE_POSTS_COLLECTION)
                .document(postId)
                .get()
                .await()
            Result.success(doc.toObject(RoommatePost::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
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

    suspend fun updateRoommatePost(post: RoommatePost): Result<Unit> {
        return try {
            firestore.collection(Constants.ROOMMATE_POSTS_COLLECTION)
                .document(post.postId)
                .set(post)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun decrementAvailableBeds(postId: String): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val postRef = firestore.collection(Constants.ROOMMATE_POSTS_COLLECTION).document(postId)
                val post = transaction.get(postRef).toObject(RoommatePost::class.java)
                    ?: throw Exception("Post not found")
                
                if (post.availableBeds > 0) {
                    val newAvailable = post.availableBeds - 1
                    transaction.update(postRef, "availableBeds", newAvailable)
                    if (newAvailable == 0) {
                        transaction.update(postRef, "isActive", false)
                    }
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
