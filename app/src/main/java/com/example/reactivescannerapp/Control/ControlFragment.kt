package com.example.reactivescannerapp.Control

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.reactivescannerapp.R
import com.example.reactivescannerapp.model.State
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_control.*


class ControlFragment : Fragment() {

    private val args: ControlFragmentArgs by navArgs()

    private lateinit var viewModelFactory: ControlViewModelFactory

    private val viewModel: ControlViewModel by viewModels (
            factoryProducer = { viewModelFactory }
    )

    //private lateinit var binding: FragmentControlBinding


    /*private lateinit var speedTextView: TextView
    private lateinit var speedTitleTextView: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var goRightButton: ImageButton
    private lateinit var goLeftButton: ImageButton
    private lateinit var pauseButton: ImageButton
    private lateinit var connectionTextView: TextView
    private lateinit var progressBar: ProgressBar*/


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModelFactory = ControlViewModelFactory()

        viewModel.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if ( !viewModel.bluetoothAdapter!!.isEnabled){
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
        }

       return inflater.inflate(R.layout.fragment_control, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)


        viewModel.state.observe(viewLifecycleOwner) { onScannerStateChanged(it) }
        viewModel.speed.observe(viewLifecycleOwner) { onScannerSpeedChanged(it) }
        viewModel.connectionStatus.observe(viewLifecycleOwner) { onConnectionStatus(it) }
        viewModel.errorMessage.observe(viewLifecycleOwner) {
            Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
        }


        seekBar.setOnSeekBarChangeListener( object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser){
                    viewModel.scannerData.speed = progress
                    viewModel.sendMessage(progress.toString())
                }
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {

            }
            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })

        goRightButton.setOnClickListener {

            if (viewModel.connectionStatus.value == ConnectionStatus.CONNECTED ){
                viewModel.sendMessage("A")
                viewModel.scannerData.state = State.IS_RUNNING_RIGHT
                viewModel.state.value = State.IS_RUNNING_RIGHT
                Log.d("mdebug", viewModel.scannerData.toString())
                // vérifier que le message a bien été recu ?
            }

            Log.d("mdebug", "coucou")
        }

        goLeftButton.setOnClickListener {

            if (viewModel.connectionStatus.value == ConnectionStatus.CONNECTED){
                viewModel.sendMessage("B")
                viewModel.scannerData.state = State.IS_RUNNING_LEFT
                viewModel.state.value = State.IS_RUNNING_LEFT
                Log.d("mdebug", viewModel.scannerData.toString())
                // vérifier que le message a bien été recu ?
            }
        }

        playPauseButton.setOnClickListener {

            if (viewModel.connectionStatus.value == ConnectionStatus.CONNECTED){
                viewModel.sendMessage("C")
                viewModel.scannerData.state = State.STOP
                viewModel.state.value = State.STOP
                // vérifier que le message a bien été recu ?
            }
        }

        connectionStatusButton.setOnClickListener {

            if (viewModel.connectionStatus.value == ConnectionStatus.CONNECTED){
                viewModel.disconnect()
            } else if (viewModel.connectionStatus.value == ConnectionStatus.DISCONNECTED){
                viewModel.connect()
            }
        }

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
    
    private fun onConnectionStatus(connectionStatus : ConnectionStatus){
        when (connectionStatus){

            ConnectionStatus.CONNECTED-> {

                progressBar.visibility = View.GONE

                playPauseButton.visibility = View.VISIBLE
                goRightButton.visibility = View.VISIBLE
                goLeftButton.visibility = View.VISIBLE
                speedTextView.visibility = View.VISIBLE
                speedTitleTextView.visibility = View.VISIBLE
                seekBar.visibility = View.VISIBLE


                connectionStatusButton.text = "Connecté à ${viewModel.deviceName}"
                connectionStatusButton.setBackgroundResource(R.drawable.bg_rounded_connected)
                connectionStatusButton.isClickable = true
            }

            ConnectionStatus.DISCONNECTED -> {
                if (progressBar.visibility == View.VISIBLE)
                    progressBar.visibility = View.GONE

                playPauseButton.visibility = View.GONE
                goRightButton.visibility = View.GONE
                goLeftButton.visibility = View.GONE
                speedTextView.visibility = View.GONE
                speedTitleTextView.visibility = View.GONE
                seekBar.visibility = View.GONE

                connectionStatusButton.text = "Se connecter"
                connectionStatusButton.setBackgroundResource(R.drawable.bg_rounded_disconnected)
                connectionStatusButton.isClickable = true
            }

            ConnectionStatus.CONNECTING -> {
                progressBar.visibility = View.VISIBLE
                connectionStatusButton.text = "En cours de connection"
                connectionStatusButton.setBackgroundResource(R.drawable.bg_rounded_connecting)
                connectionStatusButton.isClickable = false
            }

        }
    }

    private fun onScannerStateChanged(state: State){
        when (state){
            State.STOP -> {

                playPauseButton.visibility = View.GONE
                Log.d("mdebug", "viewGone")

            }
            State.IS_MAX_LEFT -> {

                playPauseButton.isClickable = false
                goLeftButton.visibility = View.GONE

            }
            State.IS_MAX_RIGHT -> {

                playPauseButton.isClickable = false
                goRightButton.visibility = View.GONE

            }
            State.IS_RUNNING_LEFT -> {

                playPauseButton.visibility = View.VISIBLE
                goRightButton.visibility = View.VISIBLE

            }
            State.IS_RUNNING_RIGHT -> {

                playPauseButton.visibility = View.VISIBLE
                goLeftButton.visibility = View.VISIBLE

            }
        }
    }

    private fun onScannerSpeedChanged(speed: Int){
        Log.d("mdebug", "vitesse : $speed")
        seekBar.progress = speed
        speedTextView.text = speed.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.disconnect()
    }

    companion object {
        private const val REQUEST_ENABLE_BLUETOOTH = 1
    }


}