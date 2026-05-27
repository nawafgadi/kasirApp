package com.nawaf.kasirpas.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.nawaf.kasirpas.databinding.ItemBluetoothDeviceBinding

class BluetoothDeviceAdapter(
    private var devices: List<BluetoothConnection>,
    private val onItemClick: (BluetoothConnection) -> Unit
) : RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemBluetoothDeviceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("MissingPermission")
        fun bind(connection: BluetoothConnection) {
            val device = connection.device
            binding.tvDeviceName.text = device.name ?: "Unknown Device"
            binding.tvDeviceAddress.text = device.address
            binding.cardDevice.setOnClickListener {
                onItemClick(connection)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBluetoothDeviceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(devices[position])
    }

    override fun getItemCount(): Int = devices.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newDevices: List<BluetoothConnection>) {
        devices = newDevices
        notifyDataSetChanged()
    }
}
