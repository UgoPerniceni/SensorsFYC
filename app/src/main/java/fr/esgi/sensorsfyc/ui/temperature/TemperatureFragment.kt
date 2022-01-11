package fr.esgi.sensorsfyc.ui.temperature

import android.content.Context
import android.graphics.Color
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
import fr.esgi.sensorsfyc.databinding.FragmentTemperatureBinding

class TemperatureFragment : Fragment(), SensorEventListener {

    private lateinit var temperatureViewModel: TemperatureViewModel

    private var _binding: FragmentTemperatureBinding? = null
    private var sensorManager: SensorManager? = null
    var v: Vibrator? = null

    private var temperature: Sensor? = null
    private var value: TextView? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        temperatureViewModel =
            ViewModelProvider(this).get(TemperatureViewModel::class.java)

        _binding = FragmentTemperatureBinding.inflate(inflater, container, false)
        initializeViews()

        sensorManager = this.activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager?.let { sensorManager ->
            if (sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null) {
                temperature = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)

            } else {
                Toast.makeText(context, "AMBIENT TEMPERATURE SENSOR NOT AVAILABLE", Toast.LENGTH_LONG).show()
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
        val temperature = event.values[0]
        value?.text = "${temperature}°C"
    }

    override fun onResume() {
        // Register a listener for the sensor.
        super.onResume()

        temperature?.also { temperature ->
            // sensorManager?.registerListener(this, temperature, SensorManager.SENSOR_DELAY_NORMAL)
            sensorManager?.registerListener(this, temperature, 1000000)
        }
    }

    override fun onPause() {
        // Be sure to unregister the sensor when the activity pauses.
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    private fun initializeViews() {
        value = _binding?.root?.findViewById(R.id.temperature_value) as TextView
    }
}