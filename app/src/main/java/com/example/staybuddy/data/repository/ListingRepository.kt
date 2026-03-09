package com.example.staybuddy.data.repository

import com.example.staybuddy.data.model.PgListing
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
class ListingRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun getListings(): Flow<List<PgListing>> = callbackFlow {
        val listener = firestore.collection(Constants.PG_LISTINGS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val listings = snapshot?.toObjects(PgListing::class.java)
                    ?.sortedByDescending { it.createdAt } ?: emptyList()
                trySend(listings)
            }
        awaitClose { listener.remove() }
    }

    fun getListingsByCity(city: String): Flow<List<PgListing>> = callbackFlow {
        val listener = firestore.collection(Constants.PG_LISTINGS_COLLECTION)
            .whereEqualTo("city", city)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val listings = snapshot?.toObjects(PgListing::class.java) ?: emptyList()
                trySend(listings)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getListingById(listingId: String): Result<PgListing?> {
        return try {
            val doc = firestore.collection(Constants.PG_LISTINGS_COLLECTION)
                .document(listingId)
                .get()
                .await()
            Result.success(doc.toObject(PgListing::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getListingsByOwner(ownerId: String): Flow<List<PgListing>> = callbackFlow {
        val listener = firestore.collection(Constants.PG_LISTINGS_COLLECTION)
            .whereEqualTo("ownerId", ownerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val listings = snapshot?.toObjects(PgListing::class.java)
                    ?.sortedByDescending { it.createdAt } ?: emptyList()
                trySend(listings)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addListing(listing: PgListing): Result<String> {
        return try {
            val docRef = firestore.collection(Constants.PG_LISTINGS_COLLECTION).document()
            val listingWithId = listing.copy(listingId = docRef.id)
            docRef.set(listingWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateListing(listing: PgListing): Result<Unit> {
        return try {
            firestore.collection(Constants.PG_LISTINGS_COLLECTION)
                .document(listing.listingId)
                .set(listing)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteListing(listingId: String): Result<Unit> {
        return try {
            firestore.collection(Constants.PG_LISTINGS_COLLECTION)
                .document(listingId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
