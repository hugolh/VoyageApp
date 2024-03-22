package com.example.voyage_kt.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.app.ActivityCompat

class LocationHandler(private val activity: Activity) {

    // Function to request permission and retrieve the location
    fun requestLocation() {
        // Check if location permission is granted
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission already granted
        } else {
            // Request permission from the user
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    // Function to retrieve the location
    @SuppressLint("MissingPermission")
    fun getLocation(): Location? {
        val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Verify if loc is accepted
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

        } else {
            // Permission is not allowed, return null
            return null
        }
    }


    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}
