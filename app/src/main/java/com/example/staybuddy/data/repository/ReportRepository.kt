package com.example.staybuddy.data.repository

import com.example.staybuddy.data.model.Report
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val reportsCollection = firestore.collection("reports")
    private val listingsCollection = firestore.collection("listings")

    suspend fun reportListing(
        listingId: String,
        reason: String,
        details: String = ""
    ): Result<Unit> = runCatching {
        val userId = auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in to report")

        val report = Report(
            listingId = listingId,
            reporterId = userId,
            reason = reason,
            details = details
        )

        // Save report document
        val docRef = reportsCollection.document()
        val reportWithId = report.copy(reportId = docRef.id)
        docRef.set(reportWithId).await()

        // Increment reportCount on the listing
        listingsCollection.document(listingId)
            .update("reportCount", FieldValue.increment(1))
            .await()

        // Auto-deactivate if >= 5 reports
        val listingDoc = listingsCollection.document(listingId).get().await()
        val currentCount = listingDoc.getLong("reportCount") ?: 0
        if (currentCount >= 5) {
            listingsCollection.document(listingId)
                .update("isActive", false)
                .await()
        }
    }

    suspend fun hasUserReported(listingId: String): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        val snapshot = reportsCollection
            .whereEqualTo("listingId", listingId)
            .whereEqualTo("reporterId", userId)
            .limit(1)
            .get()
            .await()
        return !snapshot.isEmpty
    }
}
