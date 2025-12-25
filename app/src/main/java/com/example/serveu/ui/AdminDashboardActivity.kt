package com.example.serveu.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.serveu.adapter.EmergencyAdapter
import com.example.serveu.databinding.ActivityAdminDashboardBinding
import com.example.serveu.model.EmergencyRequest
import com.google.firebase.database.*

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var database: DatabaseReference
    private lateinit var adapter: EmergencyAdapter
    private val requestList = mutableListOf<EmergencyRequest>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().getReference("emergency_requests")

        setupRecyclerView()
        fetchRequests()
    }

    private fun setupRecyclerView() {
        adapter = EmergencyAdapter(requestList) { request ->
            deleteRequest(request)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun fetchRequests() {
        database.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val request = snapshot.getValue(EmergencyRequest::class.java)
                if (request != null) {
                    requestList.add(0, request) // Add to top of the list
                    adapter.notifyItemInserted(0)
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val removedRequest = snapshot.getValue(EmergencyRequest::class.java)
                val index = requestList.indexOfFirst { it.id == removedRequest?.id }
                if (index != -1) {
                    requestList.removeAt(index)
                    adapter.notifyItemRemoved(index)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun deleteRequest(request: EmergencyRequest) {
        request.id?.let {
            database.child(it).removeValue()
        }
    }
}
