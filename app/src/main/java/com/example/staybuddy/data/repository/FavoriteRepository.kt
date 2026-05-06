package com.example.staybuddy.data.repository

import com.example.staybuddy.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import com.example.staybuddy.data.local.FavoriteDao
import com.example.staybuddy.data.local.FavoriteEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.flowOf

@Singleton
class FavoriteRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val favoriteDao: FavoriteDao
) {
    private val repositoryScope = CoroutineScope(Dispatchers.IO)
    fun getFavoriteListingIds(): Flow<List<String>> {
        val userId = auth.currentUser?.uid ?: return flowOf(emptyList())
        
        // Start sync in background
        repositoryScope.launch {
            try {
                firestore.collection(Constants.FAVORITES_COLLECTION)
                    .whereEqualTo("userId", userId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            // Handle permission errors gracefully - don't crash
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val ids = snapshot.documents.mapNotNull { it.getString("listingId") }
                            repositoryScope.launch {
                                favoriteDao.clearFavorites(userId)
                                ids.forEach { id ->
                                    favoriteDao.insertFavorite(FavoriteEntity(id, userId))
                                }
                            }
                        }
                    }
            } catch (e: Exception) {
                // Ignore sync errors
            }
        }

        return favoriteDao.getFavoriteIds(userId)
    }

    suspend fun addFavorite(listingId: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("Not logged in")
            
            // Optimistic local update
            favoriteDao.insertFavorite(FavoriteEntity(listingId, userId))
            
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
            
            // Local update
            favoriteDao.deleteFavorite(listingId, userId)
            
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
