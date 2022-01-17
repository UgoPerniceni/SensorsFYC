package fr.esgi.sensorsfyc.domain

data class StatisticalSample(
    val localDateTime: String,
    val measurementName: String,
    val unit: String,
    val value: Number
)
