package com.example.serveu.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.serveu.databinding.ItemEmergencyRequestBinding
import com.example.serveu.model.EmergencyRequest

class EmergencyAdapter(
    private val emergencyRequests: List<EmergencyRequest>,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<EmergencyAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemEmergencyRequestBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(request: EmergencyRequest) {
            binding.tvUserPhoneNumber.text = "User Phone Number: ${request.userPhoneNumber}"
            binding.tvEmergencyContact.text = "Emergency Contact: ${request.emergencyContact}"
            binding.tvLocation.text = "Location: ${request.latitude}, ${request.longitude}"
            binding.tvTimestamp.text = "Timestamp: ${request.timestamp}"
            binding.btnDelete.setOnClickListener { onDeleteClick(request.id) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEmergencyRequestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(emergencyRequests[position])
    }

    override fun getItemCount() = emergencyRequests.size
}