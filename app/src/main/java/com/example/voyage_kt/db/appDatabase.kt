package com.example.voyage_kt.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.voyage_kt.data.Itinerary
import com.example.voyage_kt.data.Travel

// Define the Room database with the specified entities and version number
@Database(entities = [Travel::class, Itinerary::class], version = 1, exportSchema = false)
abstract class TravelRoomDatabase : RoomDatabase() {

    // Define an abstract method to retrieve the DAO (Data Access Object) for Travel operations
    abstract fun getAllTravels(): TravelDao

    // Companion object to provide a singleton instance of the database
    companion object {
        // Volatile ensures that the instance is always up-to-date for all threads
        @Volatile
        private var instance: TravelRoomDatabase? = null

        // Create and return a singleton instance of the database
        fun getInstance(context: Context): TravelRoomDatabase {
            // Double-checked locking to ensure thread safety
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        // Build the Room database instance
        private fun buildDatabase(context: Context): TravelRoomDatabase {
            return Room.databaseBuilder(
                context,
                TravelRoomDatabase::class.java,
                "TravelRoomDatabaseRoom.db"
            )
                .allowMainThreadQueries() // Note: Allowing queries on the main thread is not recommended for production
                .build()
        }
    }
}
