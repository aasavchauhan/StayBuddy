package com.example.staybuddy.data.repository

import com.example.staybuddy.data.local.PgListingEntity
import com.example.staybuddy.data.local.PropertyDao
import com.example.staybuddy.data.local.toDomainModel
import com.example.staybuddy.data.local.toEntity
import com.example.staybuddy.data.model.PgListing
import com.example.staybuddy.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListingRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val propertyDao: PropertyDao
) {
    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    fun getListings(): Flow<List<PgListing>> = channelFlow {
        // First, emit local data from Room if available
        val localEntities = propertyDao.getAllPropertyListings().first()
        if (localEntities.isNotEmpty()) {
            send(localEntities.map { it.toDomainModel() })
        }

        // Then, set up Firestore listener for live updates and syncing
        val listener = firestore.collection(Constants.PG_LISTINGS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Don't close the channel on error if we've already sent local data
                    // This allows the app to continue working offline
                    _isOffline.value = true
                    return@addSnapshotListener
                }
                
                _isOffline.value = snapshot?.metadata?.isFromCache == true
                
                val listings = snapshot?.toObjects(PgListing::class.java)
                    ?.sortedByDescending { it.createdAt } ?: emptyList()
                
                if (listings.isNotEmpty()) {
                    // Update Room in background
                    CoroutineScope(Dispatchers.IO).launch {
                        propertyDao.insertPropertyListings(listings.map { it.toEntity() })
                    }
                    trySend(listings)
                }
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
            // Check Room first
            val local = propertyDao.getPropertyListingById(listingId)
            if (local != null) {
                return Result.success(local.toDomainModel())
            }
            
            // If not in Room, try Firestore
            val doc = firestore.collection(Constants.PG_LISTINGS_COLLECTION)
                .document(listingId)
                .get()
                .await()
            val listing = doc.toObject(PgListing::class.java)
            
            // Cache it if found
            listing?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    propertyDao.insertPropertyListings(listOf(it.toEntity()))
                }
            }
            
            Result.success(listing)
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

    // Room local data
    fun getLocalListings(): Flow<List<PgListing>> = propertyDao.getAllPropertyListings().map { entities ->
        entities.map { it.toDomainModel() }
    }

    fun searchLocalListings(query: String): Flow<List<PgListing>> = propertyDao.searchPropertyListings(query).map { entities ->
        entities.map { it.toDomainModel() }
    }
}
