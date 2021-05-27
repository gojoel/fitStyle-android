package ai.folded.fitstyle.viewmodels

import ai.folded.fitstyle.repository.StyledImageRepository
import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

/**
 * The ViewModel used in [StyledImageFragment]
 */
class StyledImageViewModel @AssistedInject constructor(
    @Assisted val imageId: String,
    private val styledImageRepository: StyledImageRepository
) : ViewModel() {

    val styledImage = styledImageRepository.get(imageId).asLiveData()

    companion object {
        fun provideFactory(
            assistedFactory: StyledImageViewModelFactory,
            imageId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(imageId) as T
            }
        }
    }
}

@AssistedFactory
interface StyledImageViewModelFactory {
    fun create(imageId: String): StyledImageViewModel
}