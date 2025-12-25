package com.example.serveu.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.serveu.databinding.ActivityUserHomeBinding
import com.example.serveu.model.EmergencyRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

class UserHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserHomeBinding
    private val smsPermissionCode = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkSmsPermission()

        binding.btnChangeContact.setOnClickListener {
            startActivity(Intent(this, EmergencySetupActivity::class.java))
        }

        binding.btnEmergency.setOnClickListener {
            createEmergencyRequest()
        }
    }

    private fun checkSmsPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE), smsPermissionCode)
        }
    }

    private fun createEmergencyRequest() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Assuming location permission is already handled by the main app flow
            Toast.makeText(this, "Location permission not granted.", Toast.LENGTH_SHORT).show()
            return
        }

        val locationClient = LocationServices.getFusedLocationProviderClient(this)
        locationClient.lastLocation.addOnSuccessListener { location ->
            if (location == null) {
                Toast.makeText(this, "Could not get current location.", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            val prefs = getSharedPreferences("ServeU", MODE_PRIVATE)
            val emergencyContact = prefs.getString("EMERGENCY_NUMBER", "") ?: ""
            if (emergencyContact.isEmpty()) {
                Toast.makeText(this, "Please set an emergency contact first.", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, EmergencySetupActivity::class.java))
                return@addOnSuccessListener
            }
            val userPhone = "" // Cannot get user's number reliably, leave blank

            val request = EmergencyRequest(
                id = UUID.randomUUID().toString(),
                userPhoneNumber = userPhone,
                emergencyContact = emergencyContact,
                latitude = location.latitude,
                longitude = location.longitude,
                timestamp = System.currentTimeMillis()
            )

            if (isInternetAvailable()) {
                // Online Flow
                FirebaseDatabase.getInstance().getReference("emergency_requests")
                    .child(request.id)
                    .setValue(request)
                    .addOnSuccessListener { Toast.makeText(this, "Emergency request sent to admin.", Toast.LENGTH_SHORT).show() }
                    .addOnFailureListener { Toast.makeText(this, "Failed to send request. Check connection.", Toast.LENGTH_SHORT).show() }
            } else {
                // Offline Flow (SMS fallback)
                try {
                    val adminPhoneNumber = "9440696941" // IMPORTANT: Replace with actual Admin number
                    val smsMessage = "Emergency! Contact: $emergencyContact. Location: ${location.latitude},${location.longitude}"
                    
                    val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        getSystemService(SmsManager::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        SmsManager.getDefault()
                    }
                    smsManager.sendTextMessage(adminPhoneNumber, null, smsMessage, null, null)

                    Toast.makeText(this, "Offline SMS sent to admin.", Toast.LENGTH_SHORT).show()
                } catch (_: Exception) {
                    Toast.makeText(this, "Could not send SMS.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == smsPermissionCode && (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(this, "SMS permission is required for offline emergencies.", Toast.LENGTH_LONG).show()
        }
    }
}
