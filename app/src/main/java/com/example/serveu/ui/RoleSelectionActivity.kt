package com.example.serveu.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.serveu.MainActivity
import com.example.serveu.databinding.ActivityRoleSelectionBinding

class RoleSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoleSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoleSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("ServeU_Prefs", Context.MODE_PRIVATE)

        // Check if a role is already selected
        when (prefs.getString("USER_ROLE", null)) {
            "ADMIN" -> {
                startActivity(Intent(this, SecretCodeActivity::class.java))
                finish()
                return
            }
            "USER" -> {
                // If user role is selected but contact is not set, go to setup
                if (prefs.getString("EMERGENCY_CONTACT", null).isNullOrBlank()) {
                    startActivity(Intent(this, EmergencySetupActivity::class.java))
                } else {
                    startActivity(Intent(this, MainActivity::class.java))
                }
                finish()
                return
            }
        }

        // If no role is selected, show the selection buttons
        binding.btnAdmin.setOnClickListener {
            prefs.edit().putString("USER_ROLE", "ADMIN").apply()
            startActivity(Intent(this, SecretCodeActivity::class.java))
            finish()
        }

        binding.btnRegularUser.setOnClickListener {
            prefs.edit().putString("USER_ROLE", "USER").apply()
            startActivity(Intent(this, EmergencySetupActivity::class.java))
            finish()
        }
    }
}
