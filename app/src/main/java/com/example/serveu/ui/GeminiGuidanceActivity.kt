package com.example.serveu

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.serveu.databinding.ActivityGeminiBinding
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject


class GeminiGuidanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGeminiBinding

    private val apiKey = "Type API Key Here" // optional for online mode

    private var mode: String = "OFFLINE"
    private var emergencyType: String = "Emergency"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1️⃣ Inflate UI first
        binding = ActivityGeminiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2️⃣ Read intent safely
        mode = intent?.getStringExtra("MODE") ?: "OFFLINE"
        emergencyType = intent?.getStringExtra("EMERGENCY_TYPE") ?: "Emergency"

        // 3️⃣ Configure UI
        if (mode == "OFFLINE") {
            setupOfflineUI()
        } else {
            setupOnlineUI()
        }
    }

    // ---------------- OFFLINE MODE ----------------

    private fun setupOfflineUI() {
        binding.inputText.isEnabled = false
        binding.inputText.hint = "Offline guidance only"
        binding.sendBtn.isEnabled = false
        binding.sendBtn.alpha = 0.5f

        binding.responseText.text =
            "🤖 AI Safety Guidance (Offline Mode)\n\n" + getOfflineGuidance(emergencyType)
    }

    private fun getOfflineGuidance(type: String): String {
        return when {
            type.contains("Accident", true) ->
                """
            We have received your emergency alert. Stay calm — we are with you.

            1. First, secure your own safety. Move away from traffic, fire, or any immediate danger.
            2. Do not move the injured person unless leaving them there would put their life at risk.
            3. If there is active bleeding, apply firm and continuous pressure using a clean cloth or your hand.
            4. Keep this app open and follow each instruction as it appears.
            5. Remain with the injured person, reassure them, and closely monitor their condition until responders arrive.
            """.trimIndent()

            type.contains("Medical", true) ->
                """
            Your medical emergency has been registered. We are guiding you step by step.

            1. Check whether the person is conscious and breathing normally.
            2. Help them remain calm and position them comfortably in a safe area.
            3. Do not give food, water, or medication unless specifically instructed by medical staff.
            4. Watch carefully for any changes in symptoms or behavior.
            5. Stay connected and continue following instructions until medical help reaches you.
            """.trimIndent()

            else ->
                """
            Your emergency request is active. Follow our guidance carefully.

            1. Move to a safe, well-lit, and visible location away from potential hazards.
            2. Use your phone only as needed to conserve battery for emergency communication.
            3. Keep this app open and stay attentive to further instructions.
            4. Inform us immediately if the situation changes or worsens.
            5. Remain calm and wait safely until assistance arrives.
            """.trimIndent()
        }
    }


    // ---------------- ONLINE MODE ----------------

    private fun setupOnlineUI() {

        // 1️⃣ Always show safety guidelines first (even online)
        binding.responseText.text =
            "🛟 Safety Guidance\n\n" + getOfflineGuidance(emergencyType)

        // 2️⃣ Then enhance with AI suggestions (if available)
        getAiGuidance(emergencyType)

        // 3️⃣ Enable user interaction
        binding.sendBtn.setOnClickListener {
            val userQuery = binding.inputText.text.toString().trim()
            if (userQuery.isNotEmpty()) {
                getAiGuidance(userQuery)
                binding.inputText.text?.clear()
            }
        }
    }


    private fun getAiGuidance(prompt: String) {
        lifecycleScope.launch {
            try {
                binding.responseText.append("\n\n🧑‍💻 You:\n$prompt")

                val client = OkHttpClient()

                val body = """
                {
                  "contents": [{
                    "parts":[{"text":"Give clear, calm safety guidance for: $prompt"}]
                  }]
                }
                """.trimIndent()

                val requestBody =
                    body.toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url(
                        "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=$apiKey"
                    )
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
                // ✅ Online fallback — NO repeated offline guidance
                binding.responseText.append(
                    "\n\n🚀 AI Assistant:\n" +
                            "Enhanced interactive guidance will be available in future versions.\n" +
                            "Please continue following the safety steps shown above."

                )
            }
        }
    }
}
