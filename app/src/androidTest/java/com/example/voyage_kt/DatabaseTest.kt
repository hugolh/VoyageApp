import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voyage_kt.data.Itinerary
import com.example.voyage_kt.data.Travel
import com.example.voyage_kt.db.TravelDao
import com.example.voyage_kt.db.TravelRoomDatabase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TravelDaoTest {

    private lateinit var database: TravelRoomDatabase
    private lateinit var travelDao: TravelDao

    @Before
    fun setup() {
        // Use an in-memory database for testing
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TravelRoomDatabase::class.java
        ).build()

        travelDao = database.getAllTravels()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetTravel() {
        // Insert a travel into the database
        val itineraryList = mutableListOf(
            Itinerary(id = 1, name = "it1", description = "d1", latitude = 0.0, longitude = 0.0),
            Itinerary(id = 2, name = "it2", description = "d2", latitude = 0.0, longitude = 0.0)
        )

        val travel = Travel(
            id = 1,
            name = "Test Voyage",
            date = "2023-11-09",
            description = "Test Message",
            itineraries = itineraryList
        )

        travelDao.insertTravel(travel)

        // Retrieve the travel by its ID
        val loaded = travelDao.findTravelById(1)

        // Verify that the retrieved travel matches the inserted one
        assert(loaded != null)
        assert(loaded?.id == travel.id)
        assert(loaded?.name == travel.name)
        assert(loaded?.date == travel.date)
        assert(loaded?.description == travel.description)
    }

    @Test
    fun updateTravel() {
        // Insert a travel into the database
        val itineraryList = mutableListOf(
            Itinerary(id = 1, name = "it1", description = "d1", latitude = 0.0, longitude = 0.0),
            Itinerary(id = 2, name = "it2", description = "d2", latitude = 0.0, longitude = 0.0)
        )

        val travel = Travel(
            id = 1,
            name = "Test Voyage",
            date = "2023-11-09",
            description = "Test Message",
            itineraries = itineraryList
        )

        val newTravel = Travel(
            id = 1,
            name = "New Test Voyage",
            date = "2024-11-09",
            description = "New Test Message",
            itineraries = itineraryList
        )

        travelDao.insertTravel(travel)

        // Update the travel in the database
        travelDao.updateTravel(newTravel)

        // Retrieve the updated travel by its ID
        val loaded = travelDao.findTravelById(1)

        // Verify that the retrieved travel matches the new travel
        assert(loaded != null)
        assert(loaded == newTravel)
    }

    @Test
    fun deleteTravel() {
        // Insert a travel into the database
        val itineraryList = mutableListOf(
            Itinerary(id = 1, name = "it1", description = "d1", latitude = 0.0, longitude = 0.0),
            Itinerary(id = 2, name = "it2", description = "d2", latitude = 0.0, longitude = 0.0)
        )

        val travel = Travel(
            id = 1,
            name = "Test Voyage",
            date = "2023-11-09",
            description = "Test Message",
            itineraries = itineraryList
        )

        travelDao.insertTravel(travel)
        travelDao.deleteTravel(travel)

        // Retrieve the travel by its ID
        val loaded = travelDao.findTravelById(1)

        // Verify that the retrieved travel is null after deletion
        assert(loaded == null)
    }
}
