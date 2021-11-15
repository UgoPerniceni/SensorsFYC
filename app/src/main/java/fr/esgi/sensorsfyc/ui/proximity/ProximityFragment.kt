package fr.esgi.sensorsfyc.ui.proximity

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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import fr.esgi.sensorsfyc.R
import fr.esgi.sensorsfyc.databinding.FragmentProximityBinding

class ProximityFragment : Fragment(), SensorEventListener {

    private lateinit var proximityViewModel: ProximityViewModel

    private var _binding: FragmentProximityBinding? = null
    private var sensorManager: SensorManager? = null
    var v: Vibrator? = null

    private var proximity: Sensor? = null

    private var fontColor: View? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        proximityViewModel =
            ViewModelProvider(this).get(ProximityViewModel::class.java)

        _binding = FragmentProximityBinding.inflate(inflater, container, false)
        initializeViews()

        sensorManager = this.activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager?.let { sensorManager ->
            if(sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null) {
                proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
            } else {
                // Proximity error
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
        val distance = event.values[0]
        println(distance)

        if(distance < proximity!!.maximumRange) {
            // is close
            fontColor?.setBackgroundColor(Color.RED)
        } else {
            // is far
            fontColor?.setBackgroundColor(Color.GREEN)
        }
    }

    override fun onResume() {
        // Register a listener for the sensor.
        super.onResume()

        proximity?.also { proximity ->
            // sensorManager?.registerListener(this, proximity, SensorManager.SENSOR_DELAY_NORMAL)
            sensorManager?.registerListener(this, proximity, 1000000)
        }
    }

    override fun onPause() {
        // Be sure to unregister the sensor when the activity pauses.
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    private fun initializeViews() {
        fontColor = _binding?.root?.findViewById(R.id.font_color) as View
    }
}