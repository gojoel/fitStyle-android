package ai.folded.fitstyle.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * The Data Access Object for the ReferencePhoto class.
 */
@Dao
interface StyleImageDao {
    @Query("SELECT * FROM style_images")
    fun getStyles(): LiveData<List<StyleImage>>

    @Query("SELECT * FROM style_images WHERE id = :imageId")
    fun getStyle(imageId: Int): LiveData<StyleImage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(images: List<StyleImage>)
}