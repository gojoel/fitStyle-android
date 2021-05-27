package ai.folded.fitstyle.viewmodels

import ai.folded.fitstyle.repository.StyledImageRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StyledListViewModel @Inject internal constructor(
    private val styledImageRepository: StyledImageRepository
) : ViewModel() {

    val images = styledImageRepository.getAll().asLiveData()
}