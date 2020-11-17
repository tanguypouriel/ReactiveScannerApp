package com.example.reactivescannerapp.Devices

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.reactivescannerapp.databinding.FragmentDevicesBinding


class DevicesFragment : Fragment() {

    lateinit var binding: FragmentDevicesBinding
    val viewModel: DevicesViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentDevicesBinding.inflate(layoutInflater, container, false)


        val devicesList = binding.recyclerView

        devicesList.layoutManager = LinearLayoutManager(requireContext())

        val adapter = DevicesListAdapter {
            showControlFragment(it)
        }

        devicesList.adapter = adapter

        binding.refreshButton.setOnClickListener { viewModel.refreshPairedDevicesList() }

        viewModel.pairedDevicesList.observe(viewLifecycleOwner){
            adapter.updateList(it)
        }

       viewModel.refreshPairedDevicesList()

        return binding.root
    }



    fun showControlFragment(bluetoothDevice: BluetoothDevice){
        val action = DevicesFragmentDirections.actionDevicesFragmentToControlFragment(bluetoothDevice.name, bluetoothDevice.address)
        findNavController().navigate(action)
    }


}