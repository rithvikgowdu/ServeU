package com.example.serveu.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.serveu.MainActivity
import com.example.serveu.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val sharedPreferences = getSharedPreferences("ServeU", Context.MODE_PRIVATE)
            when (sharedPreferences.getString("ROLE", null)) {
                "admin" -> startActivity(Intent(this, SecretCodeActivity::class.java))
                "user" -> startActivity(Intent(this, MainActivity::class.java))
                else -> startActivity(Intent(this, RoleSelectionActivity::class.java))
            }
            finish()
        }, 1500)
    }
}