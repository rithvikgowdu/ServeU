package com.example.serveu

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.serveu.databinding.ActivityGeminiBinding
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody



class GeminiGuidanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGeminiBinding

    private val apiKey = "AIzaSyAUyJD5DrUQ_52XoAdjLPGUmtqwSKuKvU4"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGeminiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val emergencyType = intent.getStringExtra("EMERGENCY_TYPE") ?: "Emergency"

        getGeminiGuidance(emergencyType)

        binding.sendBtn.setOnClickListener {
            val userQuery = binding.inputText.text.toString()
            if (userQuery.isNotBlank()) {
                getGeminiGuidance(userQuery)
                binding.inputText.text?.clear()
            }
        }
    }

    private fun getGeminiGuidance(prompt: String) {
        lifecycleScope.launch {
            try {
                binding.responseText.append("\n\n🧑‍💻 You: $prompt")

                val client = OkHttpClient()

                val body = """
                    {
                      "contents": [{
                        "parts":[{"text":"Give safety guidance for: $prompt"}]
                      }]
                    }
                """.trimIndent()

                val requestBody = body.toRequestBody(
                    "application/json".toMediaType()
                )


                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=$apiKey")
                    .post(requestBody)
                    .build()



                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                val json = JSONObject(responseBody)
                val text =
                    json.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")

                binding.responseText.append("\n\n🤖 Gemini:\n$text")

            } catch (e: Exception) {
                val fallback = when {
                    prompt.contains("Accident", true) ->
                        "Ensure safety, avoid moving injured persons, call emergency services."
                    prompt.contains("Medical", true) ->
                        "Check breathing, keep patient calm, avoid food or water."
                    else ->
                        "Stay calm, move to a safe location, help is on the way."
                }

                binding.responseText.append("\n\n🤖 AI (Fallback):\n$fallback")
            }

        }
    }
}
