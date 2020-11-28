package com.example.reactivescannerapp.Devices

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import com.harrysoft.androidbluetoothserial.BluetoothManager


class DevicesViewModel : ViewModel() {

    private var bluetoothManager: BluetoothManager = BluetoothManager.getInstance()
    val pairedDevicesList: MutableLiveData<List<BluetoothDevice>> = MutableLiveData()
    var bluetoothAdapter: BluetoothAdapter? = null

    private val _errorMessages: MutableLiveData<String>? = null
    val errorMessage = _errorMessages


    fun refreshPairedDevicesList(){
        pairedDevicesList.value = bluetoothManager.pairedDevicesList
    }

    override fun onCleared() {
        super.onCleared()
        bluetoothManager.close()
    }

}