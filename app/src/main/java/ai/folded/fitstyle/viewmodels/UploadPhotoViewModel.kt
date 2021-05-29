package ai.folded.fitstyle.viewmodels

import ai.folded.fitstyle.data.StyleOptions
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

/**
 * The ViewModel used in [UploadPhotoFragment].
 */
class UploadPhotoViewModel @AssistedInject constructor(
    @Assisted val styleOptions: StyleOptions
) : ViewModel() {

    private val _navigateToStyleTransfer = MutableLiveData<StyleOptions?>()

    val navigateToStyleTransfer
        get() = _navigateToStyleTransfer

    fun onPhotoUploaded(photoUri: Uri) {
        styleOptions.photoUri = photoUri
        _navigateToStyleTransfer.value = styleOptions
    }

    fun clearSelection() {
        _navigateToStyleTransfer.value = null
    }

    companion object {
        fun provideFactory(
            assistedFactory: UploadPhotoViewModelFactory,
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
interface UploadPhotoViewModelFactory {
    fun create(styleOptions: StyleOptions): UploadPhotoViewModel
}