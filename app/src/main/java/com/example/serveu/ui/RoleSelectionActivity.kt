package com.example.serveu.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.serveu.databinding.ActivityRoleSelectionBinding

class RoleSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoleSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoleSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAdmin.setOnClickListener {
            saveRole("admin")
            startActivity(Intent(this, SecretCodeActivity::class.java))
            finish()
        }

        binding.btnRegularUser.setOnClickListener {
            saveRole("user")
            startActivity(Intent(this, EmergencySetupActivity::class.java))
            finish()
        }
    }

    private fun saveRole(role: String) {
        val sharedPreferences = getSharedPreferences("ServeU", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("ROLE", role).apply()
    }
}