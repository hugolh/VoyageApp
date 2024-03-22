package com.example.voyage_kt.services

import androidx.room.TypeConverter
import com.example.voyage_kt.data.Itinerary
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Type converter class to convert Lists of Itinerary objects to and from JSON strings
class ItineraryListConverter {

    // Convert a List<Itinerary> to a JSON string
    @TypeConverter
    fun listToJson(value: List<Itinerary>?): String {
        return Gson().toJson(value)
    }

    // Convert a JSON string to a List<Itinerary>
    @TypeConverter
    fun jsonToList(value: String): List<Itinerary>? {
        // Use Gson to parse the JSON string into a List<Itinerary>
        val listType = object : TypeToken<List<Itinerary>>() {}.type
        return Gson().fromJson(value, listType)
    }
}
