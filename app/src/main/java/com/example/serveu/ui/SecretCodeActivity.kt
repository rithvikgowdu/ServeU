package com.example.serveu.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.serveu.databinding.ActivitySecretCodeBinding

class SecretCodeActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySecretCodeBinding
    private companion object {
        private const val ADMIN_SECRET_CODE = "BOSS6969"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecretCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSubmitCode.setOnClickListener {
            val enteredCode = binding.etSecretCode.text.toString()
            if (enteredCode == ADMIN_SECRET_CODE) {
                startActivity(Intent(this, AdminDashboardActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Incorrect Code", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
