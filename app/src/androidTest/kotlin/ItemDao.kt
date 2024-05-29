import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.inventory.data.InventoryDatabase
import com.example.inventory.data.Item
import com.example.inventory.data.ItemDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ItemDao {
    private lateinit var itemDao: ItemDao
    private lateinit var inventoryDatabase: InventoryDatabase

    private var item1 = Item(1, "Apples", 10.0, 20)
    private var item2 = Item(2, "Oranges", 15.0, 97)

    /**
     * In this function, you use an in-memory database and do not persist it on the disk.
     * To do so, you use the inMemoryDatabaseBuilder() function.
     * You do this because the information need not be persisted, but rather,
     * needs to be deleted when the process is killed.
     * You are running the DAO queries in the main thread with .allowMainThreadQueries(),
     * just for testing.
     * */
    @Before
    fun createDb() {
        val context: Context = ApplicationProvider.getApplicationContext()
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        inventoryDatabase =
            Room.inMemoryDatabaseBuilder(context, InventoryDatabase::class.java)
                // Allowing main thread queries, just for testing.
                .allowMainThreadQueries()
                .build()
        itemDao = inventoryDatabase.itemDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        inventoryDatabase.close()
    }

    private suspend fun addOneItemToDb() {
        itemDao.insert(item1)
    }

    private suspend fun addTwoItemsToDb() {
        itemDao.insert(item1)
        itemDao.insert(item2)
    }

    @Test
    @Throws(Exception::class)
    fun daoInsert_insertsItemIntoDb() = runBlocking {
        addOneItemToDb()
        val allItems = itemDao.getAllItems().first()
        assertEquals(allItems[0], item1)
    }

    @Test
    @Throws(Exception::class)
    fun daoInsert_insertsItemsIntoDb() = runBlocking {
        addTwoItemsToDb()
        val allItems = itemDao.getAllItems().first()
        assertEquals(allItems[0], item1)
        assertEquals(allItems[1], item2)
    }

    @Test
    @Throws(Exception::class)
    fun daoUpdate_updatesItemsInDb() = runBlocking {
        addTwoItemsToDb()

        val updatedItem1 = Item(1, "Apples", 15.0, 25)
        val updatedItem2 = Item(2, "Bananas", 5.0, 50)
        itemDao.update(updatedItem1)
        itemDao.update(updatedItem2)

        val allItems = itemDao.getAllItems().first()
        assertEquals(allItems[0], updatedItem1)
        assertEquals(allItems[1], updatedItem2)
    }

    @Test
    @Throws(Exception::class)
    fun daoDelete_deletesItemsInDb() = runBlocking {
        addTwoItemsToDb()
        itemDao.delete(item1)
        itemDao.delete(item2)

        val allItems = itemDao.getAllItems().first()
        assertTrue(allItems.isEmpty())

    }

    @Test
    @Throws(Exception::class)
    fun daoGetItem_returnsItemFromDb() = runBlocking {
        addOneItemToDb()
        val item = itemDao.getItem(1)
        assertEquals(item.first(), item1)
    }
}