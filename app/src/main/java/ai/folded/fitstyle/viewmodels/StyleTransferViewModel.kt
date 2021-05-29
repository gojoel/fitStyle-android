package ai.folded.fitstyle.viewmodels

import ai.folded.fitstyle.api.FitStyleApi
import ai.folded.fitstyle.data.Status
import ai.folded.fitstyle.data.StyleOptions
import ai.folded.fitstyle.data.StyledImage
import ai.folded.fitstyle.repository.StyledImageRepository
import ai.folded.fitstyle.utils.AwsUtils
import android.app.Application
import android.graphics.Bitmap
import android.graphics.ImageDecoder.createSource
import android.graphics.ImageDecoder.decodeBitmap
import android.net.Uri
import android.util.Base64.DEFAULT
import android.util.Base64.encodeToString
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession
import com.amplifyframework.auth.result.AuthSessionResult
import com.amplifyframework.core.Amplify
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.annotation.Nullable
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


/**
 * The ViewModel used in [StyleTransferFragment].
 */

class StyleTransferViewModel @AssistedInject constructor(
    application: Application,
    @Assisted val styleOptions: StyleOptions,
    private val styledImageRepository: StyledImageRepository
) : AndroidViewModel(application) {

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
                val userId = getUserId()
                callStyleTransfer(userId)
            } catch (e: Exception) {
                // TODO: handle and log error
                _status.value = Status.FAILED
            }
        }
    }

    private suspend fun getUserId() : String {
        return suspendCoroutine {continuation ->
            Amplify.Auth.fetchAuthSession(
                {
                    val session = it as AWSCognitoAuthSession
                    when (session.identityId.type) {

                        AuthSessionResult.Type.SUCCESS -> {
                            continuation.resume(session.identityId.value ?: "")
                        }
                        AuthSessionResult.Type.FAILURE -> {
                            //  TODO: log failure to retrieve identity id
                            continuation.resumeWithException(Exception("Unable to retrieve user"))
                        }
                    }
                },
                {
                    // TODO: log failure to fetch session
                    continuation.resumeWithException(it)
                }
            )
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

        try {
            val result = FitStyleApi.retrofitService.styleTransfer(
                userId,
                encodedPhoto,
                encodedCustomStyle,
                styleOptions.styleImage?.imageName()
            )

            val styledImage = styledImageRepository.create(result.requestId, userId)
            withContext(Dispatchers.Main) {
                _response.value = styledImage
                _status.value = Status.SUCCESS
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                _status.value = Status.FAILED
            }
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
        if (uri == null) return null
        return try {
            val source = createSource(getApplication<Application>().contentResolver, uri)
            decodeBitmap(source)
        } catch (e: Exception) {
            null
        }
    }

    fun getPhoto() : String {
        return styleOptions.photoUri?.toString() ?: ""
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