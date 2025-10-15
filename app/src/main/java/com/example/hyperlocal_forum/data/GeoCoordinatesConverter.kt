package com.example.hyperlocal_forum.data

import androidx.room.TypeConverter

class GeoCoordinatesConverter {
    @TypeConverter
    fun fromCoordinates(coordinates: GeoCoordinates?): String? {
        return coordinates?.let { "${it.latitude},${it.longitude}" }
    }

    @TypeConverter
    fun toCoordinates(data: String): GeoCoordinates {
        val parts = data.split(',')
        return GeoCoordinates(parts[0].toDouble(), parts[1].toDouble())
    }
}