package ai.folded.fitstyle.viewmodels

import ai.folded.fitstyle.data.Status
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
import kotlinx.coroutines.flow.single
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
    private val paymentRepository: PaymentRepository,
    private val styledImageRepository: StyledImageRepository,
    private val userRepository: UserRepository,
    private val analyticsManager: AnalyticsManager,
) : AndroidViewModel(application) {

    val styledImage = MutableLiveData<StyledImage>()

    private val _downloadStatus = MutableLiveData<Status?>()

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
                    val userId = userRepository.getUserId()
                    downloadS3Image(userId, styledImage) {
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

    private fun downloadS3Image(userId: String, styledImage: StyledImage, callback: ((file: File?) -> Unit)) {
        try {
            val file = styledImageRepository.createImageFile(getApplication(), styledImage)
            if (file.exists()) {
                callback.invoke(file)
            } else {
                val options = StorageDownloadFileOptions.builder()
                    .accessLevel(StorageAccessLevel.PRIVATE)
                    .build()

                val userId = userId
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