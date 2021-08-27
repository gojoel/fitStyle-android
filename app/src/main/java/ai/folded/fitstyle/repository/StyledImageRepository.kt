package ai.folded.fitstyle.repository

import ai.folded.fitstyle.data.StyledImage
import ai.folded.fitstyle.data.StyledImageDao
import ai.folded.fitstyle.utils.BUCKET_PRIVATE_PREFIX
import ai.folded.fitstyle.utils.BUCKET_REQUESTS
import kotlinx.coroutines.flow.Flow
import android.content.Context
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StyledImageRepository @Inject constructor(
    private val styledImageDao: StyledImageDao
) {
    suspend fun create(requestId: String, userId: String): StyledImage {
        val imagePath = "$BUCKET_PRIVATE_PREFIX${userId}/$BUCKET_REQUESTS${requestId}/"
        val styledImage = StyledImage(requestId, imagePath)
        styledImageDao.insert(styledImage)

        return styledImage
    }

    suspend fun add(styledImage: StyledImage) {
        styledImageDao.insert(styledImage)
    }

    suspend fun update(styledImage: StyledImage) {
        styledImageDao.update(styledImage)
    }

    fun get(id: String): Flow<StyledImage> = styledImageDao.get(id)

    fun getAll(): Flow<List<StyledImage>> = styledImageDao.getAll()

    suspend fun clear() = styledImageDao.clear()

    fun createImageFile(context: Context, styledImage: StyledImage): File {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        return File("$cachePath/${styledImage.requestId}.jpg")
    }
}