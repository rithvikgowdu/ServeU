package com.example.serveu.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.serveu.MainActivity
import com.example.serveu.databinding.ActivityEmergencySetupBinding

class EmergencySetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmergencySetupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmergencySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSave.setOnClickListener {
            val emergencyNumber = binding.etEmergencyNumber.text.toString().trim()

            if (emergencyNumber.isEmpty()) {
                binding.etEmergencyNumber.error = "Enter phone number"
                return@setOnClickListener
            }

            if (emergencyNumber.length < 8) {
                binding.etEmergencyNumber.error = "Enter a valid phone number"
                return@setOnClickListener
            }
            if (!Patterns.PHONE.matcher(emergencyNumber).matches()) {
                binding.etEmergencyNumber.error = "Invalid phone number"
                return@setOnClickListener
            }
            saveEmergencyNumber(emergencyNumber)
            Toast.makeText(this, "Emergency number saved", Toast.LENGTH_SHORT).show()

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun saveEmergencyNumber(number: String) {
        val sharedPreferences = getSharedPreferences("ServeU", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putString("EMERGENCY_NUMBER", number)
            .apply()
    }
}
