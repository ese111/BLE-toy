package com.example.bluetooth.ui.address

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.bluetooth.databinding.ActivityAddressBinding
import com.example.bluetooth.ui.bluetooth.MainActivity
import com.example.bluetooth.util.repeatOnStarted
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddressActivity : AppCompatActivity() {

    private val binding: ActivityAddressBinding by lazy {
        ActivityAddressBinding.inflate(layoutInflater)
    }

    private val viewModel: AddressViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.vm = viewModel

        val adapter = AddressAdapter {
            viewModel.setDefaultPosition(it)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.rvAddressList.adapter = adapter

        binding.btnSearch.setOnClickListener {
            viewModel.setPosition()
        }

        repeatOnStarted {
            viewModel.addressList.collect {
                adapter.submitList(it)
            }
        }

    }
}