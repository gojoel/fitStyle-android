package ai.folded.fitstyle.viewmodels

import ai.folded.fitstyle.api.ResultImage
import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

/**
 * The ViewModel used in [StyleResultFragment].
 */
class StyleResultViewModel @AssistedInject constructor(
    @Assisted val resultImage: ResultImage
) : ViewModel() {

    companion object {
        fun provideFactory(
            assistedFactory: StyleResultViewModelFactory,
            resultImage: ResultImage
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(resultImage) as T
            }
        }
    }
}

@AssistedFactory
interface StyleResultViewModelFactory {
    fun create(resultImage: ResultImage): StyleResultViewModel
}