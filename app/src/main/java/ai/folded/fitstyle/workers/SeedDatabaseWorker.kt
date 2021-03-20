package ai.folded.fitstyle.workers

import ai.folded.fitstyle.data.AppDatabase
import ai.folded.fitstyle.data.StyleImage
import ai.folded.fitstyle.utils.STYLE_IMAGES_FILENAME
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

import kotlinx.coroutines.coroutineScope

class SeedDatabaseWorker(
        context: Context,
        workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result = coroutineScope {
        try {
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val listType = Types.newParameterizedType(List::class.java, StyleImage::class.java)
            val adapter: JsonAdapter<List<StyleImage>> = moshi.adapter(listType)

            val json = applicationContext.assets.open(STYLE_IMAGES_FILENAME).bufferedReader().use{ it.readText()}
            val styleList : List<StyleImage>? = adapter.fromJson(json)

            val database = AppDatabase.getInstance(applicationContext)
            database.styleImageDao().insertAll(styleList ?: emptyList())

            Result.success()
        } catch (ex: Exception) {
            Log.e(TAG, "Error seeding database", ex)
            Result.failure()
        }
    }

    companion object {
        private const val TAG = "SeedDatabaseWorker"
    }
}