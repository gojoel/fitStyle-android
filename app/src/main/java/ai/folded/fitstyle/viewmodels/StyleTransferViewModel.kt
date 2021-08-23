package ai.folded.fitstyle.viewmodels

import ai.folded.fitstyle.api.FitStyleApi
import ai.folded.fitstyle.api.StyleTransferResultResponse
import ai.folded.fitstyle.data.Status
import ai.folded.fitstyle.data.StyleOptions
import ai.folded.fitstyle.data.StyledImage
import ai.folded.fitstyle.repository.StyledImageRepository
import ai.folded.fitstyle.repository.UserRepository
import ai.folded.fitstyle.utils.AwsUtils
import android.app.Application
import android.graphics.Bitmap
import android.graphics.ImageDecoder.createSource
import android.graphics.ImageDecoder.decodeBitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64.DEFAULT
import android.util.Base64.encodeToString
import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.FormBody
import java.io.ByteArrayOutputStream
import javax.annotation.Nullable

/**
 * The ViewModel used in [StyleTransferFragment].
 */

class StyleTransferViewModel @AssistedInject constructor(
    application: Application,
    @Assisted val styleOptions: StyleOptions,
    private val styledImageRepository: StyledImageRepository,
    private val userRepository: UserRepository
) : AndroidViewModel(application) {

    @Volatile
    var jobId: String? = null

    private val _status = MutableLiveData<Status?>()

    val status: LiveData<Status?>
        get() = _status

    private val _response = MutableLiveData<StyledImage?>()

    val response: LiveData<StyledImage?>
        get() = _response

    init {
        startStyleTransfer()
    }

    fun startStyleTransfer() {
        viewModelScope.launch {
            _status.value = Status.WAITING
            try {
                val userId = userRepository.getUserId()
                callStyleTransfer(userId)
            } catch (e: Exception) {
                // TODO: handle and log error
                _status.value = Status.FAILED
            }
        }
    }

    suspend fun cancelStyleTransfer() = withContext(Dispatchers.IO) {
        jobId?.let {
            try {
                FitStyleApi.retrofitService.cancelStyleTransferTask(it)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _status.value = Status.FAILED
                }
            }
        }
    }

    private suspend fun callStyleTransfer(userId: String) = withContext(Dispatchers.IO) {
        val photoBitmap = getBitmapFromUri(styleOptions.photoUri)
        val styleBitmap = getBitmapFromUri(styleOptions.customStyleUri)

        val encodedPhoto = if (photoBitmap != null) convertBitmap(photoBitmap) else ""
        if (encodedPhoto.isEmpty()) {
            // TODO: handle error
            cancel()
        }

        val encodedCustomStyle = if (styleBitmap != null) convertBitmap(styleBitmap) else null
        if (encodedCustomStyle.isNullOrEmpty() && styleOptions.styleImage?.key.isNullOrEmpty()) {
            // TODO: handle error
            cancel()
        }

        val builder = FormBody.Builder()
            .add("user_id", userId)
            .add("content", encodedPhoto);

        if (encodedCustomStyle != null) {
            builder.add("custom_style", encodedCustomStyle)
        }

        if (styleOptions.styleImage?.imageName() != null) {
            builder.add("style_id", styleOptions.styleImage?.imageName()!!)
        }

        try {
            val result = FitStyleApi.retrofitService.styleTransfer(builder.build())

            // track current job in progress
            jobId = result.jobId

            // continuously attempt to get the results with a retry limit and backoff delay
            getResult(result.jobId)
                .flowOn(Dispatchers.Default)
                .retry (retries = 100) {
                    delay(5000)
                    return@retry true
                }.catch {
                    withContext(Dispatchers.Main) {
                        _status.value = Status.FAILED
                    }
                }
                .collect { response ->
                    val styledImage = styledImageRepository.create(response.requestId, userId)
                    withContext(Dispatchers.Main) {
                        _response.value = styledImage
                        _status.value = Status.SUCCESS
                    }
                }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                _status.value = Status.FAILED
            }
        }
    }

    private fun getResult(jobId: String): Flow<StyleTransferResultResponse> {
        return flow {
            val result = FitStyleApi.retrofitService.styleTransferResult(jobId)
            emit(result)
        }
    }

    private fun convertBitmap(bitmap: Bitmap) : String {
        return try {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
            encodeToString(byteArray, DEFAULT)
        } catch (e: Exception) {
            ""
        }
    }

    @Nullable
    private fun getBitmapFromUri(uri: Uri?) : Bitmap? {
        uri?.let {
            return try {
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = createSource(getApplication<Application>().contentResolver, uri)
                    decodeBitmap(source)
                } else {
                    MediaStore.Images.Media.getBitmap(
                        getApplication<Application>().contentResolver,
                        uri
                    )
                }
            } catch (e: Exception) {
                null
            }
        }

        return null
    }

    fun getPhoto() : Bitmap? {
        return getBitmapFromUri(styleOptions.photoUri)
    }

    fun getStyleImage() : String {
        return styleOptions.styleImage?.let {
            AwsUtils.generateUrl(it.key)?.toString() ?: ""
        } ?: run {
            styleOptions.customStyleUri?.toString() ?: ""
        }
    }

    fun clearTransferState() {
        _response.value = null
        _status.value = null
    }

    companion object {
        fun provideFactory(
            assistedFactory: StyleTransferViewModelFactory,
            styleOptions: StyleOptions
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(styleOptions) as T
            }
        }
    }
}

@AssistedFactory
interface StyleTransferViewModelFactory {
    fun create(styleOptions: StyleOptions): StyleTransferViewModel
}