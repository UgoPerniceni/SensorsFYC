package fr.esgi.sensor_uv_application.ui.main

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.*
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import fr.esgi.sensor_uv_application.R
import fr.esgi.sensor_uv_application.databinding.MainFragmentBinding
import fr.esgi.sensor_uv_application.model.Uv
import java.time.LocalDateTime

class MainFragment : Fragment(), SensorEventListener {

    inner class MyAxisFormatter : IndexAxisValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            val index = value.toInt()
            return if (index < uvList.size) {
                "${uvList[0].datetime.hour}:${uvList[0].datetime.minute}:${uvList[0].datetime.second}"
            } else {
                ""
            }
        }
    }

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var uvValue: TextView
    private lateinit var lineChart: LineChart

    private var uvList = ArrayList<Uv>()

    private var sensorManager: SensorManager? = null
    private var light: Sensor? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = MainFragmentBinding.inflate(inflater, container, false)

        // set variables
        initializeViews(binding)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sensorManager = this.activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        sensorManager?.let { sensorManager ->
            if (sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
                light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
            } else {
                Toast.makeText(context, "LIGHT SENSOR NOT AVAILABLE", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initializeViews(binding: MainFragmentBinding) {
        uvValue = binding.root.findViewById(R.id.message) as TextView
        lineChart = binding.root.findViewById(R.id.lineChart) as LineChart

        initLineChart()
    }

    private fun initLineChart() {
        // Left axis
        lineChart.axisLeft.setDrawGridLines(true)
        lineChart.axisLeft.setStartAtZero(true)

        val limitLine = LimitLine(10000f)
        lineChart.axisLeft.addLimitLine(limitLine)

        // X axis
        val xAxis: XAxis = lineChart.xAxis
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)

        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = MyAxisFormatter()
        xAxis.setDrawLabels(true)
        xAxis.granularity = 1f

        // LineChart parameters
        lineChart.axisRight.isEnabled = false
        lineChart.legend.isEnabled = true
        lineChart.description.isEnabled = false
        lineChart.animateX(1000, Easing.EaseInSine)
        lineChart.setDrawMarkerViews(false)
    }

    private fun updateChartValues() {
        val values: ArrayList<Uv> = uvList
        val entries: ArrayList<Entry> = ArrayList()

        for (i in values.indices) {
            val uv: Uv = values[i]
            entries.add(Entry(i.toFloat(), uv.value))
        }

        val lineDataSet = LineDataSet(entries, "")
        lineDataSet.color = R.color.purple_500
        lineDataSet.setDrawValues(true)
        lineDataSet.setDrawCircles(true)

        val data = LineData(lineDataSet)
        lineChart.data = data

        addChartLegend()

        reDrawChart()
    }

    private fun addChartLegend() {
        val legendEntre = LegendEntry("Light(lux)", Legend.LegendForm.DEFAULT, 10f, 2f, null, R.color.purple_500)
        val legend: Legend = lineChart.legend
        legend.setCustom(arrayOf(legendEntre))
    }

    private fun reDrawChart() {
        lineChart.notifyDataSetChanged()
        lineChart.invalidate()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val luminosity = event?.values?.get(0)
        uvValue.text = luminosity.toString()
        Log.d("lightSensor", luminosity.toString())

        luminosity?.let {
            uvList.add(Uv(it, LocalDateTime.now()))
            updateChartValues()
        }
    }

    override fun onAccuracyChanged(event: Sensor?, p1: Int) {}

    override fun onResume() {
        super.onResume()
        light?.also { light ->
            sensorManager?.registerListener(this, light, 1000000)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

}