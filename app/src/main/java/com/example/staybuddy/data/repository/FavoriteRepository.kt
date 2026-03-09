package com.example.staybuddy.data.repository

import com.example.staybuddy.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    fun getFavoriteListingIds(): Flow<List<String>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = firestore.collection(Constants.FAVORITES_COLLECTION)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val ids = snapshot?.documents?.mapNotNull { it.getString("listingId") } ?: emptyList()
                trySend(ids)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addFavorite(listingId: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("Not logged in")
            val doc = hashMapOf(
                "userId" to userId,
                "listingId" to listingId,
                "createdAt" to System.currentTimeMillis()
            )
            firestore.collection(Constants.FAVORITES_COLLECTION)
                .add(doc)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFavorite(listingId: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("Not logged in")
            val docs = firestore.collection(Constants.FAVORITES_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("listingId", listingId)
                .get()
                .await()
            for (doc in docs) {
                doc.reference.delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
