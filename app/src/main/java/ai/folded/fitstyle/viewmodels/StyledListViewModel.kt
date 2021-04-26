package ai.folded.fitstyle.viewmodels

import ai.folded.fitstyle.utils.BUCKET_PRIVATE_PREFIX
import ai.folded.fitstyle.utils.BUCKET_REQUESTS
import ai.folded.fitstyle.utils.STYLED_IMAGE_NAME
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession
import com.amplifyframework.auth.result.AuthSessionResult
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.StorageAccessLevel
import com.amplifyframework.storage.StorageException
import com.amplifyframework.storage.options.StorageListOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@HiltViewModel
class StyledListViewModel @Inject internal constructor(
) : ViewModel() {

    private val _emptyStatus = MutableLiveData<Boolean>()

    val emptyStatus: LiveData<Boolean>
        get() = _emptyStatus

    private val _images = MutableLiveData<List<String>>()

    val images: LiveData<List<String>>
        get() = _images

    init {
        fetchUserImages()
    }

    private fun fetchUserImages() {
        viewModelScope.launch {
            try {
                val userId = getUserId()
                val result = fetchImages(userId)
                _images.value = result
            } catch (e: StorageException) {
                // TODO: handle and log error
            }
        }
    }

    private suspend fun getUserId() : String {
        return suspendCoroutine {continuation ->
            Amplify.Auth.fetchAuthSession(
                {
                    val session = it as AWSCognitoAuthSession
                    when (session.identityId.type) {

                        AuthSessionResult.Type.SUCCESS -> {
                            continuation.resume(session.identityId.value ?: "")
                        }
                        AuthSessionResult.Type.FAILURE -> {
                            //  TODO: log failure to retrieve identity id
                            continuation.resume("")
                        }
                    }
                },
                {
                    // TODO: log failure to fetch session
                }
            )
        }
    }

    private suspend fun fetchImages(userId: String) : List<String> {
        return suspendCoroutine {continuation ->

            val options = StorageListOptions.builder()
                .accessLevel(StorageAccessLevel.PRIVATE)
                .build()

            Amplify.Storage.list(
                BUCKET_REQUESTS, options,
                { result ->
                    val styledImageKeys = arrayListOf<String>()
                    result.items.forEach { item ->
                        if (!item.key.endsWith("/") && item.key.contains(STYLED_IMAGE_NAME)) {
                            val imagePath = "$BUCKET_PRIVATE_PREFIX$userId/${item.key}"
                            styledImageKeys.add(imagePath)
                        }
                    }

                    continuation.resume(styledImageKeys)
                },
                {
                    continuation.resumeWithException(it)
                }
            )
        }
    }
}