package ai.folded.fitstyle.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StyledImageDao {

    @Query("SELECT * FROM styled_images WHERE id = :id")
    fun get(id: String): Flow<StyledImage>

    @Query("SELECT * FROM styled_images ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<StyledImage>>

    @Insert
    suspend fun insert(styledImage: StyledImage): Long

    @Update
    suspend fun update(styledImage: StyledImage)

    @Query("DELETE FROM styled_images")
    suspend fun clear()
}