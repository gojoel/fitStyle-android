package ai.folded.fitstyle.viewmodels

import ai.folded.fitstyle.api.ResultImage
import ai.folded.fitstyle.utils.BUCKET_STYLED_IMAGE_PREFIX
import ai.folded.fitstyle.utils.STYLED_IMAGE_NAME
import androidx.lifecycle.*
import com.amplifyframework.core.Amplify
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import java.lang.StringBuilder

/**
 * The ViewModel used in [StyleResultFragment].
 */
class StyleResultViewModel @AssistedInject constructor(
    @Assisted val result: ResultImage
) : ViewModel() {

    fun resultImageKey() : String {
        val requestId = result.requestId
        val userId = Amplify.Auth.currentUser.userId
        return StringBuilder(BUCKET_STYLED_IMAGE_PREFIX)
            .append(userId)
            .append(requestId)
            .append(STYLED_IMAGE_NAME)
            .toString()
    }

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