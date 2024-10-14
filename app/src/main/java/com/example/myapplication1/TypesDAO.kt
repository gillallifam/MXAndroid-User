import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ProductDao {
    @Query("SELECT * FROM Products")
    fun getAll(): List<Product>

    @Insert
    fun insertProduct(vararg product: Product)

    @Delete
    fun delete(product: Product)
}