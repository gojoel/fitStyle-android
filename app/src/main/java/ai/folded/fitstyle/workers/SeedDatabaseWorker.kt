package ai.folded.fitstyle.workers

import ai.folded.fitstyle.data.AppDatabase
import ai.folded.fitstyle.data.StyleImage
import ai.folded.fitstyle.utils.STYLE_IMAGES_FILENAME
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import kotlinx.coroutines.coroutineScope

class SeedDatabaseWorker(
        context: Context,
        workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result = coroutineScope {
        try {
            applicationContext.assets.open(STYLE_IMAGES_FILENAME).use { inputStream ->
                JsonReader(inputStream.reader()).use { jsonReader ->
                    val type = object : TypeToken<List<StyleImage>>() {}.type
                    val styleList: List<StyleImage> = Gson().fromJson(jsonReader, type)

                    val database = AppDatabase.getInstance(applicationContext)
                    database.styleImageDao().insertAll(styleList)
                }
            }

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