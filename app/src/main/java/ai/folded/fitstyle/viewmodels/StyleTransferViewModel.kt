package ai.folded.fitstyle.viewmodels

import ai.folded.fitstyle.api.FitStyleApi
import ai.folded.fitstyle.api.ResultImage
import ai.folded.fitstyle.data.StyleOptions
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
import kotlin.coroutines.suspendCoroutine


/**
 * The ViewModel used in [StyleTransferFragment].
 */

class StyleTransferViewModel @AssistedInject constructor(
    application: Application,
    @Assisted val styleOptions: StyleOptions
) : AndroidViewModel(application) {

    private val _errorStatus = MutableLiveData<Boolean>()

    val errorStatus: LiveData<Boolean>
        get() = _errorStatus

    private val _response = MutableLiveData<ResultImage>()

    val response: LiveData<ResultImage>
        get() = _response

    init {
        styleTransfer()
    }

    private fun styleTransfer() {
        viewModelScope.launch {
            val userId = getUserId()
            try {
                callStyleTransfer(userId)
            } catch (e: Exception) {
                // TODO: handle and log error
                Toast.makeText(getApplication(), e.localizedMessage, Toast.LENGTH_SHORT).show()
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
                            continuation.resume("")
                        }
                    }
                },
                {
                    // TODO: log failure to fetch session
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

            withContext(Dispatchers.Main) {
                _response.value = result
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                _errorStatus.value = true
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