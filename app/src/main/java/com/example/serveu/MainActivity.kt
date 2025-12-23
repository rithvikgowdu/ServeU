package com.example.serveu

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.serveu.databinding.ActivityMainBinding
import com.google.android.gms.location.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var selectedEmergency = "General SOS"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupEmergencyButtons()
        setupSendButton()

        checkLocationPermission()
    }

    // ---------------- UI SETUP ----------------

    private fun setupEmergencyButtons() {

        binding.btnBreakdown.setOnClickListener {
            selectedEmergency = "Vehicle Breakdown"
            binding.status.text = "🚗 Vehicle Breakdown selected"
        }

        binding.btnAccident.setOnClickListener {
            selectedEmergency = "Accident"
            binding.status.text = "🚨 Accident selected"
        }

        binding.btnMedical.setOnClickListener {
            selectedEmergency = "Medical Emergency"
            binding.status.text = "🩺 Medical Emergency selected"
        }

        binding.btnFuel.setOnClickListener {
            selectedEmergency = "Fuel / Battery Issue"
            binding.status.text = "⛽ Fuel / Battery Issue selected"
        }
    }

    private fun setupSendButton() {
        binding.startHelpBtn.setOnClickListener {
            sendEmergencyRequest()
        }
    }

    // ---------------- LOCATION ----------------

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
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

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                updateLocationUI(location)
            } else {
                binding.locationText.text = "📍 Unable to fetch GPS"
            }
        }
    }

    private fun updateLocationUI(location: Location) {
        val lat = String.format("%.6f", location.latitude)
        val lng = String.format("%.6f", location.longitude)
        binding.locationText.text = "📍 $lat , $lng"
    }

    // ---------------- EMERGENCY ACTION ----------------

    private fun sendEmergencyRequest() {
        fetchLocation()

        Toast.makeText(
            this,
            "Emergency sent: $selectedEmergency",
            Toast.LENGTH_SHORT
        ).show()

        binding.status.text =
            "✅ Emergency request sent\nType: $selectedEmergency"
    }

    // ---------------- PERMISSION RESULT ----------------

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
        } else {
            Toast.makeText(
                this,
                "Location permission required for ServeU",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
