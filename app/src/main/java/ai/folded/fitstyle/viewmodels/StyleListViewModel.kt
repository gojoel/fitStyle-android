package ai.folded.fitstyle.viewmodels

import ai.folded.fitstyle.data.StyleImage
import ai.folded.fitstyle.utils.BUCKET_PUBLIC_PREFIX
import ai.folded.fitstyle.utils.STYLE_IMAGES_PATH
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.StorageException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


@HiltViewModel
class StyleListViewModel @Inject internal constructor(
) : ViewModel() {

    private val _images = MutableLiveData<List<StyleImage>>()

    val images: LiveData<List<StyleImage>>
        get() = _images

    init {
        getStyleImages()
    }

    private fun getStyleImages() {
        viewModelScope.launch {
            try {
                val result = fetchImages()
                _images.value = result
            } catch (e: StorageException) {
                // TODO: handle and log error
            }
        }
    }

    private suspend fun fetchImages() : List<StyleImage> {
        return suspendCoroutine {continuation ->
            Amplify.Storage.list(STYLE_IMAGES_PATH,
                { result ->
                    val styleImages = arrayListOf<StyleImage>()
                    result.items.forEach { item ->
                        if (!item.key.endsWith("/")) {
                            styleImages.add(StyleImage("${BUCKET_PUBLIC_PREFIX}${item.key}"))
                        }
                    }

                    continuation.resume(styleImages)
                },
                {
                    continuation.resumeWithException(it)
                }
            )
        }
    }
}