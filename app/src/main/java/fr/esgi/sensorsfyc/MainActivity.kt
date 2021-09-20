package fr.esgi.sensorsfyc

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Vibrator
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import fr.esgi.sensorsfyc.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityMainBinding

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeViews()

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

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
        v = this.getSystemService(VIBRATOR_SERVICE) as Vibrator
    }

    private fun initializeViews() {
        currentX = findViewById<View>(R.id.currentX) as TextView
        currentY = findViewById<View>(R.id.currentY) as TextView
        currentZ = findViewById<View>(R.id.currentZ) as TextView
        maxX = findViewById<View>(R.id.maxX) as TextView
        maxY = findViewById<View>(R.id.maxY) as TextView
        maxZ = findViewById<View>(R.id.maxZ) as TextView
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
        deltaX = Math.abs(lastX - event!!.values[0])
        deltaY = Math.abs(lastY - event.values[1])
        deltaZ = Math.abs(lastZ - event.values[2])

        // if the change is below 2, it is just plain noise

        // if the change is below 2, it is just plain noise
        if (deltaX < 2) deltaX = 0f
        if (deltaY < 2) deltaY = 0f
        if (deltaY > vibrateThreshold || deltaZ > vibrateThreshold) { //(deltaZ  vibrateThreshold) ||
            v!!.vibrate(50)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        TODO("Not yet implemented")
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