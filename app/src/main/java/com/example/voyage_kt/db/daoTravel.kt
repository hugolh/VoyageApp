package com.example.voyage_kt.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.voyage_kt.data.Travel

// Data Access Object (DAO) interface for Travel entities
@Dao
interface TravelDao {

    // Query to get all travels from the database
    @Query("SELECT * FROM travels")
    fun getAllTravels(): List<Travel>

    // Query to find a travel by its unique identifier (id)
    @Query("SELECT * FROM travels WHERE id = :id")
    fun findTravelById(id: Long): Travel

    // Insert a new travel into the database
    @Insert
    fun insertTravel(travel: Travel)

    // Delete a travel from the database
    @Delete
    fun deleteTravel(travel: Travel)

    // Update an existing travel in the database
    @Update
    fun updateTravel(travel: Travel)
}
