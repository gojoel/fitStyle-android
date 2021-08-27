package ai.folded.fitstyle.viewmodels

import ai.folded.fitstyle.api.FitStyleApi
import ai.folded.fitstyle.data.Status
import ai.folded.fitstyle.data.StyledImage
import ai.folded.fitstyle.repository.PaymentRepository
import ai.folded.fitstyle.repository.StyledImageRepository
import ai.folded.fitstyle.repository.UserRepository
import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.single
import java.util.*
import kotlin.coroutines.CoroutineContext

internal class PaymentViewModel (
    application: Application,
    val styledImage: StyledImage,
    private val repository: PaymentRepository,
    private val styledImageRepository: StyledImageRepository,
    private val userRepository: UserRepository
) : AndroidViewModel(application) {
    val progress = MutableLiveData<Boolean>()
    val status = MutableLiveData<Status>()

    fun preparePaymentRequest() = liveData {
        progress.postValue(true)
        status.postValue(Status.WAITING)

        val userId = userRepository.getUserId();
        val paymentResponse = repository.createWatermarkPayment(userId, styledImage.requestId).single()

        paymentResponse.fold(
            onSuccess = {
                status.postValue(Status.SUCCESS)
            },
            onFailure = {
                status.postValue(Status.FAILED)
            }
        )

        progress.postValue(false)
        emit(paymentResponse.getOrNull())
    }

    fun removeWatermark(styledImage: StyledImage) = liveData {
        progress.postValue(true)

        var successful = false
        try {
            val userId = userRepository.getUserId();
            repository.removeWatermark(userId, styledImage.requestId)

            val file = styledImageRepository.createImageFile(getApplication(), styledImage)
            if (file.exists()) {
                file.delete()
            }

            styledImage.updatedAt = Date().time
            styledImage.purchased = true
            styledImageRepository.update(styledImage)

            successful = true

        } catch (e: Exception) {
            // TODO: log exception
        }

        progress.postValue(false)

        emit(
            if (successful) {
                Status.SUCCESS
            } else {
                Status.FAILED
            }
        )
    }

    internal class Factory(
        private val application: Application,
        private val styledImage: StyledImage,
        private val workContext: CoroutineContext = Dispatchers.IO,
        private val styledImageRepository: StyledImageRepository,
        private val userRepository: UserRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            val repository = PaymentRepository(
                FitStyleApi.retrofitService,
                workContext
            )

            return PaymentViewModel(
                application,
                styledImage,
                repository,
                styledImageRepository,
                userRepository
            ) as T
        }
    }
}
