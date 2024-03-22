package com.example.voyage_kt

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.voyage_kt.data.Travel
import com.example.voyage_kt.db.TravelDao
import com.example.voyage_kt.db.TravelRoomDatabase
import com.example.voyage_kt.services.PlacesRecyclerAdapter
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
import kotlin.properties.Delegates

// Activity to display details of a specific travel
class TravelActivity : AppCompatActivity() {

    // Declare variables
    lateinit var travel: Travel
    private var travelId by Delegates.notNull<Long>()
    lateinit var db: TravelRoomDatabase
    lateinit var dao: TravelDao
    private lateinit var mapViewTravel: MapView
    private lateinit var marker: Bitmap
    private var currentPointIndex: Int = 0
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PlacesRecyclerAdapter
    private lateinit var client: MapboxDirections
    private var currentRoute: DirectionsRoute? = null
    private val ROUTE_SOURCE_ID = "my-source-id"

    // Override the onCreate method to initialize the activity
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the content view to the travel activity layout
        setContentView(R.layout.activity_travel)

        // Initialize variables and retrieve travel details from the intent
        db = TravelRoomDatabase.getInstance(this)
        dao = db.getAllTravels()
        travelId = intent.getLongExtra("travelId", 0)
        travel = dao.findTravelById(travelId)
        mapViewTravel = findViewById(R.id.mvTravelVis)

        // Add travel itinerary points to the map
        addTravelItinerary()
        // Set up RecyclerView to display places in the travel itinerary
        recyclerView = findViewById(R.id.recyclerView)
        adapter = PlacesRecyclerAdapter(applicationContext, travel.itineraries, false)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Find views for travel details
        val textViewTitle = findViewById<TextView>(R.id.tvTravelTitle)
        val textViewDesc = findViewById<TextView>(R.id.tvDesc)
        val textViewDate = findViewById<TextView>(R.id.tvDate)
        textViewTitle.text = travel.name
        textViewDesc.text = travel.description
        textViewDate.text = travel.date
        // Set up MapView to display the travel itinerary on the map
       val initialCameraOptions = CameraOptions.Builder()
            .center(
                Point.fromLngLat(travel.itineraries[0].longitude, travel.itineraries[0].latitude)
            )
            .pitch(45.0)
            .zoom(10.5)
            .build()
        mapViewTravel.getMapboxMap().setCamera(initialCameraOptions)

        // Set up button to navigate to the next point in the itinerary
        val bNextPoint = findViewById<ImageButton>(R.id.bNextPlace)
        bNextPoint.setOnClickListener {
            goNextPoint()
        }

        val listPoint: MutableList<Point> = mutableListOf()
        // Display latitude and longitude for each point in the travel itinerary (for debugging)
        travel.itineraries.forEach { it ->
            listPoint.add(Point.fromLngLat(it.longitude, it.latitude))
        }

        getRoute(listPoint)
    }

    // Function to add travel itinerary points to the map
    private fun addTravelItinerary() {
        val annotationApi = mapViewTravel.annotations
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

    // Function to navigate to the next point in the travel itinerary
    private fun goNextPoint() {
        if (currentPointIndex < travel.itineraries.size - 1) {
            // Increment the current index
            currentPointIndex++

            // Get the coordinates of the next point in the list
            val nextPoint = travel.itineraries[currentPointIndex]

            // Center the camera on the next point
            val nextCameraOptions = CameraOptions.Builder()
                .center(Point.fromLngLat(nextPoint.longitude, nextPoint.latitude))
                .build()

            mapViewTravel.getMapboxMap().setCamera(nextCameraOptions)
        } else {
            // If reached the end of the itinerary, go back to the first point
            currentPointIndex = 0
            val nextPoint = travel.itineraries[currentPointIndex]

            // Center the camera on the first point
            val nextCameraOptions = CameraOptions.Builder()
                .center(Point.fromLngLat(nextPoint.longitude, nextPoint.latitude))
                .build()
            mapViewTravel.getMapboxMap().setCamera(nextCameraOptions)
        }
    }

    // Function to get a route between points using Mapbox Directions API
    private fun getRoute(points: MutableList<Point>) {
        if (points.size < 2) {
            // There must be at least two points to create a route
            return
        }

        // Set up Mapbox Directions API client
        client = MapboxDirections.builder()
            .accessToken("pk.eyJ1IjoiaHVnb2xoIiwiYSI6ImNsbms5enJ4MDA1Mjkya3BiZXd0MmNibmwifQ.5_COIXK9om2af_kbPpnL1g")
            .routeOptions(
                RouteOptions.builder()
                    .coordinatesList(points)
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

    // Function to draw the route on the map
    private fun drawRoute(route: DirectionsRoute) {
        mapViewTravel.getMapboxMap().getStyle { style ->
            style.addSource(geoJsonSource(ROUTE_SOURCE_ID) {
                geometry(LineString.fromPolyline(route.geometry()!!, Constants.PRECISION_6))
            })

            val lineLayer = LineLayer("my-line-layer-id", ROUTE_SOURCE_ID)
            lineLayer.lineColor("#FF0000")
            lineLayer.lineWidth(2.0)

            style.addLayer(lineLayer)
        }
    }

    // Lifecycle method onStart
    @SuppressLint("Lifecycle")
    override fun onStart() {
        super.onStart()
        mapViewTravel.onStart()
    }

    // Lifecycle method onStop
    @SuppressLint("Lifecycle")
    override fun onStop() {
        super.onStop()
        mapViewTravel.onStop()
    }

    // Lifecycle method onDestroy
    @SuppressLint("Lifecycle")
    override fun onDestroy() {
        super.onDestroy()
        // Cancel the Directions API request
        if (::client.isInitialized) {
            client.cancelCall()
        }
        mapViewTravel.onDestroy()
    }

    // Lifecycle method onLowMemory
    @SuppressLint("Lifecycle")
    override fun onLowMemory() {
        super.onLowMemory()
        mapViewTravel.onLowMemory()
    }
}
