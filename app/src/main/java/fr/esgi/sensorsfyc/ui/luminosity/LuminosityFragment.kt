package fr.esgi.sensorsfyc.ui.luminosity

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import fr.esgi.sensorsfyc.R
import fr.esgi.sensorsfyc.databinding.FragmentLuminosityBinding
import kotlin.time.Duration

class LuminosityFragment : Fragment(), SensorEventListener {

    private lateinit var luminosityViewModel: LuminosityViewModel

    private var _binding: FragmentLuminosityBinding? = null
    private var sensorManager: SensorManager? = null
    var v: Vibrator? = null

    private var light: Sensor? = null

    private var value: TextView? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        luminosityViewModel =
            ViewModelProvider(this).get(LuminosityViewModel::class.java)

        _binding = FragmentLuminosityBinding.inflate(inflater, container, false)
        initializeViews()

        sensorManager = this.activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager?.let { sensorManager ->
            if (sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
                light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

            } else {
                Toast.makeText(context, "LIGHT SENSOR NOT AVAILABLE", Toast.LENGTH_LONG).show()
            }
        }

        //initialize vibration
        v = this.activity?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        return binding.root
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do something here if sensor accuracy changes.
    }

    override fun onSensorChanged(event: SensorEvent) {
        val luminosity = event.values[0];
        value?.text = luminosity.toString()
    }

    override fun onResume() {
        // Register a listener for the sensor.
        super.onResume()

        light?.also { light ->
            // sensorManager?.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL)
            sensorManager?.registerListener(this, light, 1000000)
        }
    }

    override fun onPause() {
        // Be sure to unregister the sensor when the activity pauses.
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    private fun initializeViews() {
        value = _binding?.root?.findViewById(R.id.luminosity_value) as TextView
    }
}