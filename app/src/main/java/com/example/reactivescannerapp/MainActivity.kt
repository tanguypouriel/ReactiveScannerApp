package com.example.reactivescannerapp

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.reactivescannerapp.Control.ConnectionStatus
import com.example.reactivescannerapp.Control.ControlViewModel
import com.example.reactivescannerapp.model.State
import com.google.android.material.snackbar.Snackbar
import it.sephiroth.android.library.numberpicker.doOnProgressChanged
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


// est ce qu'on affiche un message quand la batterie est déchargée ?
class MainActivity : AppCompatActivity() {

    val viewModel: ControlViewModel by viewModels()
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        if ( !bluetoothAdapter!!.isEnabled ){
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
        }

        viewModel.state.observe(this) { onScannerStateChanged(it) }
        viewModel.speed.observe(this) { onScannerSpeedChanged(it) }
        viewModel.connectionStatus.observe(this) { onConnectionStatus(it) }
        viewModel.errorMessage.observe(this) {
            Snackbar.make(findViewById(R.id.connectionStatusButton), it, Snackbar.LENGTH_LONG).show()
        }


        numberPicker.doOnProgressChanged { _, progress, fromUser ->
            if (fromUser){
                viewModel.scannerData.speed = progress
                viewModel.sendMessage(progress.toString())
            }
        }

        goRightButton.setOnClickListener {

            if (viewModel.connectionStatus.value == ConnectionStatus.CONNECTED ){
                viewModel.sendMessage("A")
                viewModel.scannerData.state = State.IS_RUNNING_RIGHT
                viewModel.state.value = State.IS_RUNNING_RIGHT
                Log.d("mdebug", viewModel.scannerData.toString())
                // vérifier que le message a bien été recu ?
            }

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_contacts -> {

                val builder: AlertDialog.Builder? = AlertDialog.Builder(this)


                builder?.setMessage(R.string.dialog_contacts)
                        ?.setTitle(R.string.menu_contacts)

                builder?.show()

                true
            }
            R.id.menu_credits ->{

               AlertDialog.Builder(this).apply {
                    setTitle(getString(R.string.menu_credits))
                    setMessage(getString(R.string.dialog_credits))
                }.show()

                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_ENABLE_BLUETOOTH){

            if (resultCode == RESULT_OK){
                if (bluetoothAdapter!!.isEnabled){
                    Toast.makeText(this, "Le bluetooth a été activé", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Le bluetooth a été désactivé", Toast.LENGTH_SHORT).show()
                }
            } else if ( resultCode == RESULT_CANCELED){
                Toast.makeText(this, "L'activation du bluetooth a été annulé", Toast.LENGTH_SHORT).show()
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
                speedTitleTextView.visibility = View.VISIBLE
                numberPicker.visibility = View.VISIBLE


                connectionStatusButton.text = getString(R.string.status_button_connected)
                connectionStatusButton.backgroundTintList = getColorStateList(R.color.state_list_connected)
                connectionStatusButton.isClickable = true
            }

            ConnectionStatus.DISCONNECTED -> {
                if (progressBar.visibility == View.VISIBLE)
                    progressBar.visibility = View.GONE

                playPauseButton.visibility = View.GONE
                goRightButton.visibility = View.GONE
                goLeftButton.visibility = View.GONE
                speedTitleTextView.visibility = View.GONE
                numberPicker.visibility = View.GONE

                connectionStatusButton.text = getString(R.string.status_button_disconnected)
                connectionStatusButton.backgroundTintList = getColorStateList(R.color.state_list_disconnected)
                connectionStatusButton.isClickable = true
            }

            ConnectionStatus.CONNECTING -> {
                progressBar.visibility = View.VISIBLE
                connectionStatusButton.text = getString(R.string.status_button_connecting)
                connectionStatusButton.backgroundTintList = getColorStateList(R.color.state_list_connecting)
                connectionStatusButton.isClickable = false
            }

        }
    }

    private fun onScannerStateChanged(state: State){
        when (state){
            State.STOP -> {

                playPauseButton.visibility = View.GONE

            }
            State.IS_MAX_LEFT -> {

                playPauseButton.visibility = View.GONE
                goLeftButton.visibility = View.GONE

            }
            State.IS_MAX_RIGHT -> {

                playPauseButton.visibility = View.GONE
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
        numberPicker.progress = speed
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.disconnect()
    }

    companion object {
        private const val REQUEST_ENABLE_BLUETOOTH = 1
    }


}