package com.example.serveu.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.serveu.MainActivity
import com.example.serveu.databinding.ActivityRoleSelectionBinding

class RoleSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoleSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // SharedPreferences
        val prefs = getSharedPreferences("ServeU", MODE_PRIVATE)

        // 🔁 AUTO-REDIRECT IF ROLE ALREADY CHOSEN
        when (prefs.getString("ROLE", null)) {
            "ADMIN" -> {
                startActivity(Intent(this, AdminDashboardActivity::class.java))
                finish()
                return
            }
            "USER" -> {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                return
            }
        }

        // 🔹 No role yet → show UI
        binding = ActivityRoleSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ---------------- BUTTONS ----------------

        binding.btnAdmin.setOnClickListener {
            startActivity(Intent(this, SecretCodeActivity::class.java))
        }

        binding.btnRegularUser.setOnClickListener {
            prefs.edit().putString("ROLE", "USER").apply()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
