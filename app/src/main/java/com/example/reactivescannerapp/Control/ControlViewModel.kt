package com.example.reactivescannerapp.Control

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
import java.io.IOException
import java.lang.IllegalArgumentException


class ControlViewModelFactory(private val args: ControlFragmentArgs) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ControlViewModel::class.java)){
            return ControlViewModel(args) as T
        }

        throw IllegalArgumentException("Unknown ViewModel Class")
    }

}

class ControlViewModel(args: ControlFragmentArgs) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val bluetoothManager = BluetoothManager.getInstance()
    private var deviceInterface: SimpleBluetoothDeviceInterface? = null

    val connectionStatus: MutableLiveData<ConnectionStatus> = MutableLiveData(
        ConnectionStatus.DISCONNECTED
    )

    private val deviceAdress: String = args.deviceAdress
    private var connectionAttemptedOrMade = false

    var scannerData: ScannerData = ScannerData()
    var speed: MutableLiveData<Int> = MutableLiveData(scannerData.speed)
    var state: MutableLiveData<State> = MutableLiveData(scannerData.state)


    init{
        if (bluetoothManager == null){

        }
    }

    fun connect() {
        if (!connectionAttemptedOrMade){

            compositeDisposable.add(bluetoothManager.openSerialDevice(deviceAdress)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { device: BluetoothSerialDevice ->
                        onConnected(device.toSimpleDeviceInterface())
                    }) { t: Throwable? ->
                    connectionAttemptedOrMade = false
                    connectionStatus.value = ConnectionStatus.DISCONNECTED
                }
            )

            connectionAttemptedOrMade = true

            connectionStatus.value = ConnectionStatus.CONNECTING

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

            deviceInterface.setListeners(::onMessageReceived, ::onMessageSent) {
                //print error message
            }
        } else {
            connectionStatus.value = ConnectionStatus.DISCONNECTED
        }
    }



    private fun onMessageReceived(message: String){
        when(message){

            "A" -> scannerData.state = State.IS_RUNNING_RIGHT

            "B" -> scannerData.state = State.IS_RUNNING_LEFT

            "C" -> scannerData.state = State.STOP

            "D" -> scannerData.state = State.IS_MAX_RIGHT

            "E" -> scannerData.state = State.IS_MAX_LEFT

            "1" -> scannerData.speed = 1

            "2" -> scannerData.speed = 2

            "3" -> scannerData.speed = 3

            "4" -> scannerData.speed = 4

            "5" -> scannerData.speed = 5

            "6" -> scannerData.speed = 6

            else -> throw(IOException("Unknown message received"))
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

