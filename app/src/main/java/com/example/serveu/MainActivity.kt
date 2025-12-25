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
import androidx.activity.result.contract.ActivityResultContracts
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
    private val adminPhoneNumber = "9440696941" // Admin number for offline SMS

    // Launcher for OFFLINE SMS to admin
    private val smsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // User returned from SMS app → now open AI guidance
            val intent = Intent(this, GeminiGuidanceActivity::class.java)
            intent.putExtra("MODE", "OFFLINE")
            intent.putExtra("EMERGENCY_TYPE", selectedEmergency)
            startActivity(intent)
        }
    
    // Launcher for ONLINE SMS to emergency contact
    private val onlineSmsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // User returned from SMS app → now open AI guidance
            val intent = Intent(this, GeminiGuidanceActivity::class.java)
            intent.putExtra("MODE", "ONLINE")
            intent.putExtra("EMERGENCY_TYPE", selectedEmergency)
            startActivity(intent)
        }

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

    private fun setupSendButton() {
        binding.startHelpBtn.setOnClickListener {
            if (isInternetAvailable()) {
                sendOnlineEmergency()
            } else {
                sendEmergencySms()
            }
        }
    }

    private fun sendOnlineEmergency() {
        val sharedPreferences = getSharedPreferences("ServeU", Context.MODE_PRIVATE)
        val emergencyContact = sharedPreferences.getString("EMERGENCY_NUMBER", null)

        if (emergencyContact.isNullOrEmpty()) {
            Toast.makeText(this, "Please set up an emergency contact first.", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, EmergencySetupActivity::class.java))
            return
        }

        if (currentLocation == null) {
            Toast.makeText(this, "Location not available. Retrying...", Toast.LENGTH_SHORT).show()
            fetchLocation()
            return
        }

        val database = FirebaseDatabase.getInstance().getReference("emergency_requests")
        val requestId = database.push().key ?: return

        val request = EmergencyRequest(
            userPhoneNumber = "", // Cannot get user's phone number.
            emergencyContact = emergencyContact,
            latitude = currentLocation!!.latitude,
            longitude = currentLocation!!.longitude,
            timestamp = System.currentTimeMillis()
        )

        database.child(requestId).setValue(request)
            .addOnSuccessListener {
                binding.status.text = "🌐 Emergency sent online. Sending SMS..."

                val message = """
                    SERVEU ALERT 🚨
                    Type: $selectedEmergency
                    Location: ${binding.locationText.text}
                    This is an emergency alert from a ServeU user. Please contact them or the authorities immediately.
                """.trimIndent()

                val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("smsto:$emergencyContact")
                    putExtra("sms_body", message)
                }
                
                Toast.makeText(this, "Please send the SMS to your emergency contact.", Toast.LENGTH_LONG).show()
                onlineSmsLauncher.launch(smsIntent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Online request failed, falling back to offline SMS to admin.", Toast.LENGTH_LONG).show()
                sendEmergencySms()
            }
    }

    private fun sendEmergencySms() {
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

        Toast.makeText(
            this,
            "You are offline. Please send the SMS to the admin. Safety guidance will appear after.",
            Toast.LENGTH_LONG
        ).show()

        smsLauncher.launch(smsIntent)
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        } else {
            fetchLocation()
        }
    }

    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                currentLocation = location
                updateLocationUI(location)
            } else {
                binding.locationText.text = "Location not available"
            }
        }
    }

    private fun updateLocationUI(location: Location) {
        val lat = String.format(Locale.US, "%.6f", location.latitude)
        val lng = String.format(Locale.US, "%.6f", location.longitude)
        binding.locationText.text = "$lat , $lng"
    }

    private fun isInternetAvailable(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchLocation()
        }
    }
}
