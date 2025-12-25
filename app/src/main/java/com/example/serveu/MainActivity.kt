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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.serveu.databinding.ActivityMainBinding
import com.example.serveu.model.EmergencyRequest
import com.example.serveu.ui.EmergencySetupActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.FirebaseDatabase
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Location? = null

    private var selectedEmergency = "General SOS"
    private val adminPhoneNumber = "9440696941"

    // 🔒 STATE LOCK
    private var emergencySent = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupEmergencyButtons()
        setupSendButton()
        checkLocationPermission()
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
        emergencySent = false   // reset only when user chooses again
        binding.status.text = "$type selected"
    }


    // ---------------- SEND BUTTON ----------------

    private fun setupSendButton() {
        binding.startHelpBtn.setOnClickListener {

            if (currentLocation == null) {
                Toast.makeText(this, "Getting location…", Toast.LENGTH_SHORT).show()
                fetchLocation()
                return@setOnClickListener
            }

            val mode = if (isInternetAvailable()) "ONLINE" else "OFFLINE"

            // ✅ 1️⃣ SET STATUS IMMEDIATELY (VISIBLE)
            binding.status.text =
                if (mode == "ONLINE") "🌐 Emergency sent online"
                else "📴 Emergency sending offline…"

            // ✅ 2️⃣ OPEN AI SCREEN
            val aiIntent = Intent(this, GeminiGuidanceActivity::class.java)
            aiIntent.putExtra("MODE", mode)
            aiIntent.putExtra("EMERGENCY_TYPE", selectedEmergency)
            startActivity(aiIntent)

            // ✅ 3️⃣ DO BACKGROUND WORK (NO UI DEPENDENCY)
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
        val emergencyContact = prefs.getString("EMERGENCY_NUMBER", null)

        if (emergencyContact.isNullOrEmpty()) {
            startActivity(Intent(this, EmergencySetupActivity::class.java))
            return
        }

        val database = FirebaseDatabase.getInstance().getReference("emergency_requests")
        val requestId = database.push().key ?: return

        val request = EmergencyRequest(
            userPhoneNumber = "",
            emergencyContact = emergencyContact,
            latitude = currentLocation!!.latitude,
            longitude = currentLocation!!.longitude,
            timestamp = System.currentTimeMillis()
        )

        database.child(requestId).setValue(request)
            .addOnSuccessListener {
                emergencySent = true
                binding.status.text = "🌐 Emergency sent online"

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
        emergencySent = true
        binding.status.text = "📴 Emergency sent offline"

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

    // ---------------- AI SCREEN ----------------

    private fun openAiGuidance(mode: String) {
        val intent = Intent(this, GeminiGuidanceActivity::class.java)
        intent.putExtra("MODE", mode)
        intent.putExtra("EMERGENCY_TYPE", selectedEmergency)
        startActivity(intent)
    }

    // ---------------- LOCATION ----------------

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100
            )
        } else {
            fetchLocation()
        }
    }

    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.lastLocation.addOnSuccessListener {
            if (it != null) updateLocationUI(it)
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
