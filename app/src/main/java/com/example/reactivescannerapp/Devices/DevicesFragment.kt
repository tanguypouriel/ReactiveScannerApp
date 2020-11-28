package com.example.reactivescannerapp.Devices

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.reactivescannerapp.databinding.FragmentDevicesBinding
import com.google.android.material.snackbar.Snackbar


//La recherche de nouveaux appareils bluetooth ne fonctionne pas
// Placer le scanner en haut de liste si il est dans la liste
class DevicesFragment : Fragment() {

    lateinit var binding: FragmentDevicesBinding
    private val viewModel: DevicesViewModel by viewModels()

    private val REQUEST_ENABLE_BLUETOOTH = 1


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentDevicesBinding.inflate(layoutInflater, container, false)

        viewModel.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if ( !viewModel.bluetoothAdapter!!.isEnabled){
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
        }

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

        viewModel.errorMessage?.observe(viewLifecycleOwner){
            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
        }

       viewModel.refreshPairedDevicesList()

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_ENABLE_BLUETOOTH){
            if (resultCode == Activity.RESULT_OK){
                if (viewModel.bluetoothAdapter!!.isEnabled){
                    Toast.makeText(requireContext(), "Le bluetooth a été activé", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Le bluetooth a été désactivé", Toast.LENGTH_SHORT).show()
                }
            } else if ( resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(requireContext(), "L'activation du bluetooth a été annulé", Toast.LENGTH_SHORT).show()
            }
        }
    }



    fun showControlFragment(bluetoothDevice: BluetoothDevice){
        val action = DevicesFragmentDirections.actionDevicesFragmentToControlFragment(bluetoothDevice.name, bluetoothDevice.address)
        findNavController().navigate(action)
    }


}