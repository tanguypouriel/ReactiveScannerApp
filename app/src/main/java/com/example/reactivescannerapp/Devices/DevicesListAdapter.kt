package com.example.reactivescannerapp.Devices

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.reactivescannerapp.R

class DevicesListViewHolder(
    view: View
) : RecyclerView.ViewHolder(view) {

    private val deviceNameText: TextView = view.findViewById(R.id.device_name)
    private val deviceAdressText: TextView = view.findViewById(R.id.device_adress)

    fun bind (bluetoothDevice: BluetoothDevice){
        deviceNameText.text = bluetoothDevice.name
        deviceAdressText.text = bluetoothDevice.address
    }
}

class DevicesListAdapter(
    private val clickDeviceHandler: (BluetoothDevice) -> Unit
) : RecyclerView.Adapter<DevicesListViewHolder>(){

    private lateinit var deviceList: List<BluetoothDevice>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevicesListViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_device, parent, false)
        return DevicesListViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DevicesListViewHolder, position: Int) {
        holder.bind(deviceList.get(position))
        holder.itemView.setOnClickListener {
            clickDeviceHandler(deviceList.get(position))
        }
    }

    override fun getItemCount(): Int {
        return  deviceList.size
    }

    fun updateList(deviceList : List<BluetoothDevice>) {
        this.deviceList = deviceList
        notifyDataSetChanged()
    }

}