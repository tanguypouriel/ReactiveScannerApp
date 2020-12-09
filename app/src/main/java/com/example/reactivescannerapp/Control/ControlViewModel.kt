package com.example.reactivescannerapp.Control

import android.bluetooth.BluetoothAdapter
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.reactivescannerapp.model.ScannerData
import com.example.reactivescannerapp.model.State
import com.harrysoft.androidbluetoothserial.BluetoothManager
import com.harrysoft.androidbluetoothserial.BluetoothSerialDevice
import com.harrysoft.androidbluetoothserial.SimpleBluetoothDeviceInterface
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


class ControlViewModel() : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val bluetoothManager = BluetoothManager.getInstance()
    private var deviceInterface: SimpleBluetoothDeviceInterface? = null

    val errorMessage : MutableLiveData<String> by lazy { MutableLiveData<String>() }

    val connectionStatus: MutableLiveData<ConnectionStatus> = MutableLiveData(ConnectionStatus.DISCONNECTED)

    private var deviceAdress: String? = null
    val deviceName: String = "Scanner"

    private var connectionAttemptedOrMade = false

    var scannerData: ScannerData = ScannerData()
    var speed: MutableLiveData<Int> = MutableLiveData(scannerData.speed)
    var state: MutableLiveData<State> = MutableLiveData(scannerData.state)


    fun connect() {

        for (device in bluetoothManager.pairedDevicesList){
            if (device.name == deviceName)
                deviceAdress = device.address
        }

        if (!connectionAttemptedOrMade){

            if (deviceAdress != null) {
                compositeDisposable.add(bluetoothManager.openSerialDevice(deviceAdress)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { device: BluetoothSerialDevice ->
                            onConnected(device.toSimpleDeviceInterface())
                        }) { t: Throwable? ->
                        connectionAttemptedOrMade = false
                        errorMessage.value = "Le Scanner n'as pas été trouvé, assurez vous qu'il est allumé et à portée"
                        connectionStatus.value = ConnectionStatus.DISCONNECTED
                    }
                )

                connectionAttemptedOrMade = true

                connectionStatus.value = ConnectionStatus.CONNECTING

            } else {
                errorMessage.value = "Veuillez vous connecter une première fois au scanner dans les paramètres"
            }

        }
    }

    fun disconnect() {

        if (connectionAttemptedOrMade && deviceInterface != null) {
            connectionAttemptedOrMade = false
            bluetoothManager.closeDevice(deviceInterface)
            deviceInterface = null
            connectionStatus.value = ConnectionStatus.DISCONNECTED
        }
    }


    private fun onConnected(deviceInterface: SimpleBluetoothDeviceInterface?){
        this.deviceInterface = deviceInterface

        if (deviceInterface != null){
            connectionStatus.value = ConnectionStatus.CONNECTED

            this.deviceInterface!!.setListeners(::onMessageReceived, ::onMessageSent) {
                errorMessage.value = "Le Scanner n'as pas été trouvé, assurez vous qu'il est allumé et à portée"
                connectionStatus.value = ConnectionStatus.DISCONNECTED
                disconnect()
            }

            // demande de synchronisation des valeurs à la connexion
            sendMessage("F")
        } else {
            connectionStatus.value = ConnectionStatus.DISCONNECTED
        }
    }



    private fun onMessageReceived(message: String){

        when(message){

            "A" -> {
                scannerData.state = State.IS_RUNNING_RIGHT
                state.value = State.IS_RUNNING_RIGHT
            }

            "B" -> {
                scannerData.state = State.IS_RUNNING_LEFT
                state.value = State.IS_RUNNING_LEFT
            }

            "C" -> {
                scannerData.state = State.STOP
                state.value = State.STOP
            }

            "D" -> {
                scannerData.state = State.IS_MAX_RIGHT
                state.value = State.IS_MAX_RIGHT
            }

            "E" -> {
                scannerData.state = State.IS_MAX_LEFT
                state.value = State.IS_MAX_LEFT
            }

            "1" -> {
                scannerData.speed = 1
                speed.value = 1
            }

            "2" -> {
                scannerData.speed = 2
                speed.value = 2
            }

            "3" -> {
                scannerData.speed = 3
                speed.value = 3
            }

            "4" -> {
                scannerData.speed = 4
                speed.value = 4
            }

            "5" -> {
                scannerData.speed = 5
                speed.value = 5
            }

            "6" -> {
                scannerData.speed = 6
                speed.value = 6
            }

            else -> errorMessage.value = "Message inconnu reçu : $message"

        }
    }

    private fun onMessageSent(message: String){
    }

    fun sendMessage(message: String){
        if (deviceInterface != null && message.isNotEmpty())
            deviceInterface!!.sendMessage(message)
    }

    override fun onCleared() {
        super.onCleared()

        compositeDisposable.clear()
        bluetoothManager.close()
    }



}

enum class ConnectionStatus {
    DISCONNECTED, CONNECTING, CONNECTED
}

