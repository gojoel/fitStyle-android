package ai.folded.fitstyle.viewmodels

import ai.folded.fitstyle.data.StyleOptions
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

/**
 * The ViewModel used in [StyleTransferFragment].
 */
class StyleTransferViewModel @AssistedInject constructor(
    @Assisted val styleOptions: StyleOptions
) : ViewModel() {

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