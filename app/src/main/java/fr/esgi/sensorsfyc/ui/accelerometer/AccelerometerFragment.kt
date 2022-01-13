package fr.esgi.sensorsfyc.ui.accelerometer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
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
import fr.esgi.sensorsfyc.databinding.FragmentAccelerometerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.LocalDateTime
import kotlin.math.abs

class AccelerometerFragment : Fragment(), SensorEventListener {

    val httpClient: OkHttpClient = OkHttpClient()

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
            // use TYPE_ACCELERATION for acceleration with gravity
            if(sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
                // success! we have an accelerometer
                accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
                accelerometer?.let { accelerometer -> vibrateThreshold = accelerometer.maximumRange / 2 }
            } else {
                Toast.makeText(context, "ACCELEROMETER SENSOR NOT AVAILABLE", Toast.LENGTH_LONG).show()
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
        displayCleanValues()
        // display the current x,y,z accelerometer values
        // display the current x,y,z accelerometer values
        displayCurrentValues()
        // display the max x,y,z accelerometer values
        // display the max x,y,z accelerometer values
        displayMaxValues()

        if(deltaX != 0f && deltaY != 0f)
            sendPostRequest(deltaX, deltaY, deltaZ)

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
        // https://stackoverflow.com/questions/27134676/accelerometer-data-z-axis-gives-wrong-data
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

    private fun sendPostRequest(xValue: Number, yValue: Number, zValue: Number) {
        GlobalScope.launch(Dispatchers.IO) {
            val localDateTime: LocalDateTime = LocalDateTime.now();
            val body =
                    "[\n" +
                    "    {\n" +
                    "        \"localDateTime\": \"$localDateTime\",\n" +
                    "        \"measurementName\": \"acc-x\",\n" +
                    "        \"unit\": \"m.s\",\n" +
                    "        \"value\": $xValue\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"localDateTime\": \"$localDateTime\",\n" +
                    "        \"measurementName\": \"acc-y\",\n" +
                    "        \"unit\": \"m.s\",\n" +
                    "        \"value\": $yValue\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"localDateTime\": \"$localDateTime\",\n" +
                    "        \"measurementName\": \"acc-z\",\n" +
                    "        \"unit\": \"m.s\",\n" +
                    "        \"value\": $zValue\n" +
                    "    }\n" +
                    "]"

            val request = Request.Builder()
                .header("Content-Type", "application/json")
                .url("https://sleepy-refuge-95334.herokuapp.com/api/v1/elastic/send/samples")
                .post(body.toRequestBody())
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Unexpected code $response")
                }

                for ((name, value) in response.headers) {
                    println("$name: $value")
                }

                println(response.body!!.string())
            }
        }}
}