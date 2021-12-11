package ai.folded.fitstyle.repository

import ai.folded.fitstyle.api.FitStyleApi
import ai.folded.fitstyle.api.FitStyleApiService
import ai.folded.fitstyle.api.PaymentRequest
import ai.folded.fitstyle.utils.CURRENCY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class PaymentRepository @Inject constructor() {
    suspend fun createWatermarkPayment(userId: String, requestId: String) = withContext(Dispatchers.IO) {
        flowOf(
            kotlin.runCatching {
                FitStyleApi.retrofitService.createWatermarkPayment(PaymentRequest(userId = userId, requestId = requestId, currency = CURRENCY))
            }
        )
    }

    suspend fun removeWatermark(userId: String, requestId: String) = withContext(Dispatchers.IO) {
        FitStyleApi.retrofitService.removeWatermark(userId, requestId)
    }
}