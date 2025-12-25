package com.example.serveu

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.serveu.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var selectedEmergency = "General SOS"
    private val emergencyNumber = "9440696941"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupEmergencyButtons()
        setupSendButton()
        checkLocationPermission()
    }
    private fun setupEmergencyButtons() {

        binding.btnBreakdown.setOnClickListener {
            selectedEmergency = "Vehicle Breakdown"
            binding.status.text = "Vehicle Breakdown selected"
        }

        binding.btnAccident.setOnClickListener {
            selectedEmergency = "Accident"
            binding.status.text = "Accident selected"
        }

        binding.btnMedical.setOnClickListener {
            selectedEmergency = "Medical Emergency"
            binding.status.text = "Medical Emergency selected"
        }

        binding.btnFuel.setOnClickListener {
            selectedEmergency = "Fuel / Battery Issue"
            binding.status.text = "Fuel / Battery Issue selected"
        }
    }


    // ---------------- SEND LOGIC ----------------

    private fun setupSendButton() {
        binding.startHelpBtn.setOnClickListener {
            if (isInternetAvailable()) {
                sendOnlineEmergency()
            } else {
                sendEmergencySms()
            }
        }
    }

    // ---------------- ONLINE MODE ----------------

    private fun sendOnlineEmergency() {

        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        val emergency = hashMapOf(
            "type" to selectedEmergency,
            "location" to binding.locationText.text.toString(),
            "timestamp" to System.currentTimeMillis(),
            "status" to "PENDING"
        )

        db.collection("emergencies")
            .add(emergency)
            .addOnSuccessListener {
                binding.status.text = "🌐 Emergency sent online"
                showAiGuidance()
                val intent = Intent(this, GeminiGuidanceActivity::class.java)
                intent.putExtra("EMERGENCY_TYPE", selectedEmergency)
                startActivity(intent)

            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Firebase failed, using SMS", Toast.LENGTH_SHORT).show()
                sendEmergencySms()
            }
    }


    // ---------------- OFFLINE MODE (SMS) ----------------

    private fun sendEmergencySms() {
        val message = """
            SERVEU ALERT 🚨
            Type: $selectedEmergency
            Location: ${binding.locationText.text}
            Please help immediately.
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("smsto:$emergencyNumber")
        intent.putExtra("sms_body", message)

        startActivity(intent)
        binding.status.text = "📩 SMS ready to send"
        showOfflineGuidance()
    }

    // ---------------- AI GUIDANCE ----------------

    private fun showAiGuidance() {
        val guidance = when (selectedEmergency) {
            "Accident" -> "Do not move injured person. Call emergency services."
            "Medical Emergency" -> "Check breathing. Keep patient calm."
            "Vehicle Breakdown" -> "Turn on hazard lights. Stay visible."
            else -> "Stay calm. Help is on the way."
        }

        AlertDialog.Builder(this)
            .setTitle("AI Safety Guidance")
            .setMessage(guidance)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showOfflineGuidance() {
        AlertDialog.Builder(this)
            .setTitle("Safety Instructions")
            .setMessage("Stay calm and safe until help arrives.")
            .setPositiveButton("OK", null)
            .show()
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
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) updateLocationUI(location)
        }
    }

    private fun updateLocationUI(location: Location) {
        val lat = String.format(Locale.US, "%.6f", location.latitude)
        val lng = String.format(Locale.US, "%.6f", location.longitude)
        binding.locationText.text = "$lat , $lng"
    }

    // ---------------- INTERNET CHECK ----------------

    private fun isInternetAvailable(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // ---------------- PERMISSIONS ----------------

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            fetchLocation()
        }
    }
}
