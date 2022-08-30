package com.example.bluetooth.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetooth.data.model.Device
import com.example.bluetooth.databinding.ItemBluetoothDeviceBinding

class DeviceListAdapter(
    private val connect: (String) -> Unit,
    private val disconnect: (String) -> Unit
) : ListAdapter<Device, DeviceListAdapter.DeviceViewHolder>(DeviceDiffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        return DeviceViewHolder(
            ItemBluetoothDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DeviceViewHolder(private val binding: ItemBluetoothDeviceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Device) {
            binding.item = item.name
            setOnConnectClick(item.address)
            setOnDisconnectClick(item.address)
        }

        private fun setOnConnectClick(address: String) {
            binding.tvConnect.setOnClickListener {
                connect(address)
            }
        }

        private fun setOnDisconnectClick(address: String) {
            binding.tvDisconnect.setOnClickListener {
                disconnect(address)
            }
        }
    }

    private object DeviceDiffUtil : DiffUtil.ItemCallback<Device>() {
        override fun areItemsTheSame(oldItem: Device, newItem: Device) =
            oldItem.hashCode() == newItem.hashCode()

        override fun areContentsTheSame(oldItem: Device, newItem: Device) =
            oldItem == newItem

    }

}