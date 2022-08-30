package com.example.bluetooth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetooth.data.model.MapResponse
import com.example.bluetooth.data.repository.AddressRepository
import com.example.bluetooth.util.DefaultPosition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddressViewModel @Inject constructor(
    private val repository: AddressRepository
) : ViewModel() {

    private val _addressList = MutableStateFlow<List<MapResponse>>(emptyList())
    val addressList = _addressList.asStateFlow()

    var search = ""

    fun setPosition() {
        viewModelScope.launch {
            _addressList.value = repository.getPosition(search)
        }
    }

    fun setDefaultPosition(info: MapResponse) = repository.setDefaultPosition(info)
}