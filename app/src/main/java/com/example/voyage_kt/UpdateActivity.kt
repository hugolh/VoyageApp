package com.example.voyage_kt

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.text.Editable
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.voyage_kt.data.Itinerary
import com.example.voyage_kt.data.Travel
import com.example.voyage_kt.db.TravelDao
import com.example.voyage_kt.db.TravelRoomDatabase
import com.example.voyage_kt.services.PlacesRecyclerAdapter
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager

class UpdateActivity : AppCompatActivity() {
    lateinit var db: TravelRoomDatabase
    lateinit var dao: TravelDao
    private lateinit var recyclerView: RecyclerView
    lateinit var adapter: PlacesRecyclerAdapter
    lateinit var travel: Travel
    lateinit var mapView: MapView
    private lateinit var marker: Bitmap

    @SuppressLint("SuspiciousIndentation", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update)

        // Initialize UI elements
        val etName = findViewById<EditText>(R.id.etName)
        val etDesc = findViewById<EditText>(R.id.etDesc)
        val etDate = findViewById<EditText>(R.id.etDate)
        val travelId: String = intent.getStringExtra("travelId").toString()
        mapView = findViewById(R.id.mvTravelSec)

        // Initialize database and retrieve travel details
        db = TravelRoomDatabase.getInstance(this)
        dao = db.getAllTravels()
        travel = dao.findTravelById(travelId.toLong())

        // Set initial values for EditText fields
        etName.text = Editable.Factory.getInstance().newEditable(travel.name)
        etDesc.text = Editable.Factory.getInstance().newEditable(travel.description)
        etDate.text = Editable.Factory.getInstance().newEditable(travel.date)

        // Set up RecyclerView to display places in the travel itinerary
        recyclerView = findViewById(R.id.rvPlaces)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PlacesRecyclerAdapter(this, travel.itineraries, true)
        recyclerView.adapter = adapter

        // Set up button to apply updates to the travel details
        val bUpdateApply: Button = findViewById(R.id.bUpdateApply)
        bUpdateApply.setOnClickListener {
            // Update travel details and navigate to the TravelsActivity
            travel.name = etName.text.toString()
            travel.description = etDesc.text.toString()
            travel.date = etDate.text.toString()
            dao.updateTravel(travel)
            val intent = Intent(this, TravelsActivity::class.java)
            startActivity(intent)
        }

        // Set initial camera position on the map
        val initialCameraOptions = CameraOptions.Builder()
            .center(
                Point.fromLngLat(travel.itineraries[0].longitude, travel.itineraries[0].latitude)
            )
            .pitch(45.0)
            .zoom(10.5)
            .build()
        mapView.getMapboxMap().setCamera(initialCameraOptions)

        // Add travel itinerary points to the map
        addTravelItinerary()
    }

    // Function to add travel itinerary points to the map
    private fun addTravelItinerary() {
        val annotationApi = mapView.annotations
        val pointAnnotationManager = annotationApi.createPointAnnotationManager()
        pointAnnotationManager.deleteAll()
        var index = 1
        travel.itineraries.forEach { it ->

            marker = bitmapFromDrawableResString(this, R.drawable.marker, index.toString())
            val point = Point.fromLngLat(it.longitude, it.latitude)
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(point)
                .withIconImage(marker)
                .withIconAnchor(IconAnchor.BOTTOM)
            pointAnnotationManager.create(pointAnnotationOptions)
            index++
        }
    }

    // Notify the adapter when a new itinerary is added
    @SuppressLint("NotifyDataSetChanged")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == FormTravelActivity.NEW_IT_REQUEST_CODE) {
            // Retrieve data from the result intent
            val name: String? = data?.getStringExtra(FormItinaryActivity.NEW_TITLE)
            val description: String? = data?.getStringExtra(FormItinaryActivity.NEW_DESCRIPTION)
            val default: Double = 0.0
            val longitude: Double? = data?.getDoubleExtra("longitude", default)
            val latitude: Double? = data?.getDoubleExtra("latitude", default)

            // Check if the required data is not null
            if (name != null && description != null && longitude != null && latitude != null) {
                // Create a new itinerary and add it to the travel
                val itinerary = Itinerary(0L, name, description, latitude, longitude)
                travel.itineraries.add(itinerary)
                adapter.notifyDataSetChanged()
            } else {
                // Display a toast message indicating invalid data
                Toast.makeText(applicationContext, R.string.invalid_data, Toast.LENGTH_LONG).show()
            }
        } else {
            // Display a toast message indicating that the data was not saved
            Toast.makeText(applicationContext, R.string.not_saved, Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        const val NEW_IT_REQUEST_CODE = 2
    }

}
fun bitmapFromDrawableResString(context: Context, @DrawableRes resourceId: Int, text: String): Bitmap {
    val sourceDrawable = AppCompatResources.getDrawable(context, resourceId)!!

    return if (sourceDrawable is BitmapDrawable) {
        val bitmap = sourceDrawable.bitmap
        addTextToBitmap(bitmap, text)
    } else {
        val constantState = sourceDrawable.constantState
        val drawable = constantState!!.newDrawable().mutate()

        // Augmenter la hauteur du canevas de base

        val bitmap: Bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth+55 ,
            drawable.intrinsicHeight ,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width-55, canvas.height)
        drawable.draw(canvas)

        addTextToBitmap(bitmap, text)
    }
}

  fun addTextToBitmap(bitmap: Bitmap, text: String): Bitmap {
    val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(mutableBitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.color = Color.BLACK
    paint.textSize = 40f
    paint.textAlign = Paint.Align.CENTER

    // Calculate the position to center the text on the bitmap
    val x = canvas.width / 2f+ 20
    val y = (canvas.height / 2f - (paint.descent() + paint.ascent()) / 2)
    // Draw the text on the bitmap
    canvas.drawText(text, x, y, paint)

    return mutableBitmap
}
