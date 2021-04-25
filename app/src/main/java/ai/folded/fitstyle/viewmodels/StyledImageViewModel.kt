package ai.folded.fitstyle.viewmodels

import ai.folded.fitstyle.data.StyledImage
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

/**
 * The ViewModel used in [StyledImageFragment].
 */
class StyledImageViewModel @AssistedInject constructor(
    @Assisted val styledImage: StyledImage
) : ViewModel() {

    companion object {
        fun provideFactory(
            assistedFactory: StyledImageViewModelFactory,
            styledImage: StyledImage
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(styledImage) as T
            }
        }
    }
}

@AssistedFactory
interface StyledImageViewModelFactory {
    fun create(styledImage: StyledImage): StyledImageViewModel
}