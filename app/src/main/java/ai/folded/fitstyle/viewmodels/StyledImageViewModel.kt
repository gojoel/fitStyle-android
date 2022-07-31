package ai.folded.fitstyle.viewmodels

import ai.folded.fitstyle.data.StyledImage
import ai.folded.fitstyle.repository.PaymentRepository
import ai.folded.fitstyle.repository.StyledImageRepository
import ai.folded.fitstyle.repository.UserRepository
import ai.folded.fitstyle.utils.AnalyticsManager
import android.app.Application
import androidx.lifecycle.*
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.StorageAccessLevel
import com.amplifyframework.storage.options.StorageDownloadFileOptions
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.*

/**
 * The ViewModel used in [StyledImageFragment]
 */
class StyledImageViewModel @AssistedInject constructor(
    application: Application,
    @Assisted val imageId: String,
    private val styledImageRepository: StyledImageRepository,
    private val analyticsManager: AnalyticsManager,
) : AndroidViewModel(application) {

    val styledImage = MutableLiveData<StyledImage>()

    private var _shareableImage = MutableLiveData<File?>()

    val shareableImage: LiveData<File?>
        get() = _shareableImage

    init {
        updateStyledImage()
    }

    fun updateStyledImage() {
        viewModelScope.launch {
            styledImageRepository.get(imageId).collect { image ->
                styledImage.value = image
            }
        }
    }

    fun shareImage() {
        viewModelScope.launch {
            styledImage.value?.let { styledImage ->
                try {
                    downloadS3Image(styledImage) {
                        _shareableImage.value = it
                    }
                } catch (e: Exception) {
                    analyticsManager.logError(AnalyticsManager.FitstyleError.SHARE, e.localizedMessage)
                    _shareableImage.value = null
                }
            }
        }
    }

    fun resetShareableState() {
        _shareableImage.value = null
    }

    private fun downloadS3Image(styledImage: StyledImage, callback: ((file: File?) -> Unit)) {
        try {
            val file = styledImageRepository.createImageFile(getApplication(), styledImage)
            if (file.exists()) {
                callback.invoke(file)
            } else {
                val options = StorageDownloadFileOptions.builder()
                    .accessLevel(StorageAccessLevel.PRIVATE)
                    .build()

                val key = styledImage.downloadKey()

                Amplify.Storage.downloadFile(key, file, options,
                    { result ->
                        callback.invoke(result.file)
                    },
                    { error ->
                        file.delete()
                        callback.invoke(null)
                    }
                )
            }
        } catch (e: IOException) {
            callback.invoke(null)
        }
    }

    companion object {
        fun provideFactory(
            assistedFactory: StyledImageViewModelFactory,
            imageId: String,
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