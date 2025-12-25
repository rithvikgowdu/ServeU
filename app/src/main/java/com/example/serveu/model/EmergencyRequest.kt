package com.example.serveu.model

import com.google.firebase.database.IgnoreExtraProperties

/**
 * Data model for an emergency request stored in Firebase Realtime Database.
 */
@IgnoreExtraProperties
data class EmergencyRequest(
    val id: String? = null,
    val userPhoneNumber: String? = null, // Note: Cannot be retrieved reliably without specific permissions.
    val emergencyContactNumber: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timestamp: Long? = null
)
