package fr.esgi.sensorsfyc.ui.compass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import fr.esgi.sensorsfyc.R
import fr.esgi.sensorsfyc.databinding.FragmentCompassBinding

class CompassFragment : Fragment(), SensorEventListener {

    private lateinit var compassViewModel: CompassViewModel
    private var _binding: FragmentCompassBinding? = null

    private var sensorManager: SensorManager? = null

    // private var orientation: Sensor? = null
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null

    // define the compass picture that will be use
    private lateinit var compassImage: ImageView

    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null

    private val floatOrientation = FloatArray(3)
    private val floatRotationMatrix = FloatArray(9)

    private var pi = 3.14159

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        compassViewModel =
            ViewModelProvider(this).get(CompassViewModel::class.java)

        _binding = FragmentCompassBinding.inflate(inflater, container, false)

        initializeViews()

        sensorManager = this.activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager?.let { sensorManager ->
            if(sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null) {
                // orientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)
                accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            } else {
                Toast.makeText(context, "ACCELEROMETER OR MAGNETOMETER SENSOR NOT AVAILABLE", Toast.LENGTH_LONG).show()
            }

        }

        return binding.root
    }

    private fun initializeViews() {
        compassImage = _binding?.root?.findViewById(R.id.compass_image) as ImageView
    }

    override fun onResume() {
        super.onResume()
        sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager?.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this);
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event?.sensor?.type == Sensor.TYPE_ACCELEROMETER)
            gravity = event.values
        if(event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD)
            geomagnetic = event.values

        Sensor.TYPE_STEP_COUNTER

        if(geomagnetic != null && geomagnetic?.isNotEmpty() == true && gravity != null && gravity?.isNotEmpty() == true) {

            SensorManager.getRotationMatrix(floatRotationMatrix, null, gravity, geomagnetic)
            SensorManager.getOrientation(floatRotationMatrix, floatOrientation)

            compassImage.rotation = (-floatOrientation[0] * 180 / pi).toFloat()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}