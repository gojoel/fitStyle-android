package ai.folded.fitstyle.viewmodels

import ai.folded.fitstyle.data.StyleImage
import ai.folded.fitstyle.data.StyleImageRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StyleListViewModel @Inject internal constructor(
    styleImageRepository: StyleImageRepository
) : ViewModel() {
    val styleImages: LiveData<List<StyleImage>> = styleImageRepository.getStyles()
}