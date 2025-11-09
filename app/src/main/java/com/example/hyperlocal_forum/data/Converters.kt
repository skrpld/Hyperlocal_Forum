package com.example.hyperlocal_forum.data

import androidx.room.TypeConverter
import com.example.hyperlocal_forum.data.GeoCoordinates

class Converters {
    @TypeConverter
    fun fromGeoCoordinates(coordinates: GeoCoordinates): String {
        return "${coordinates.latitude},${coordinates.longitude}"
    }

    @TypeConverter
    fun toGeoCoordinates(value: String): GeoCoordinates {
        val parts = value.split(",")
        return GeoCoordinates(parts[0].toDouble(), parts[1].toDouble())
    }
}
