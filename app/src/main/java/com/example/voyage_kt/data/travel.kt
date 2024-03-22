package com.example.voyage_kt.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.voyage_kt.services.ItineraryListConverter

@Entity(tableName = "travels")
@TypeConverters(ItineraryListConverter::class)
data class Travel(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0, // Unique identifier of the travel (auto-generated)

    @ColumnInfo(name = "name")
    var name: String, // Title of the travel

    @ColumnInfo(name = "date")
    var date: String, // Date and time of the travel (in String format)

    @ColumnInfo(name = "description")
    var description: String, // Message content / description

    @ColumnInfo(name = "itinerary")
    var itineraries: MutableList<Itinerary>
)

@Entity(tableName = "itinerary")
data class Itinerary(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long, // Unique identifier of the message (auto-generated)

    @ColumnInfo(name = "name")
    val name: String, // Location of the message

    @ColumnInfo(name = "description")
    val description: String, // Location of the message

    @ColumnInfo(name = "latitude")
    val latitude: Double, // Latitude of the location

    @ColumnInfo(name = "longitude")
    val longitude: Double // Longitude of the location
)
