import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ProductDao {
    @Query("SELECT * FROM Product")
    fun getAll(): List<Product>

    @Insert
    fun insertProduct(vararg prod: Product)

    @Delete
    fun delete(prod: Product)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM User")
    fun getAll(): List<User>

    @Insert
    fun insertProduct(vararg user: User)

    @Delete
    fun delete(user: User)
}

