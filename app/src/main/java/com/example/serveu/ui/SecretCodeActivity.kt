package com.example.serveu.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.serveu.databinding.ActivitySecretCodeBinding

class SecretCodeActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySecretCodeBinding
    private val ADMIN_SECRET_CODE = "ADD_SECRET_CODE_HERE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecretCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSubmit.setOnClickListener {
            val secretCode = binding.etSecretCode.text.toString()
            if (secretCode == ADMIN_SECRET_CODE) {
                startActivity(Intent(this, AdminDashboardActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Invalid Secret Code", Toast.LENGTH_SHORT).show()
            }
        }
    }
}