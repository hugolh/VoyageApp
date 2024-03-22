package com.example.voyage_kt

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.voyage_kt.data.Itinerary
import com.example.voyage_kt.data.Travel
import com.example.voyage_kt.db.TravelDao
import com.example.voyage_kt.db.TravelRoomDatabase
import com.example.voyage_kt.services.LocationHandler
import com.google.android.material.textfield.TextInputEditText
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.core.constants.Constants
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.LineLayer
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Activity for creating a new travel with associated itineraries
class FormTravelActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var marker: Bitmap
    private lateinit var db: TravelRoomDatabase
    private lateinit var dao: TravelDao
    private var newTravel: Travel = Travel(0L, "", "", "", mutableListOf())
    private var listIt: MutableList<Itinerary> = mutableListOf()
    private val calendar = Calendar.getInstance()
    private lateinit var tvSelectedDate: TextView
    private lateinit var locationHandler: LocationHandler
    private lateinit var client: MapboxDirections
    private lateinit var oldPoint: Point
    private  var index = 1
    private val ROUTE_SOURCE_ID = "my-source-id"

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_travel)
        mapView = findViewById(R.id.mvTravel)
        locationHandler = LocationHandler(this)

        if(ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        {
            val userPosition : Location? =locationHandler.getLocation()
            val initialCameraOptions = CameraOptions.Builder()
                .center(userPosition?.let { Point.fromLngLat(userPosition.longitude, userPosition.latitude) })
                .zoom(10.5)
                .build()

            if (userPosition != null) {
                Toast.makeText(applicationContext, userPosition.longitude.toString(), Toast.LENGTH_LONG).show()
            }

            mapView.getMapboxMap().setCamera(initialCameraOptions)
        }
        else
        {
            val initialCameraOptions = CameraOptions.Builder()
                .center(Point.fromLngLat(2.333333, 48.866667,))
                .zoom(1.5)
                .build()

            mapView.getMapboxMap().setCamera(initialCameraOptions)
        }


        marker = bitmapFromDrawableRes(this, R.drawable.marker)
        tvSelectedDate = findViewById(R.id.tvSelectedDate)

        val btnDatePicker = findViewById<Button>(R.id.btnDatePicker)
        btnDatePicker.setOnClickListener {
            // Show the DatePicker dialog
            showDatePicker()
        }
        val bAddIt = findViewById<Button>(R.id.bAddIt)
        bAddIt.setOnClickListener {
            // Launch FormItinaryActivity to add a new itinerary
            val intent = Intent(this, FormItinaryActivity::class.java)
            startActivityForResult(intent, NEW_IT_REQUEST_CODE)
        }
        val submitButton = findViewById<Button>(R.id.bSubmitTravelButton)
        submitButton.setOnClickListener {
            // Retrieve input values from the form
            val nameEditText = findViewById<TextInputEditText>(R.id.tnName)
            val dateEditText = tvSelectedDate
            val descriptionEditText = findViewById<TextInputEditText>(R.id.tnMessage)

            // Set values for a new Travel instance
            val id: Long = 0L
            val name = nameEditText.text.toString()
            val date = dateEditText.text.toString()
            val message = descriptionEditText.text.toString()
            if(name != "" && listIt.size > 0)
            {
                newTravel = Travel(id, name, date, message, listIt)


                // Insert the new travel into the Room database
                db = TravelRoomDatabase.getInstance(this)
                dao = db.getAllTravels()
                dao.insertTravel(newTravel)

                // Navigate back to the main activity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                // Display a toast message indicating successful creation of the travel
                Toast.makeText(applicationContext, "Voyage créé", Toast.LENGTH_LONG).show()
            }
            else{
                Toast.makeText(applicationContext, "Le voyage a besoin d'un nom", Toast.LENGTH_LONG).show()
            }

        }
    }

    // Handle the result from FormItinaryActivity
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
                // Create a new itinerary and add it to the list
                val itinerary = Itinerary(0L, name, description, latitude, longitude)
                listIt.add(itinerary)
                // Update the map with the new itinerary
                addTravelItinary()

                if (listIt.size >= 2)
                {
                    val lastIndex = listIt.last()
                    getRoute(
                        Point.fromLngLat(oldPoint.longitude(), oldPoint.latitude()),
                        Point.fromLngLat(listIt.last().longitude, listIt.last().latitude)
                    )
                }
                oldPoint = Point.fromLngLat(listIt.last().longitude, listIt.last().latitude)

            } else {
                // Display a toast message indicating invalid data
                Toast.makeText(applicationContext, R.string.invalid_data, Toast.LENGTH_LONG).show()
            }
        } else {
            // Display a toast message indicating that the data was not saved
            Toast.makeText(applicationContext, R.string.not_saved, Toast.LENGTH_LONG).show()
        }
    }

    // Add a new itinerary to the map
    private fun addTravelItinary() {
        val annotationApi = mapView.annotations
        val pointAnnotationManager = annotationApi.createPointAnnotationManager()
        pointAnnotationManager.deleteAll()

        // Get the last added itinerary's coordinates
        val point = Point.fromLngLat(listIt.last().longitude, listIt.last().latitude)

        // Create PointAnnotationOptions for the new itinerary
        val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
            .withPoint(point)
            .withIconImage(marker)
            .withIconAnchor(IconAnchor.BOTTOM)

        // Create and display the new itinerary on the map
        pointAnnotationManager.create(pointAnnotationOptions)

        // Set the initial camera position to focus on the last added itinerary
        val initialCameraOptions = CameraOptions.Builder()
            .center(Point.fromLngLat(listIt.last().longitude, listIt.last().latitude))
            .zoom(10.5)
            .build()

        mapView.getMapboxMap().setCamera(initialCameraOptions)
    }

    // Show a DatePicker dialog to select a date
    private fun showDatePicker() {
        // Create a DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            this, { DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                // Create a new Calendar instance to hold the selected date
                val selectedDate = Calendar.getInstance()
                // Set the selected date using the values received from the DatePicker dialog
                selectedDate.set(year, monthOfYear, dayOfMonth)
                // Create a SimpleDateFormat to format the date as "dd/MM/yyyy"
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                // Format the selected date into a string
                val formattedDate = dateFormat.format(selectedDate.time)
                // Update the TextView to display the selected date with the "Selected Date: " prefix
                tvSelectedDate.text = formattedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        // Show the DatePicker dialog
        datePickerDialog.show()
    }

    // Function to get a route between points using Mapbox Directions API
    private fun getRoute(origin: Point, destination: Point) {


        // Set up Mapbox Directions API client
        client = MapboxDirections.builder()
            .accessToken("pk.eyJ1IjoiaHVnb2xoIiwiYSI6ImNsbms5enJ4MDA1Mjkya3BiZXd0MmNibmwifQ.5_COIXK9om2af_kbPpnL1g")
            .routeOptions(
                RouteOptions.builder()
                    .coordinatesList(
                      listOf( origin, destination)

                    )
                    .profile(DirectionsCriteria.PROFILE_DRIVING_TRAFFIC)
                    .overview(DirectionsCriteria.OVERVIEW_FULL)
                    .build()
            )
            .build()

        // Enqueue the Directions API call
        client.enqueueCall(object : Callback<DirectionsResponse> {
            @SuppressLint("SuspiciousIndentation")
            override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
                if (response.isSuccessful) {
                    if (response.body() == null) {
                        return
                    } else if (response.body()?.routes()?.size ?: 0 < 1) {
                        return
                    }

                    val routes = response.body()?.routes() ?: emptyList()

                    // Draw the route for each itinerary in the list
                    for (route in routes) {
                        drawRoute(route)
                    }
                }
            }

            override fun onFailure(call: Call<DirectionsResponse>, throwable: Throwable) {
                // Handle failure
            }
        })
    }

    private fun drawRoute(route: DirectionsRoute) {
        mapView.getMapboxMap().getStyle { style ->
            val sourceId = ROUTE_SOURCE_ID + index
            val layerId = "my-line-layer-id$index"
            index ++
                // La source n'existe pas encore, ajoutez-la au style
                style.addSource(geoJsonSource(sourceId) {
                    geometry(LineString.fromPolyline(route.geometry()!!, Constants.PRECISION_6))
                })

                val lineLayer = LineLayer(layerId, sourceId)
                lineLayer.lineColor("#FF0000")
                lineLayer.lineWidth(2.0)

                style.addLayer(lineLayer)

        }
    }


    companion object {
        const val NEW_IT_REQUEST_CODE = 1
    }
}
