package com.example.voyage_kt

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import com.example.voyage_kt.services.LocationHandler
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.addOnMapClickListener

// Activity for adding new itinerary points
class FormItinaryActivity : AppCompatActivity() {

    private lateinit var marker: Bitmap
    private lateinit var mapView: MapView
    private var pointAnnotationManager: PointAnnotationManager? = null
    private var existingPointAnnotation: PointAnnotation? = null
    private var lat: Double = 0.0
    private var long: Double = 0.0
    private lateinit var locationHandler: LocationHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_itinary)

        mapView = findViewById(R.id.mvItinerary)
        marker = bitmapFromDrawableRes(this, R.drawable.marker)

        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {
            // Add a listener for map clicks
            mapView.getMapboxMap().addOnMapClickListener { point ->
                onMapClick(point)
            }
        }
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

        val submitItinerary = findViewById<Button>(R.id.bSubmitItinerary)
        val titre = findViewById<EditText>(R.id.tnName)
        val description = findViewById<EditText>(R.id.tnMessageIt)

        submitItinerary.setOnClickListener {
            // Create an intent to pass back to the calling activity
            val resultIntent = Intent()

            // Check if required fields are empty
            if (TextUtils.isEmpty(titre.text) || TextUtils.isEmpty(description.text)) {
                setResult(Activity.RESULT_CANCELED)
            } else {
                // Retrieve values from the form
                val newTitre = titre.text.toString()
                val newDescription: String = description.text.toString()

                // Add data to the intent
                resultIntent.putExtra("longitude", long)
                resultIntent.putExtra("latitude", lat)
                resultIntent.putExtra(NEW_TITLE, newTitre)
                resultIntent.putExtra(NEW_DESCRIPTION, newDescription)

                // Set the result and finish the activity
                setResult(Activity.RESULT_OK, resultIntent)
            }
            finish()
        }
    }

    // Handle map clicks
    private fun onMapClick(point: Point): Boolean {
        if (pointAnnotationManager == null) {
            val annotationApi = mapView.annotations
            pointAnnotationManager = annotationApi.createPointAnnotationManager()
        }

        if (existingPointAnnotation != null) {
            existingPointAnnotation?.point = point
            pointAnnotationManager?.update(existingPointAnnotation!!)
            lat = point.latitude()
            long = point.longitude()
        } else {
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(point)
                .withIconImage(marker)
                .withIconAnchor(IconAnchor.BOTTOM)

            existingPointAnnotation = pointAnnotationManager?.create(pointAnnotationOptions)
            lat = point.latitude()
            long = point.longitude()
        }
        return true
    }

    companion object {
        const val NEW_TITLE: String = "new author"
        const val NEW_DESCRIPTION: String = "new Book"
    }
}

// Function to create a Bitmap from a drawable resource
fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int): Bitmap {
    val sourceDrawable = AppCompatResources.getDrawable(context, resourceId)!!

    return if (sourceDrawable is BitmapDrawable) {
        sourceDrawable.bitmap
    } else {
        val constantState = sourceDrawable.constantState
        val drawable = constantState!!.newDrawable().mutate()
        val bitmap: Bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bitmap
    }
}
