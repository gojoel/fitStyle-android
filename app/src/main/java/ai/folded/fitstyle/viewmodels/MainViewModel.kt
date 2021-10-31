package ai.folded.fitstyle.viewmodels

import ai.folded.fitstyle.repository.UserRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userId = MutableLiveData<String>()

    val userId: LiveData<String>
        get() = _userId

    fun fetchUser() {
        viewModelScope.launch {
            val userId = userRepository.getUserId()
            _userId.value = userId
        }
    }
}

