package com.example.serveu.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.serveu.R
import com.example.serveu.model.EmergencyRequest
import java.text.SimpleDateFormat
import java.util.*

class EmergencyAdapter(
    private val requests: MutableList<EmergencyRequest>,
    private val onDeleteClick: (EmergencyRequest) -> Unit
) : RecyclerView.Adapter<EmergencyAdapter.EmergencyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmergencyViewHolder {
        val view = LayoutInflater.from(parent.context).
            inflate(R.layout.item_emergency_request, parent, false)
        return EmergencyViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmergencyViewHolder, position: Int) {
        val request = requests[position]
        holder.bind(request)
    }

    override fun getItemCount(): Int = requests.size

    inner class EmergencyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvContact: TextView = itemView.findViewById(R.id.tvEmergencyContact)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        private val btnDelete: Button = itemView.findViewById(R.id.btnDelete)

        fun bind(request: EmergencyRequest) {
            tvContact.text = "Contact: ${request.emergencyContactNumber ?: "N/A"}"
            tvLocation.text = "Lat/Lng: ${request.latitude}, ${request.longitude}"
            tvTimestamp.text = "Time: ${formatTimestamp(request.timestamp)}"

            btnDelete.setOnClickListener {
                onDeleteClick(request)
            }
        }

        private fun formatTimestamp(timestamp: Long?): String {
            if (timestamp == null) return "N/A"
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }
}
