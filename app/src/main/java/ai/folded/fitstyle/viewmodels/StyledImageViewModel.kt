package ai.folded.fitstyle.viewmodels

import ai.folded.fitstyle.data.StyledImage
import androidx.lifecycle.*

/**
 * The ViewModel used in [StyledImageFragment].Z
 */
internal class StyledImageViewModel(
    val styledImage: StyledImage,
) : ViewModel() {

    internal class Factory(
        private val styledImage: StyledImage,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return StyledImageViewModel(
                styledImage,
            ) as T
        }
    }
}
