package com.example.voyage_kt

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.voyage_kt.db.TravelDao
import com.example.voyage_kt.db.TravelRoomDatabase
import com.example.voyage_kt.services.TravelsListAdapter

// Activity to display a list of travels
class TravelsActivity : AppCompatActivity() {

    // Declare variables
    lateinit var db: TravelRoomDatabase
    lateinit var dao: TravelDao

    // Override the onCreate method to initialize the activity
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the content view to the travels activity layout
        setContentView(R.layout.activity_travels)

        // Initialize Room database and TravelDao
        db = TravelRoomDatabase.getInstance(this)
        dao = db.getAllTravels()

        // Retrieve a list of travels from the database
        val travels = dao.getAllTravels().toMutableList()

        // Find the ListView in the layout
        val listViewVoyages = findViewById<ListView>(R.id.list_item_voyage)

        // Create an adapter for the ListView using the custom VoyageListAdapter
        val adapter = TravelsListAdapter(this, travels)

        // Set the adapter for the ListView
        listViewVoyages.adapter = adapter
    }
}
