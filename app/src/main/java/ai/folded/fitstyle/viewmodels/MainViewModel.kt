package ai.folded.fitstyle.viewmodels

import ai.folded.fitstyle.repository.UserRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    fun fetchUser() {
        viewModelScope.launch {
            userRepository.getUserId()
        }
    }
}

