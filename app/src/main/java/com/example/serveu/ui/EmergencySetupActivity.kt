package com.example.serveu.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
            val emergencyNumber = binding.etEmergencyNumber.text.toString()
            if (emergencyNumber.isNotEmpty()) {
                saveEmergencyNumber(emergencyNumber)
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }

    private fun saveEmergencyNumber(number: String) {
        val sharedPreferences = getSharedPreferences("ServeU", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("EMERGENCY_NUMBER", number).apply()
    }
}