package com.example.voyage_kt.services

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.TextView
import com.example.voyage_kt.R
import com.example.voyage_kt.TravelActivity
import com.example.voyage_kt.UpdateActivity
import com.example.voyage_kt.data.Travel
import com.example.voyage_kt.db.TravelDao
import com.example.voyage_kt.db.TravelRoomDatabase

// Adapter class for populating a ListView with Travel objects
class TravelsListAdapter(private val context: Context, private val voyages: MutableList<Travel>) : BaseAdapter() {

    // Database and DAO instances
    lateinit var db: TravelRoomDatabase
    lateinit var dao: TravelDao

    // Return the number of items in the list
    override fun getCount(): Int {
        return voyages.size
    }

    // Return the item at a specific position
    override fun getItem(position: Int): Any {
        return voyages[position]
    }

    // Return the item ID at a specific position
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    // Create and return a View for each item in the list
    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        // Get the Travel object at the specified position
        val travel = getItem(position) as Travel

        // Inflate the layout for each list item
        val view = LayoutInflater.from(context).inflate(R.layout.list_view_item, parent, false)

        // Find Views within the layout
        val travelTV = view.findViewById<TextView>(R.id.voyageTitle)
        val dateTextView = view.findViewById<TextView>(R.id.voyageDate)
        val descriptionTextView = view.findViewById<TextView>(R.id.voyageDescription)
        val buttonViewDetails = view.findViewById<ImageButton>(R.id.buttonViewDetails)
        val bDeleteTravel = view.findViewById<ImageButton>(R.id.bDeleteTravel)
        val bUpdate = view.findViewById<ImageButton>(R.id.bUpdate)

        // Set the text of TextViews with the corresponding data from the Travel object
        travelTV.text = travel.name
        dateTextView.text = travel.date
        descriptionTextView.text = travel.description

        // Set click listeners for the buttons within each list item
        buttonViewDetails.setOnClickListener {
            // Start the TravelActivity and pass the travelId as an extra
            val intent = Intent(context, TravelActivity::class.java)
            intent.putExtra("travelId", travel.id)
            context.startActivity(intent)
        }

        // Initialize database and DAO instances
        db = TravelRoomDatabase.getInstance(context)
        dao = db.getAllTravels()

        // Delete the selected travel and update the list
        bDeleteTravel.setOnClickListener {
            dao.deleteTravel(travel)
            voyages.remove(travel)
            notifyDataSetChanged()
        }

        // Start the UpdateActivity and pass the travelId as an extra
        bUpdate.setOnClickListener {
            val intentUpdate = Intent(context, UpdateActivity::class.java)
            intentUpdate.putExtra("travelId", travel.id.toString())
            context.startActivity(intentUpdate)
        }

        return view
    }
}
