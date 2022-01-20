package ai.folded.fitstyle.viewmodels

import ai.folded.fitstyle.data.Status
import ai.folded.fitstyle.data.StyledImage
import ai.folded.fitstyle.repository.PaymentRepository
import ai.folded.fitstyle.repository.StyledImageRepository
import ai.folded.fitstyle.repository.UserRepository
import ai.folded.fitstyle.utils.AnalyticsManager
import android.app.Application
import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.*
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.StorageAccessLevel
import com.amplifyframework.storage.options.StorageDownloadFileOptions
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.single
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
    private val paymentRepository: PaymentRepository,
    private val styledImageRepository: StyledImageRepository,
    private val userRepository: UserRepository,
    private val analyticsManager: AnalyticsManager,
) : AndroidViewModel(application) {

    private val collection =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Images.Media.getContentUri(
            MediaStore.VOLUME_EXTERNAL
        ) else MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    val styledImage = MutableLiveData<StyledImage>()

    private val _downloadStatus = MutableLiveData<Status?>()

    val downloadStatus: LiveData<Status?>
        get() = _downloadStatus

    private val _shareableImage = MutableLiveData<File?>()

    val shareableImage: LiveData<File?>
        get() = _shareableImage

    val paymentProgress = MutableLiveData<Boolean>()

    val paymentStatus = MutableLiveData<Status>()

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
                    val file = downloadS3Image(styledImage)
                    _shareableImage.value = file
                } catch (e: Exception) {
                    analyticsManager.logError(AnalyticsManager.FitstyleError.SHARE, e.localizedMessage)
                    _shareableImage.value = null
                }
            }
        }
    }

    fun downloadImage() {
        viewModelScope.launch {
            styledImage.value?.let { styledImage ->
                downloadQ(styledImage)
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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
            analyticsManager.logError(AnalyticsManager.FitstyleError.DOWNLOAD, e.localizedMessage)
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

    fun preparePaymentRequest() = liveData {
        paymentProgress.postValue(true)
        paymentStatus.postValue(Status.WAITING)

        val userId = userRepository.getUserId();
        val paymentResponse = paymentRepository.createWatermarkPayment(userId, styledImage.value!!.requestId).single()

        paymentResponse.fold(
            onSuccess = {
                paymentStatus.postValue(Status.SUCCESS)
            },
            onFailure = {
                analyticsManager.logError(AnalyticsManager.FitstyleError.PAYMENT, it.localizedMessage)
                paymentStatus.postValue(Status.FAILED)
            }
        )

        paymentProgress.postValue(false)
        emit(paymentResponse.getOrNull())
    }

    fun removeWatermark(styledImage: StyledImage) = liveData {
        paymentProgress.postValue(true)

        var successful = false
        try {
            val userId = userRepository.getUserId();
            paymentRepository.removeWatermark(userId, styledImage.requestId)

            val file = styledImageRepository.createImageFile(getApplication(), styledImage)
            if (file.exists()) {
                file.delete()
            }

            styledImage.updatedAt = Date().time
            styledImage.purchased = true
            styledImageRepository.update(styledImage)

            successful = true

        } catch (e: Exception) {
            analyticsManager.logError(AnalyticsManager.FitstyleError.WATERMARK, e.localizedMessage)
        }

        paymentProgress.postValue(false)

        emit(
            if (successful) {
                Status.SUCCESS
            } else {
                analyticsManager.logError(AnalyticsManager.FitstyleError.WATERMARK, "Failed to remove watermark")
                Status.FAILED
            }
        )
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