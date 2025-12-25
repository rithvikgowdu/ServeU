package com.example.serveu.model

data class EmergencyRequest(
    val userPhoneNumber: String = "",
    val emergencyContact: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = 0L,
    var id: String = ""
)
