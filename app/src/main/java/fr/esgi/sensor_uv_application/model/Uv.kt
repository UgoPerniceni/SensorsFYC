package fr.esgi.sensor_uv_application.model

import java.time.LocalDateTime

data class Uv(
    val value: Float,
    val datetime: LocalDateTime,
)