package com.example.serveu.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.serveu.adapter.EmergencyAdapter
import com.example.serveu.databinding.ActivityAdminDashboardBinding
import com.example.serveu.model.EmergencyRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var database: DatabaseReference
    private lateinit var adapter: EmergencyAdapter
    private val emergencyRequests = mutableListOf<EmergencyRequest>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().getReference("emergency_requests")

        adapter = EmergencyAdapter(emergencyRequests) { requestId ->
            deleteRequest(requestId)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        fetchEmergencyRequests()
    }

    private fun fetchEmergencyRequests() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                emergencyRequests.clear()
                for (requestSnapshot in snapshot.children) {
                    val request = requestSnapshot.getValue(EmergencyRequest::class.java)
                    if (request != null) {
                        request.id = requestSnapshot.key!!
                        emergencyRequests.add(request)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun deleteRequest(requestId: String) {
        database.child(requestId).removeValue()
    }
}