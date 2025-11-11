package com.example.hyperlocal_forum.data

import kotlin.math.*

object GeoUtils {
    private const val EARTH_RADIUS_KM = 6371.0
    private const val BITS_PER_CHAR = 5
    private val BASE32_CHARS = "0123456789bcdefghjkmnpqrstuvwxyz".toCharArray()
    private val BASE32_DECODE_MAP = BASE32_CHARS
        .withIndex()
        .associate { it.value to it.index }

    fun distanceBetween(start: GeoCoordinates, end: GeoCoordinates): Double {
        val dLat = Math.toRadians(end.latitude - start.latitude)
        val dLon = Math.toRadians(end.longitude - start.longitude)
        val lat1 = Math.toRadians(start.latitude)
        val lat2 = Math.toRadians(end.latitude)

        val a = sin(dLat / 2).pow(2) + sin(dLon / 2).pow(2) * cos(lat1) * cos(lat2)
        val c = 2 * asin(sqrt(a))
        return EARTH_RADIUS_KM * c
    }

    fun getGeoHashForLocation(location: GeoCoordinates, precision: Int = 9): String {
        var latRange = doubleArrayOf(-90.0, 90.0)
        var lonRange = doubleArrayOf(-180.0, 180.0)
        val geohash = StringBuilder()
        var isEvenBit = true
        val totalBits = precision * BITS_PER_CHAR
        var bit = 0
        var ch = 0

        while (geohash.length < precision) {
            val mid: Double
            if (isEvenBit) {
                mid = (lonRange[0] + lonRange[1]) / 2
                if (location.longitude > mid) {
                    ch = ch or (1 shl (BITS_PER_CHAR - 1 - (bit % BITS_PER_CHAR)))
                    lonRange[0] = mid
                } else {
                    lonRange[1] = mid
                }
            } else {
                mid = (latRange[0] + latRange[1]) / 2
                if (location.latitude > mid) {
                    ch = ch or (1 shl (BITS_PER_CHAR - 1 - (bit % BITS_PER_CHAR)))
                    latRange[0] = mid
                } else {
                    latRange[1] = mid
                }
            }
            isEvenBit = !isEvenBit
            bit++

            if (bit % BITS_PER_CHAR == 0) {
                geohash.append(BASE32_CHARS[ch])
                ch = 0
            }
        }
        return geohash.toString()
    }

    fun getNearbyGeoHashes(location: GeoCoordinates, radiusKm: Double): List<String> {
        val precision = getPrecisionForRadius(radiusKm)
        val centerHash = getGeoHashForLocation(location, precision)

        val (lat, lon) = decodeGeoHash(centerHash)

        val neighbors = mutableSetOf(centerHash)

        val latStep = (90.0 - (-90.0)) / (1 shl (precision * BITS_PER_CHAR / 2))
        val lonStep = (180.0 - (-180.0)) / (1 shl (precision * BITS_PER_CHAR / 2 + if (precision * BITS_PER_CHAR % 2 == 1) 1 else 0))

        // N, S, E, W
        neighbors.add(getGeoHashForLocation(GeoCoordinates(lat + latStep, lon), precision))
        neighbors.add(getGeoHashForLocation(GeoCoordinates(lat - latStep, lon), precision))
        neighbors.add(getGeoHashForLocation(GeoCoordinates(lat, lon + lonStep), precision))
        neighbors.add(getGeoHashForLocation(GeoCoordinates(lat, lon - lonStep), precision))
        // NE, NW, SE, SW
        neighbors.add(getGeoHashForLocation(GeoCoordinates(lat + latStep, lon + lonStep), precision))
        neighbors.add(getGeoHashForLocation(GeoCoordinates(lat + latStep, lon - lonStep), precision))
        neighbors.add(getGeoHashForLocation(GeoCoordinates(lat - latStep, lon + lonStep), precision))
        neighbors.add(getGeoHashForLocation(GeoCoordinates(lat - latStep, lon - lonStep), precision))

        return neighbors.toList()
    }

    private fun decodeGeoHash(geohash: String): GeoCoordinates {
        var latRange = doubleArrayOf(-90.0, 90.0)
        var lonRange = doubleArrayOf(-180.0, 180.0)
        var isEvenBit = true

        for (c in geohash) {
            val cd = BASE32_DECODE_MAP[c]!!
            for (i in 0 until BITS_PER_CHAR) {
                val mask = 1 shl (BITS_PER_CHAR - 1 - i)
                if (isEvenBit) {
                    if ((cd and mask) != 0) {
                        lonRange[0] = (lonRange[0] + lonRange[1]) / 2
                    } else {
                        lonRange[1] = (lonRange[0] + lonRange[1]) / 2
                    }
                } else {
                    if ((cd and mask) != 0) {
                        latRange[0] = (latRange[0] + latRange[1]) / 2
                    } else {
                        latRange[1] = (latRange[0] + latRange[1]) / 2
                    }
                }
                isEvenBit = !isEvenBit
            }
        }
        return GeoCoordinates((latRange[0] + latRange[1]) / 2, (lonRange[0] + lonRange[1]) / 2)
    }

    private fun getPrecisionForRadius(radiusKm: Double): Int {
        return when {
            radiusKm <= 0.075 -> 9
            radiusKm <= 0.6 -> 8
            radiusKm <= 2.4 -> 7 // 1km
            radiusKm <= 20 -> 6
            radiusKm <= 78 -> 5
            radiusKm <= 630 -> 4
            else -> 3
        }
    }
}