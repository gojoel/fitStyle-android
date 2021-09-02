package ai.folded.fitstyle.viewmodels

import ai.folded.fitstyle.api.FitStyleApi
import ai.folded.fitstyle.api.StyleTransferResultResponse
import ai.folded.fitstyle.data.Status
import ai.folded.fitstyle.data.StyleOptions
import ai.folded.fitstyle.data.StyledImage
import ai.folded.fitstyle.repository.StyledImageRepository
import ai.folded.fitstyle.repository.UserRepository
import ai.folded.fitstyle.utils.CACHE_DIR_CHILD
import ai.folded.fitstyle.utils.TRANSFER_RETRIES
import android.app.Application
import android.graphics.Bitmap
import android.graphics.ImageDecoder.createSource
import android.graphics.ImageDecoder.decodeBitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import org.apache.commons.io.IOUtils
import java.io.File
import javax.annotation.Nullable
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody


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
        val contentFile = getFileRequestBody(styleOptions.photoUri, "content")
        var styleFile: MultipartBody.Part? = null

        styleOptions.customStyleUri?.let { styleUri ->
            styleFile = getFileRequestBody(styleUri, "custom_style")
        }

        val textMediaType = "text/plain".toMediaType()
        val userIdBody = userId.toRequestBody(textMediaType)
        val styleIdBody: RequestBody? = styleOptions.styleImage?.imageName()?.toRequestBody(textMediaType)

        try {
            val result = FitStyleApi.retrofitService.styleTransfer(
                userIdBody,
                contentFile!!,
                styleIdBody,
                styleFile
            )

            // track current job in progress
            jobId = result.jobId

            // continuously attempt to get the results with a retry limit and backoff delay
            getResult(result.jobId)
                .flowOn(Dispatchers.Default)
                .retry (retries = TRANSFER_RETRIES) {
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

    private fun getFileRequestBody(uri: Uri?, fileKey: String): MultipartBody.Part? {
        val fileUri = uri ?: return null
        val cachePath = File(getApplication<Application>().cacheDir, CACHE_DIR_CHILD)
        cachePath.mkdirs()

        val file = File("$cachePath/$fileKey.jpg")
        val resolver = getApplication<Application>().contentResolver
        try {
            resolver.openInputStream(fileUri)
                .use { inputStream ->
                    inputStream?.let {
                        file.outputStream().use { outputStream ->
                            IOUtils.copy(it, outputStream)
                        }
                    }
                }

            val imageFileType = "image/*".toMediaType()
            val requestFile = file.asRequestBody(imageFileType)
            return MultipartBody.Part.createFormData(fileKey, file.name, requestFile)
        } catch (e: Exception) {
            // TODO: handle exception
            return null
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

    fun getStyleImageBitmap(): Bitmap? {
      return getBitmapFromUri(styleOptions.customStyleUri)
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