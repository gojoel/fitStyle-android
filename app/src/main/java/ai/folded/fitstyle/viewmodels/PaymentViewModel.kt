package ai.folded.fitstyle.viewmodels

import ai.folded.fitstyle.api.FitStyleApi
import ai.folded.fitstyle.data.Status
import ai.folded.fitstyle.data.StyledImage
import ai.folded.fitstyle.repository.PaymentRepository
import ai.folded.fitstyle.repository.StyledImageRepository
import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.single
import kotlin.coroutines.CoroutineContext

internal class PaymentViewModel (
    application: Application,
    val styledImage: StyledImage,
    private val repository: PaymentRepository,
    private val styledImageRepository: StyledImageRepository
) : AndroidViewModel(application) {
    val progress = MutableLiveData<Boolean>()
    val status = MutableLiveData<Status>()

    fun preparePaymentRequest() = liveData {
        progress.postValue(true)
        status.postValue(Status.WAITING)

        val paymentResponse = repository.createWatermarkPayment(styledImage.requestId).single()

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
            repository.removeWatermark(styledImage.requestId)
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


    fun test(styledImage: StyledImage) = liveData {
        progress.postValue(true)

        styledImage.purchased = true
        styledImageRepository.update(styledImage)

        progress.postValue(false)

        emit(
            Status.SUCCESS
        )
    }

    internal class Factory(
        private val application: Application,
        private val styledImage: StyledImage,
        private val workContext: CoroutineContext = Dispatchers.IO,
        private val styledImageRepository: StyledImageRepository
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
                styledImageRepository
            ) as T
        }
    }
}
