package com.example.bluetooth.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetooth.data.model.MapResponse
import com.example.bluetooth.databinding.ItemAddressBinding

class AddressAdapter(private val callback: (MapResponse) -> Unit): ListAdapter<MapResponse, AddressAdapter.AddressViewHolder>(AddressDiffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        return AddressViewHolder(
            ItemAddressBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    inner class AddressViewHolder(private val binding: ItemAddressBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MapResponse) {
            binding.info = item
            setOnItemViewClick(item)
        }

        private fun setOnItemViewClick(item: MapResponse) {
            itemView.setOnClickListener {
                callback(item)
            }
        }
    }

    private object AddressDiffUtil: DiffUtil.ItemCallback<MapResponse>() {
        override fun areItemsTheSame(oldItem: MapResponse, newItem: MapResponse) =
            oldItem.roadAddress == newItem.roadAddress

        override fun areContentsTheSame(oldItem: MapResponse, newItem: MapResponse) =
            oldItem == newItem

    }

}