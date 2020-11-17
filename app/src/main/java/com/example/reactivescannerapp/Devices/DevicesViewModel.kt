package com.example.reactivescannerapp.Devices

import android.bluetooth.BluetoothDevice
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import com.harrysoft.androidbluetoothserial.BluetoothManager


class DevicesViewModel : ViewModel() {

    private var bluetoothManager: BluetoothManager = BluetoothManager.getInstance()
    var pairedDevicesList: MutableLiveData<List<BluetoothDevice>> = MutableLiveData()

    init {
        if (bluetoothManager == null) {
            //TODO notifier que le bluetooth ne fonctionne pas
        }
    }

    fun refreshPairedDevicesList(){
        pairedDevicesList.value = bluetoothManager.pairedDevicesList
    }

    override fun onCleared() {
        super.onCleared()
        bluetoothManager.close()
    }

}