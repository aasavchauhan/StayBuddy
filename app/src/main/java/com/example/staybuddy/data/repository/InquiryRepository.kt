package com.example.staybuddy.data.repository

import com.example.staybuddy.data.model.Inquiry
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
class InquiryRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun sendInquiry(inquiry: Inquiry): Result<String> {
        return try {
            val docRef = firestore.collection(Constants.INQUIRIES_COLLECTION).document()
            val inquiryWithId = inquiry.copy(inquiryId = docRef.id)
            docRef.set(inquiryWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getInquiriesForHost(hostId: String): Flow<List<Inquiry>> = callbackFlow {
        val listener = firestore.collection(Constants.INQUIRIES_COLLECTION)
            .whereEqualTo("hostId", hostId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot == null || snapshot.isEmpty) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val inquiries = snapshot.toObjects(Inquiry::class.java)
                trySend(inquiries)
            }
        awaitClose { listener.remove() }
    }

    fun getInquiriesForUser(userId: String): Flow<List<Inquiry>> = callbackFlow {
        val listener = firestore.collection(Constants.INQUIRIES_COLLECTION)
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot == null || snapshot.isEmpty) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val inquiries = snapshot.toObjects(Inquiry::class.java)
                trySend(inquiries)
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateInquiryStatus(inquiryId: String, status: String): Result<Unit> {
        return try {
            firestore.collection(Constants.INQUIRIES_COLLECTION)
                .document(inquiryId)
                .update("status", status)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
