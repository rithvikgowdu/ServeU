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

        val prefs = getSharedPreferences("ServeU_Prefs", Context.MODE_PRIVATE)

        // Pre-fill the contact if it already exists
        binding.etEmergencyContact.setText(prefs.getString("EMERGENCY_CONTACT", ""))

        binding.btnSaveContact.setOnClickListener {
            val contactNumber = binding.etEmergencyContact.text.toString()
            if (contactNumber.isNotBlank()) {
                prefs.edit().putString("EMERGENCY_CONTACT", contactNumber).apply()
                
                // Redirect to the main user screen
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }
}
