package ai.folded.fitstyle.viewmodels

import ai.folded.fitstyle.api.FitStyleApi
import ai.folded.fitstyle.api.ResultImage
import ai.folded.fitstyle.data.StyleOptions
import android.app.Application
import android.graphics.Bitmap
import android.graphics.ImageDecoder.createSource
import android.graphics.ImageDecoder.decodeBitmap
import android.net.Uri
import android.util.Base64.DEFAULT
import android.util.Base64.encodeToString
import android.widget.Toast
import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.annotation.Nullable


/**
 * The ViewModel used in [StyleTransferFragment].
 */
class StyleTransferViewModel @AssistedInject constructor(
    application: Application,
    @Assisted val styleOptions: StyleOptions
) : AndroidViewModel(application) {

    private val _response = MutableLiveData<ResultImage>()

    val response: LiveData<ResultImage>
        get() = _response

    init {
        styleTransfer()
    }

    private fun styleTransfer() {
        viewModelScope.launch {
            try {
                callStyleTransfer()
            } catch (e: Exception) {
                // TODO: handle and log error
                Toast.makeText(getApplication(), e.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun callStyleTransfer() = withContext(Dispatchers.IO) {
        val photoBitmap = getBitmapFromUri(styleOptions.photoUri)
        val styleBitmap = getBitmapFromUri(styleOptions.customStyleUri)

        val encodedPhoto = if (photoBitmap != null) convertBitmap(photoBitmap) else ""
        val encodedCustomStyle = if (styleBitmap != null) convertBitmap(styleBitmap) else ""

        if (encodedPhoto.isNotEmpty() && (encodedCustomStyle.isNotEmpty() || styleOptions.styleImage != null)) {
            val result = FitStyleApi.retrofitService.styleTransfer(encodedPhoto,
                encodedCustomStyle,
                styleOptions.styleImage?.id)

            withContext(Dispatchers.Main) {
                _response.value = result
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
        val url: String? = styleOptions.customStyleUri?.toString() ?: styleOptions.styleImage?.imageUrl
        return url ?: ""
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