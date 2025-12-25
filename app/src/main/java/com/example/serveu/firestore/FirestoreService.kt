package com.example.serveu.firestore

import com.example.serveu.model.Emergency
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreService {

    private val db = FirebaseFirestore.getInstance()

    suspend fun saveEmergency(emergency: Emergency) {
        db.collection("emergency_requests").document(emergency.id).set(emergency).await()
    }

    // This function can be used later to update the status
    suspend fun updateEmergencyStatus(emergencyId: String, newStatus: String) {
        db.collection("emergency_requests").document(emergencyId).update("status", newStatus).await()
    }
}
