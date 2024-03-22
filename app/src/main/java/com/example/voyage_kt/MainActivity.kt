package com.example.voyage_kt

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.voyage_kt.services.LocationHandler

// Main activity that serves as the entry point for the application
class MainActivity : AppCompatActivity() {
    private lateinit var locationHandler: LocationHandler
    // Override the onCreate method to initialize the activity
    @SuppressLint("MissingInflatedId", "SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the content view to the main activity layout
        setContentView(R.layout.activity_main)
        locationHandler = LocationHandler(this)
        locationHandler.requestLocation()

        // Find and set up the "Create Travel" button
        val bCreateTravel = findViewById<Button>(R.id.bCreateTravel)
        bCreateTravel.setOnClickListener {
            // Launch the FormTravelActivity to create a new travel
            val intent = Intent(this, FormTravelActivity::class.java)
            startActivity(intent)
        }

        // Find and set up the "All Travels" button
        val bAllTravel = findViewById<Button>(R.id.bAllTravels)
        bAllTravel.setOnClickListener {
            // Launch the TravelsActivity to view all travels
            val intent = Intent(this, TravelsActivity::class.java)
            startActivity(intent)
        }
    }
}
