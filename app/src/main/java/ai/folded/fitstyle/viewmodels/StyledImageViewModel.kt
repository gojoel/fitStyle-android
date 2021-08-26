package ai.folded.fitstyle.viewmodels

import ai.folded.fitstyle.data.Status
import ai.folded.fitstyle.data.StyledImage
import ai.folded.fitstyle.repository.StyledImageRepository
import android.app.Application
import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.*
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.StorageAccessLevel
import com.amplifyframework.storage.options.StorageDownloadFileOptions
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * The ViewModel used in [StyledImageFragment]
 */
class StyledImageViewModel @AssistedInject constructor(
    application: Application,
    @Assisted val imageId: String,
    val styledImageRepository: StyledImageRepository
) : AndroidViewModel(application) {

    private val collection =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Images.Media.getContentUri(
            MediaStore.VOLUME_EXTERNAL
        ) else MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    val styledImage = styledImageRepository.get(imageId).asLiveData()

    private val _downloadStatus = MutableLiveData<Status?>()

    val downloadStatus: LiveData<Status?>
        get() = _downloadStatus

    private val _shareableImage = MutableLiveData<File?>()

    val shareableImage: LiveData<File?>
        get() = _shareableImage


    fun shareImage() {
        viewModelScope.launch {
            styledImage.value?.let { styledImage ->
                suspend {
                    try {
                        val file = downloadS3Image(styledImage)
                        _shareableImage.value = file
                    } catch (e: Exception) {
                        // TODO: handle exception
                    }
                }
            }
        }
    }

    fun downloadImage() {
        viewModelScope.launch {
            styledImage.value?.let { styledImage ->
                suspend {
                    downloadQ(styledImage)
                }
            }
        }
    }

    fun resetSavedState() {
        _downloadStatus.value = null
    }

    fun resetShareableState() {
        _shareableImage.value = null
    }

    private suspend fun downloadQ(styledImage: StyledImage) = withContext(Dispatchers.IO) {
        try {
            val file = downloadS3Image(styledImage)
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, styledImage.requestId)
                put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { //this one
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val resolver = getApplication<Application>().contentResolver
            val optionalUri = resolver.insert(collection, contentValues)
            optionalUri?.let { uri ->
                resolver.openOutputStream(uri)
                    .use { os ->
                        os?.write(file.readBytes())
                        os?.close()
                    }

                contentValues.clear()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                }

                resolver.update(uri, contentValues, null, null)
                withContext(Dispatchers.Main) {
                    _downloadStatus.value = Status.SUCCESS
                }
            }
        } catch (e: Exception) {
            // TODO: handle exception
            withContext(Dispatchers.Main) {
                _downloadStatus.value = Status.FAILED
            }
        }
    }

    private suspend fun downloadS3Image(styledImage: StyledImage) = suspendCoroutine<File> {
        try {
            val file = styledImageRepository.createImageFile(getApplication(), styledImage)
            if (file.exists()) {
                it.resume(file)
                return@suspendCoroutine
            }

            val options = StorageDownloadFileOptions.builder()
                .accessLevel(StorageAccessLevel.PRIVATE)
                .build()
            val key = styledImage.downloadKey()

            Amplify.Storage.downloadFile(key, file, options,
                { result ->
                    it.resume(result.file)
                },
                { error ->
                    file.delete()
                    it.resumeWithException(error)
                }
            )
        } catch (e: IOException) {
            it.resumeWithException(e)
        }
    }

    companion object {
        fun provideFactory(
            assistedFactory: StyledImageViewModelFactory,
            imageId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(imageId) as T
            }
        }
    }
}

@AssistedFactory
interface StyledImageViewModelFactory {
    fun create(imageId: String): StyledImageViewModel
}