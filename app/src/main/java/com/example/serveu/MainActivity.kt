package com.example.serveu

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.serveu.databinding.ActivityMainBinding
import com.example.serveu.firestore.FirestoreService
import com.example.serveu.model.Emergency
import com.example.serveu.model.EmergencyRequest
import com.example.serveu.ui.EmergencySetupActivity
import com.example.serveu.ui.RoleSelectionActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import android.location.LocationManager
import android.provider.Settings
import com.google.android.gms.location.Priority



class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Location? = null

    private var selectedEmergency = "General SOS"
    private val adminPhoneNumber = "9440696941"

    private val firestoreService = FirestoreService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check for emergency contact
        val prefs = getSharedPreferences("ServeU", Context.MODE_PRIVATE)
        val emergencyContact = prefs.getString("EMERGENCY_NUMBER", null)

        if (emergencyContact.isNullOrEmpty()) {
            startActivity(Intent(this, EmergencySetupActivity::class.java))
            finish() // Finish MainActivity so user can't go back
            return
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupEmergencyButtons()
        setupSendButton()
        checkLocationPermission()

        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    // ---------------- LOGOUT ----------------

    private fun logout() {
        val prefs = getSharedPreferences("ServeU", MODE_PRIVATE)
        prefs.edit().clear().apply()

        val intent = Intent(this, RoleSelectionActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // ---------------- EMERGENCY TYPE ----------------

    private fun setupEmergencyButtons() {
        binding.btnBreakdown.setOnClickListener { selectEmergency("Vehicle Breakdown") }
        binding.btnAccident.setOnClickListener { selectEmergency("Accident") }
        binding.btnMedical.setOnClickListener { selectEmergency("Medical Emergency") }
        binding.btnFuel.setOnClickListener { selectEmergency("Fuel / Battery Issue") }
    }

    private fun selectEmergency(type: String) {
        selectedEmergency = type
        binding.status.text = "$type selected"
    }

    // ---------------- SEND BUTTON ----------------

    private fun setupSendButton() {
        binding.startHelpBtn.setOnClickListener {

            // 🔴 Location service OFF
            if (!isLocationEnabled()) {
                Toast.makeText(
                    this,
                    "Please turn on location to continue",
                    Toast.LENGTH_LONG
                ).show()

                // Open location settings
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                return@setOnClickListener
            }

// 🔴 Location ON but not fetched yet
            if (currentLocation == null) {
                Toast.makeText(this, "Getting location…", Toast.LENGTH_SHORT).show()
                fetchLocation()
                return@setOnClickListener
            }


            val mode = if (isInternetAvailable()) "ONLINE" else "OFFLINE"

            // Show status immediately
            binding.status.text =
                if (mode == "ONLINE") "🌐 Emergency sent online"
                else "📴 Emergency sending offline…"

            // Open AI guidance screen
            val aiIntent = Intent(this, GeminiGuidanceActivity::class.java)
            aiIntent.putExtra("MODE", mode)
            aiIntent.putExtra("EMERGENCY_TYPE", selectedEmergency)
            startActivity(aiIntent)

            // Send emergency
            if (mode == "ONLINE") {
                sendOnlineEmergency()
            } else {
                sendOfflineSms()
            }
        }
    }

    // ---------------- ONLINE ----------------

    private fun sendOnlineEmergency() {
        val prefs = getSharedPreferences("ServeU", Context.MODE_PRIVATE)
        val emergencyContact = prefs.getString("EMERGENCY_NUMBER", null)!!

        val database = FirebaseDatabase.getInstance().getReference("emergency_requests")
        val requestId = database.push().key ?: return

        val request = EmergencyRequest(
            userPhoneNumber = "",
            emergencyContact = emergencyContact,
            latitude = currentLocation!!.latitude,
            longitude = currentLocation!!.longitude,
            timestamp = System.currentTimeMillis(),
            emergencyType = selectedEmergency
        )

        // Firestore (optional analytics/logs)
        val emergency = Emergency(
            id = requestId,
            emergencyType = selectedEmergency,
            description = "Emergency Contact: $emergencyContact",
            latitude = currentLocation!!.latitude,
            longitude = currentLocation!!.longitude,
            status = "pending"
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                firestoreService.saveEmergency(emergency)
            } catch (e: Exception) {
                Log.e("MainActivity", "Firestore error", e)
            }
        }

        // Realtime DB
        database.child(requestId).setValue(request)
            .addOnSuccessListener {

                val message = """
                    SERVEU ALERT 🚨
                    Type: $selectedEmergency
                    Location: ${binding.locationText.text}
                    Please help immediately.
                """.trimIndent()

                val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("smsto:$emergencyContact")
                    putExtra("sms_body", message)
                }

                startActivity(smsIntent)
            }
    }

    // ---------------- OFFLINE ----------------

    private fun sendOfflineSms() {
        val message = """
            SERVEU ALERT 🚨
            Type: $selectedEmergency
            Location: ${binding.locationText.text}
            Please help immediately.
        """.trimIndent()

        val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$adminPhoneNumber")
            putExtra("sms_body", message)
        }

        startActivity(smsIntent)
    }

    // ---------------- LOCATION ----------------

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
        } else {
            fetchLocation()
        }
    }

    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener { location ->
            if (location != null) {
                updateLocationUI(location)
            } else {
                Toast.makeText(
                    this,
                    "Unable to get location. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun updateLocationUI(location: Location) {
        currentLocation = location
        val lat = String.format(Locale.US, "%.6f", location.latitude)
        val lng = String.format(Locale.US, "%.6f", location.longitude)
        binding.locationText.text = "$lat , $lng"
    }

    // ---------------- INTERNET ----------------

    private fun isInternetAvailable(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    private fun isLocationEnabled(): Boolean {
        val locationManager =
            getSystemService(LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            fetchLocation()
        }
    }
}
