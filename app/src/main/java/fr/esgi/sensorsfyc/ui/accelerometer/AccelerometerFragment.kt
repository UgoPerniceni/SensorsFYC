package fr.esgi.sensorsfyc.ui.accelerometer

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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import fr.esgi.sensorsfyc.R
import fr.esgi.sensorsfyc.databinding.FragmentAccelerometerBinding
import kotlin.math.abs

class AccelerometerFragment : Fragment(), SensorEventListener {

    private lateinit var accelerometerViewModel: AccelerometerViewModel
    private var _binding: FragmentAccelerometerBinding? = null

    private val lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null

    private var deltaXMax = 0f
    private var deltaYMax = 0f
    private var deltaZMax = 0f

    private var deltaX = 0f
    private var deltaY = 0f
    private var deltaZ = 0f

    private var vibrateThreshold = 0f

    private var currentX: TextView? = null
    private var currentY:TextView? = null
    private var currentZ:TextView? = null

    private var maxX:TextView? = null
    private var maxY:TextView? = null
    private var maxZ:TextView? = null

    var v: Vibrator? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        accelerometerViewModel =
            ViewModelProvider(this).get(AccelerometerViewModel::class.java)

        _binding = FragmentAccelerometerBinding.inflate(inflater, container, false)
        initializeViews()

        sensorManager = this.activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        sensorManager?.let { sensorManager ->
            if(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                // success! we have an accelerometer
                accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
                accelerometer?.let { accelerometer -> vibrateThreshold = accelerometer.maximumRange / 2 }
            } else {
                // Accelerometer error
            }

        }

        //initialize vibration
        v = this.activity?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun initializeViews() {
        currentX = _binding?.root?.findViewById<View>(R.id.currentX) as TextView
        currentY = _binding?.root?.findViewById<View>(R.id.currentY) as TextView
        currentZ = _binding?.root?.findViewById<View>(R.id.currentZ) as TextView
        maxX = _binding?.root?.findViewById<View>(R.id.maxX) as TextView
        maxY = _binding?.root?.findViewById<View>(R.id.maxY) as TextView
        maxZ = _binding?.root?.findViewById<View>(R.id.maxZ) as TextView
    }

    //onResume() register the accelerometer for listening the events
    override fun onResume() {
        super.onResume()
        sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    //onPause() unregister the accelerometer for stop listening the events
    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        // clean current values


        // clean current values
        displayCleanValues()
        // display the current x,y,z accelerometer values
        // display the current x,y,z accelerometer values
        displayCurrentValues()
        // display the max x,y,z accelerometer values
        // display the max x,y,z accelerometer values
        displayMaxValues()

        // get the change of the x,y,z values of the accelerometer

        // get the change of the x,y,z values of the accelerometer
        deltaX = abs(lastX - event!!.values[0])
        deltaY = abs(lastY - event.values[1])
        deltaZ = abs(lastZ - event.values[2])

        // if the change is below 2, it is just plain noise

        // if the change is below 2, it is just plain noise
        if (deltaX < 2) deltaX = 0f
        if (deltaY < 2) deltaY = 0f
        if (deltaY > vibrateThreshold || deltaZ > vibrateThreshold) { //(deltaZ  vibrateThreshold) ||
            v!!.vibrate(50)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // TODO NOT IMPLEMENTED
    }

    private fun displayCleanValues() {
        currentX!!.text = "0.0"
        currentY!!.text = "0.0"
        currentZ!!.text = "0.0"
    }

    // display the current x,y,z accelerometer values
    private fun displayCurrentValues() {
        currentX?.text = deltaX.toString()
        currentY?.text = deltaY.toString()
        currentZ?.text = deltaZ.toString()
    }

    // display the max x,y,z accelerometer values
    private fun displayMaxValues() {
        if (deltaX > deltaXMax) {
            deltaXMax = deltaX
            maxX?.text = deltaXMax.toString()
        }
        if (deltaY > deltaYMax) {
            deltaYMax = deltaY
            maxY?.text = deltaYMax.toString()
        }
        if (deltaZ > deltaZMax) {
            deltaZMax = deltaZ
            maxZ?.text = deltaZMax.toString()
        }
    }
}