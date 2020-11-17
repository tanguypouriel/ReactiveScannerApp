package com.example.reactivescannerapp.Control

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.reactivescannerapp.R
import com.example.reactivescannerapp.databinding.FragmentControlBinding
import com.example.reactivescannerapp.model.State


class ControlFragment : Fragment() {

    private val args: ControlFragmentArgs by navArgs()

    private lateinit var viewmodelFactory: ControlViewModelFactory
    private val viewModel: ControlViewModel by viewModels (
            factoryProducer = { viewmodelFactory }
    )

    private lateinit var binding: FragmentControlBinding

    lateinit var deviceName: String
    lateinit var deviceAdress: String

    private lateinit var speedTextView: TextView
    private lateinit var speedTitleTextView: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var goRightButton: ImageButton
    private lateinit var goLeftButton: ImageButton
    private lateinit var pauseButton: ImageButton
    private lateinit var connectionTextView: TextView
    private lateinit var progressBar: ProgressBar


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        deviceAdress = args.deviceAdress
        deviceName = args.deviceName

        viewmodelFactory = ControlViewModelFactory(args)

        binding = FragmentControlBinding.inflate(layoutInflater, container, false)

        speedTextView = binding.speedTextView
        seekBar = binding.seekBar
        goRightButton = binding.goRightButton
        goLeftButton = binding.goLeftButton
        pauseButton = binding.playPauseButton
        connectionTextView = binding.connectionStatusText
        progressBar = binding.progressBar

        viewModel.state.observe(viewLifecycleOwner) { onScannerStateChanged(it) }
        viewModel.speed.observe(viewLifecycleOwner) { onScannerSpeedChanged(it) }
        viewModel.connectionStatus.observe(viewLifecycleOwner) { onConnectionStatus(it) }

        seekBar.setOnSeekBarChangeListener( object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser){
                    viewModel.scannerData.speed = progress
                }
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {

            }
            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })

        goRightButton.setOnClickListener {
            if (viewModel.connectionStatus.equals(ConnectionStatus.CONNECTED)){
                viewModel.sendMessage("A")
                viewModel.scannerData.state = State.IS_RUNNING_RIGHT
                // vérifier que le message a bien été recu ?
            }
        }

        goLeftButton.setOnClickListener {
            if (viewModel.connectionStatus.equals(ConnectionStatus.CONNECTED)){
                viewModel.sendMessage("B")
                viewModel.scannerData.state = State.IS_RUNNING_LEFT
                // vérifier que le message a bien été recu ?
            }
        }

        pauseButton.setOnClickListener {
            if (viewModel.connectionStatus.equals(ConnectionStatus.CONNECTED)){
                viewModel.sendMessage("C")
                viewModel.scannerData.state = State.STOP
                // vérifier que le message a bien été recu ?
            }
        }

        viewModel.connect()

        return binding.root
    }

    
    fun onConnectionStatus(connectionStatus : ConnectionStatus){
        when (connectionStatus){

            ConnectionStatus.CONNECTED-> {

                progressBar.visibility = View.GONE

                pauseButton.visibility = View.VISIBLE
                goRightButton.visibility = View.VISIBLE
                goLeftButton.visibility = View.VISIBLE
                speedTextView.visibility = View.VISIBLE
                speedTitleTextView.visibility = View.VISIBLE
                seekBar.visibility = View.VISIBLE


                connectionTextView.text = "Connecté à $deviceName"
                connectionTextView.setBackgroundResource(R.drawable.bg_rounded_connected)
            }

            ConnectionStatus.DISCONNECTED -> {
                if (progressBar.visibility == View.VISIBLE)
                    progressBar.visibility = View.GONE

                connectionTextView.text = "Déconnecté"
                connectionTextView.setBackgroundResource(R.drawable.bg_rounded_disconnected)
            }

            ConnectionStatus.CONNECTING -> {
                progressBar.visibility = View.VISIBLE
                connectionTextView.text = "En cours de connection"
                connectionTextView.setBackgroundResource(R.drawable.bg_rounded_connecting)
            }

        }
    }

    fun onScannerStateChanged(state: State){
        when (state){
            State.STOP -> {

                pauseButton.isClickable = false

            }
            State.IS_MAX_LEFT -> {

                pauseButton.isClickable = false
                goLeftButton.visibility = View.GONE

            }
            State.IS_MAX_RIGHT -> {

                pauseButton.isClickable = false
                goRightButton.visibility = View.GONE

            }
            State.IS_RUNNING_LEFT -> {

                pauseButton.isClickable = true
                goRightButton.visibility = View.VISIBLE

            }
            State.IS_RUNNING_RIGHT -> {

                pauseButton.isClickable = true
                goLeftButton.visibility = View.VISIBLE

            }
        }
    }

    fun onScannerSpeedChanged(speed: Int){
        seekBar.progress = speed
        speedTextView.text = speed.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.disconnect()
    }


}